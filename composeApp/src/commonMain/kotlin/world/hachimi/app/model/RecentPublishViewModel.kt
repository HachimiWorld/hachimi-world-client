package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class RecentPublishViewModel(
    private val apiClient: ApiClient,
    private val global: GlobalStore
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var loading by mutableStateOf(false)
        private set
    var songs by mutableStateOf<List<SongGroup>>(emptyList())
        private set
    var hasMore by mutableStateOf(true)
        private set

    private var flatSongs: List<SongModule.PublicSongDetail> = emptyList()
    private var cursor: Instant? = null

    data class SongGroup(
        val date: LocalDate,
        val songs: List<SongModule.PublicSongDetail>
    )

    fun mounted() {
        if (initializeStatus == InitializeStatus.INIT) {
            getRecommendSongs()
        }
    }

    fun unmount() {

    }

    fun retry() {
        initializeStatus = InitializeStatus.INIT
        getRecommendSongs()
    }

    private fun getRecommendSongs() = viewModelScope.launch(Dispatchers.Default) {
        if (!hasMore) return@launch
        
        loading = true
        try {
            val resp =
                apiClient.songModule.recentV2(SongModule.RecentReq(cursor, 50, false))
            if (resp.ok) {
                val data = resp.ok()
                flatSongs = flatSongs + data.songs
                val groupByDate = flatSongs.groupBy {
                    it.createTime.toLocalDateTime(TimeZone.currentSystemDefault()).date
                }.entries.sortedByDescending {
                    it.key
                }.map { (date, songs) ->
                    SongGroup(date, songs)
                }
                hasMore = data.songs.isNotEmpty()
                val last = data.songs.lastOrNull()
                last?.let {
                    cursor = it.createTime
                }

                this@RecentPublishViewModel.songs = groupByDate
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
            Logger.e("home", "Failed to get recommend songs", e)
            global.alert("获取推荐音乐失败：${e.message}")
            if (initializeStatus == InitializeStatus.INIT) {
                initializeStatus = InitializeStatus.FAILED
            }
        } finally {
            loading = false
        }
    }

    /**
     * Set cursor to null and refresh
     */
    private suspend fun refresh() {
        try {
            refreshing = true
            cursor = null
            flatSongs = emptyList()
            hasMore = true
            getRecommendSongs().join()
        } finally {
            refreshing = false
        }
    }

    private var lastRefreshTime = Clock.System.now() - 20.minutes

    fun fakeRefresh() {
        // This is used to treat obsessive-compulsive disorder (OCD).
        val now = Clock.System.now()
        if (now - lastRefreshTime > 10.minutes) {
            viewModelScope.launch {
                refresh()
                lastRefreshTime = now
            }
        } else {
            viewModelScope.launch {
                loading = true
                delay(Random.nextLong(1000, 5000))
                loading = false
            }
        }
    }


    fun loadMore() {
        getRecommendSongs()
    }

    fun playAllRecent() {
        viewModelScope.launch {
            val items = songs.flatMap {
                it.songs
            }.map { GlobalStore.MusicQueueItem.fromPublicDetail(it) }
            global.player.playAll(items)
        }
    }
}