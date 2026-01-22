package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.search_sort_newest
import hachimiworld.composeapp.generated.resources.search_sort_oldest
import hachimiworld.composeapp.generated.resources.search_sort_relevance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.PlaylistModule
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

class SearchViewModel(
    private val global: GlobalStore,
    private val api: ApiClient
) : ViewModel(
    CoroutineScope(Dispatchers.Default)
) {
    enum class SearchType {
        SONG, USER, ALBUM, PLAYLIST
    }

    enum class SortMethod(
        val labelRes: StringResource,
        val value: String
    ) {
        RELEVANCE(Res.string.search_sort_relevance, SongModule.SearchReq.SORT_BY_RELEVANCE),
        NEWEST(Res.string.search_sort_newest, SongModule.SearchReq.SORT_BY_RELEASE_TIME_DESC),
        OLDEST(Res.string.search_sort_oldest, SongModule.SearchReq.SORT_BY_RELEASE_TIME_ASC),
    }

    data class SearchSongItem(
        val info: SongModule.SearchSongItem,
        val matchTitleRanges: List<IntRange>,
        val matchDescRanges: List<IntRange>,
        val matchSubtitleRanges: List<IntRange>,
        val matchedOriginArtists: List<String>,
        val matchedOriginTitles: List<String>
    ) {
        companion object {
            fun fromItem(query: String, item: SongModule.SearchSongItem): SearchSongItem {
                val matchTitleRanges = query.toRegex(RegexOption.IGNORE_CASE).findAll(item.title).map { it.range }.toList()
                val matchDescRanges = query.toRegex(RegexOption.IGNORE_CASE).findAll(item.description).map { it.range }.toList()
                val matchSubtitleRanges = query.toRegex(RegexOption.IGNORE_CASE).findAll(item.subtitle).map { it.range }.toList()
                val matchedOriginArtists = item.originalArtists.filter { query.toRegex(RegexOption.IGNORE_CASE).find(it) != null }
                val matchedOriginalTitles = item.originalTitles.filter { query.toRegex(RegexOption.IGNORE_CASE).find(it) != null }
                return SearchSongItem(
                    info = item,
                    matchTitleRanges = matchTitleRanges,
                    matchDescRanges = matchDescRanges,
                    matchSubtitleRanges = matchSubtitleRanges,
                    matchedOriginArtists = matchedOriginArtists,
                    matchedOriginTitles = matchedOriginalTitles
                )
            }

            fun fromItem(query: String, item: SongModule.PublicSongDetail): SearchSongItem {
                val matchTitleRanges = query.toRegex(RegexOption.IGNORE_CASE).findAll(item.title).map { it.range }.toList()
                val matchDescRanges = query.toRegex(RegexOption.IGNORE_CASE).findAll(item.description).map { it.range }.toList()
                val matchSubtitleRanges = query.toRegex(RegexOption.IGNORE_CASE).findAll(item.subtitle).map { it.range }.toList()
                val matchedOriginalTitles = item.originInfos.mapNotNull { it.title }.filter { query.toRegex(RegexOption.IGNORE_CASE).find(it) != null }
                val matchedOriginArtists = item.originInfos.mapNotNull { it.artist }.filter { query.toRegex(RegexOption.IGNORE_CASE).find(it) != null }
                return SearchSongItem(
                    info = SongModule.SearchSongItem(
                        id = item.id,
                        displayId = item.displayId,
                        title = item.title,
                        subtitle = item.subtitle,
                        description = item.description,
                        artist = item.uploaderName,
                        durationSeconds = item.durationSeconds,
                        playCount = item.playCount,
                        likeCount = item.likeCount,
                        coverArtUrl = item.coverUrl,
                        audioUrl = item.audioUrl,
                        uploaderUid = item.uploaderUid,
                        uploaderName = item.uploaderName,
                        explicit = item.explicit,
                        originalTitles = item.originInfos.mapNotNull { it.title },
                        originalArtists = item.originInfos.mapNotNull{ it.artist }
                    ),
                    matchTitleRanges = matchTitleRanges,
                    matchDescRanges = matchDescRanges,
                    matchSubtitleRanges = matchSubtitleRanges,
                    matchedOriginArtists = matchedOriginArtists,
                    matchedOriginTitles = matchedOriginalTitles
                )
            }
        }
    }

    var searchType by mutableStateOf(SearchType.SONG)
        private set
    var query by mutableStateOf("")
        private set
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var loading by mutableStateOf(false)
    var songData by mutableStateOf<List<SearchSongItem>>(emptyList())
        private set
    var userData by mutableStateOf<List<UserModule.PublicUserProfile>>(emptyList())
        private set
    var playlistData by mutableStateOf<List<PlaylistModule.PlaylistMetadata>>(emptyList())
        private set
    var searchProcessingTimeMs by mutableStateOf(0L)
        private set

    var songSortMethod by mutableStateOf<SortMethod>(SortMethod.RELEVANCE)

    fun mounted(query: String, searchType: SearchType) {
        if (this.query != query) {
            this.query = query
            this.searchType = searchType
            search()
        }
    }

    fun dispose() {

    }


    fun search() = viewModelScope.launch {
        when (searchType) {
            SearchType.SONG -> searchSongs()
            SearchType.USER -> searchUsers()
            SearchType.ALBUM -> {}
            SearchType.PLAYLIST -> searchPlaylists()
        }
    }

    fun updateSearchType(type: SearchType) = viewModelScope.launch {
        searchType = type
        search()
    }

    fun updateSortMethod(method: SortMethod) = viewModelScope.launch {
        songSortMethod = method
        search()
    }

    private suspend fun searchSongs() {
        loading = true
        try {
            val result = mutableListOf<SearchSongItem>()
            // If the query text is display id: JM-ABCD-123, use detail to get information.
            // The `JM-` prefix and dash `-` is optional
            val displayIdPattern = "^(?:JM-)?([A-Z]{3,4})-?(\\d{3})$".toRegex()
            val matchResult = displayIdPattern.find(query.trim().uppercase())
            if (matchResult != null) {
                val part1 = matchResult.groupValues[1]
                val part2 = matchResult.groupValues[2]
                val displayId = "JM-$part1-$part2"
                val resp = api.songModule.detail(displayId)
                if (resp.ok) {
                    val data = resp.okData<SongModule.PublicSongDetail>()
                    result.add(SearchSongItem.fromItem(query, data))
                    searchProcessingTimeMs = 0
                }
            }

            val resp = api.songModule.search(
                SongModule.SearchReq(
                    q = query,
                    limit = null,
                    offset = null,
                    filter = null,
                    sortBy = songSortMethod.value
                )
            )
            if (resp.ok) {
                val data = resp.okData<SongModule.SearchResp>()
                result.addAll(data.hits.map { SearchSongItem.fromItem(query, it) })
                searchProcessingTimeMs = data.processingTimeMs
            } else {
                val err = resp.err()
                global.alert(err.msg)
            }

            songData = result
        } catch (e: Throwable) {
            Logger.e("search", "Failed to search", e)
            global.alert(e.message)
        } finally {
            loading = false
        }
    }

    private suspend fun searchUsers() {
        loading = true
        try {
            val result = mutableListOf<UserModule.PublicUserProfile>()
            if (query.toLongOrNull() != null) {
                val resp = api.userModule.profile(query.toLong())
                if (resp.ok) {
                    val data = resp.ok()
                    result.add(data)
                    searchProcessingTimeMs = 0
                } else {
                    val err = resp.err()
                    global.alert(err.msg)
                }
            }

            val resp = api.userModule.search(
                UserModule.SearchReq(
                    q = query,
                    page = 0,
                    size = 20
                )
            )
            if (resp.ok) {
                val data = resp.ok()
                result.addAll(data.hits)
                searchProcessingTimeMs = data.processingTimeMs
            } else {
                val err = resp.err()
                global.alert(err.msg)
            }

            userData = result
        } catch (e: Throwable) {
            Logger.e("search", "Failed to search", e)
            global.alert(e.message)
        } finally {
            loading = false
        }
    }

    private suspend fun searchPlaylists() {
        loading = true
        try {
            val resp = api.playlistModule.search(PlaylistModule.SearchReq(q = query, limit = null, offset = null, sortBy = null, userId = null))
            val data = resp.ok()
            searchProcessingTimeMs = data.processingTimeMs
            playlistData = data.hits
        } catch (e: Throwable) {
            Logger.e("search", "Failed to search", e)
            global.alert(e.message)
        } finally {
            loading = false
        }
    }
}