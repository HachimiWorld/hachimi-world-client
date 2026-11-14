package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@KoinViewModel
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
    /** Likes of songs that were deleted or hidden, shown as unavailable. */
    private var flatUnavailable: List<SongModule.UnavailableLikeItem> = emptyList()
    private var nextPageIndex = 0L
    private val pageSize = 20L

    /**
     * How many songs the user has liked, as the server counts them.
     * @since 261114
     */
    var total by mutableStateOf<Long?>(null)
        private set

    /**
     * Songs liked each day, up to today. Derived from the likes loaded so far, and covers only the
     * days they fully account for.
     * @since 261114
     */
    var rhythm by mutableStateOf<LikeRhythm?>(null)
        private set

    /**
     * Songs liked each day from [firstDay] to today, oldest first.
     * @since 261114
     */
    data class LikeRhythm(val firstDay: LocalDate, val counts: List<Int>) {
        val days: Int get() = counts.size
        val liked: Int get() = counts.sum()
    }

    data class SongGroup(
        val date: LocalDate,
        /** Most recently liked first. */
        val entries: List<LikeEntry>,
    )

    /** A liked song, or one that's no longer available. */
    sealed interface LikeEntry {
        val songId: Long
        val likedTime: Instant

        data class Song(val item: SongModule.MyLikeItem) : LikeEntry {
            override val songId: Long get() = item.songData.id
            override val likedTime: Instant get() = item.likedTime
        }

        data class Unavailable(val item: SongModule.UnavailableLikeItem) : LikeEntry {
            override val songId: Long get() = item.songId
            override val likedTime: Instant get() = item.likedTime
        }
    }

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
        viewModelScope.launch {
            fetch(clear = true).join()
            fillRhythm()
        }
    }

    /** Loads further pages until the last [RhythmDays] days are all counted, within a few pages. */
    private suspend fun fillRhythm() {
        var pages = 0
        while (hasMore && pages < RhythmMaxPages && initializeStatus == InitializeStatus.LOADED) {
            val oldest = oldestLoadedDate() ?: break
            if (oldest < rhythmStart()) break
            val before = nextPageIndex
            fetch(clear = false).join()
            if (nextPageIndex == before) break
            pages++
        }
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

    fun unlike(songId: Long) {
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
                    flatUnavailable = flatUnavailable.filterNot { it.songId == songId }
                    rebuildSongs()
                    refreshLoadedPages(loadedPageCount)
                } else {
                    global.alert(resp.err().msg)
                }
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
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
                val mergedUnavailable = if (clear) data.unavailable else flatUnavailable + data.unavailable

                flatLikes = mergedLikes
                flatUnavailable = mergedUnavailable
                nextPageIndex = data.pageIndex + 1
                total = data.total
                val pageCount = data.data.size + data.unavailable.size
                hasMore = mergedLikes.size + mergedUnavailable.size < data.total && pageCount > 0
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
            if (e is CancellationException) throw e
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
            val mergedUnavailable = mutableListOf<SongModule.UnavailableLikeItem>()
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
                mergedUnavailable += data.unavailable
                latestNextPageIndex = data.pageIndex + 1
                total = data.total
                val pageCount = data.data.size + data.unavailable.size
                latestHasMore = mergedLikes.size + mergedUnavailable.size < data.total && pageCount > 0

                if (pageCount < pageSize) {
                    break
                }
            }

            if (refreshSucceeded) {
                flatLikes = mergedLikes.sortedByDescending { it.likedTime }
                flatUnavailable = mergedUnavailable
                nextPageIndex = latestNextPageIndex
                hasMore = latestHasMore
                rebuildSongs()
            }
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            Logger.e(TAG, "Failed to refresh recent likes after unlike", e)
            global.alert(e.message)
        } finally {
            loading = false
        }
    }

    private fun rebuildSongs() {
        val entries: List<LikeEntry> = flatLikes.map { LikeEntry.Song(it) } + flatUnavailable.map { LikeEntry.Unavailable(it) }
        songs = entries
            .sortedByDescending { it.likedTime }
            .groupBy { it.likedTime.toLocalDateTime(TimeZone.currentSystemDefault()).date }
            .entries
            .sortedByDescending { it.key }
            .map { (date, items) ->
                SongGroup(date, items)
            }
        rhythm = countRhythm()
    }

    private fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private fun rhythmStart(): LocalDate = today().minus(RhythmDays - 1, DateTimeUnit.DAY)

    private fun oldestLoadedDate(): LocalDate? = songs.lastOrNull()?.date

    private fun countRhythm(): LikeRhythm? {
        val today = today()
        val start = rhythmStart()
        val oldest = oldestLoadedDate()
        // With more to load, the oldest loaded day may be missing likes from the next page
        val firstDay = when {
            !hasMore || oldest == null -> start
            oldest < start -> start
            else -> oldest.plus(1, DateTimeUnit.DAY)
        }
        if (firstDay > today) return null
        val byDay = songs.associate { it.date to it.entries.size }
        val days = (today.toEpochDays() - firstDay.toEpochDays()).toInt() + 1
        return LikeRhythm(firstDay, List(days) { byDay[firstDay.plus(it, DateTimeUnit.DAY)] ?: 0 })
    }

    private companion object {
        const val TAG = "recent-like"
        const val RhythmDays = 30
        const val RhythmMaxPages = 8
    }
}





