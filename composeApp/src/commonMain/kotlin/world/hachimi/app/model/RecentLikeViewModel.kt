package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import kotlin.time.Duration.Companion.seconds

class RecentLikeViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var loading by mutableStateOf(false)
        private set
    var hasMore by mutableStateOf(true)
        private set
    var songs by mutableStateOf<List<SongGroup>>(emptyList())
        private set
    private var unlikingSongIds by mutableStateOf<Set<Long>>(emptySet())

    private var flatLikes: List<SongModule.MyLikeItem> = emptyList()
    private var nextPageIndex = 0L
    private val pageSize = 20L

    data class SongGroup(
        val date: LocalDate,
        val songs: List<SongModule.MyLikeItem>,
    )

    fun mounted() {
        refresh()
    }

    fun dispose() {
    }

    fun retry() {
        initializeStatus = InitializeStatus.INIT
        refresh()
    }

    fun refresh() {
        fetch(clear = true)
    }

    fun loadMore() {
        fetch(clear = false)
    }

    fun play(item: SongModule.MyLikeItem) {
        global.player.insertToQueue(
            GlobalStore.MusicQueueItem.fromPublicDetail(item.songData),
            true,
            false,
        )
    }

    fun playAllLoaded() {
        viewModelScope.launch {
            val items = flatLikes.map {
                GlobalStore.MusicQueueItem(
                    id = it.songData.id,
                    displayId = it.songData.displayId,
                    name = it.songData.title,
                    artist = it.songData.uploaderName,
                    duration = it.songData.durationSeconds.seconds,
                    coverUrl = it.songData.coverUrl,
                    explicit = it.songData.explicit,
                )
            }
            global.player.playAll(items)
        }
    }

    fun isUnliking(songId: Long): Boolean = songId in unlikingSongIds

    fun unlike(item: SongModule.MyLikeItem) {
        val songId = item.songData.id
        if (songId in unlikingSongIds) {
            return
        }

        viewModelScope.launch {
            unlikingSongIds = unlikingSongIds + songId
            try {
                val resp = api.songModule.unlike(SongModule.UnlikeReq(songId))
                if (resp.ok) {
                    val loadedPageCount = nextPageIndex.coerceAtLeast(1L)
                    flatLikes = flatLikes.filterNot { it.songData.id == songId }
                    rebuildSongs()
                    refreshLoadedPages(loadedPageCount)
                } else {
                    global.alert(resp.err().msg)
                }
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to unlike song $songId", e)
                global.alert(e.message)
            } finally {
                unlikingSongIds = unlikingSongIds - songId
            }
        }
    }

    private fun fetch(clear: Boolean) = viewModelScope.launch {
        if (loading) {
            return@launch
        }
        if (!clear && !hasMore) {
            return@launch
        }

        loading = true
        try {
            val requestPageIndex = if (clear) 0L else nextPageIndex
            val resp = api.songModule.pageMyLikes(
                SongModule.MyLikesReq(
                    pageIndex = requestPageIndex,
                    pageSize = pageSize,
                )
            )
            if (resp.ok) {
                val data = resp.ok()
                val mergedLikes = if (clear) {
                    data.data
                } else {
                    flatLikes + data.data
                }.sortedByDescending { it.likedTime }

                flatLikes = mergedLikes
                nextPageIndex = data.pageIndex + 1
                hasMore = mergedLikes.size < data.total && data.data.isNotEmpty()
                rebuildSongs()

                if (initializeStatus == InitializeStatus.INIT) {
                    initializeStatus = InitializeStatus.LOADED
                }
            } else {
                global.alert(resp.err().msg)
                if (initializeStatus == InitializeStatus.INIT) {
                    initializeStatus = InitializeStatus.FAILED
                }
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to fetch recent likes", e)
            global.alert(e.message)
            if (initializeStatus == InitializeStatus.INIT) {
                initializeStatus = InitializeStatus.FAILED
            }
        } finally {
            loading = false
        }
    }

    private suspend fun refreshLoadedPages(loadedPageCount: Long) {
        loading = true
        try {
            val mergedLikes = mutableListOf<SongModule.MyLikeItem>()
            var latestNextPageIndex = 0L
            var latestHasMore = false
            var refreshSucceeded = true

            for (pageIndex in 0 until loadedPageCount) {
                val resp = api.songModule.pageMyLikes(
                    SongModule.MyLikesReq(
                        pageIndex = pageIndex,
                        pageSize = pageSize,
                    )
                )
                if (!resp.ok) {
                    global.alert(resp.err().msg)
                    refreshSucceeded = false
                    break
                }

                val data = resp.ok()
                mergedLikes += data.data
                latestNextPageIndex = data.pageIndex + 1
                latestHasMore = mergedLikes.size < data.total && data.data.isNotEmpty()

                if (data.data.isEmpty() || data.data.size < pageSize) {
                    break
                }
            }

            if (refreshSucceeded) {
                flatLikes = mergedLikes.sortedByDescending { it.likedTime }
                nextPageIndex = latestNextPageIndex
                hasMore = latestHasMore
                rebuildSongs()
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to refresh recent likes after unlike", e)
            global.alert(e.message)
        } finally {
            loading = false
        }
    }

    private fun rebuildSongs() {
        songs = flatLikes
            .groupBy { it.likedTime.toLocalDateTime(TimeZone.currentSystemDefault()).date }
            .entries
            .sortedByDescending { it.key }
            .map { (date, items) ->
                SongGroup(date, items)
            }
    }

    private companion object {
        const val TAG = "recent-like"
    }
}





