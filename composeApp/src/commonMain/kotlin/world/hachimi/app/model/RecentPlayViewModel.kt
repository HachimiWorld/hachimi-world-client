package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.Instant
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.PlayHistoryModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import kotlin.time.Duration.Companion.seconds

class RecentPlayViewModel(
    private val global: GlobalStore,
    private val api: ApiClient
): ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var loading by mutableStateOf(false)
        private set
    private val _history = mutableStateListOf<PlayHistoryModule.PlayHistoryItem>()
    val history: List<PlayHistoryModule.PlayHistoryItem> = _history
    private var cursor: Instant? = null

    fun mounted() {
        // We call refresh for every branch because the init and refresh logic are the same (for now)
        if (initializeStatus == InitializeStatus.INIT) {
            refresh()
        } else {
            refresh()
        }
    }

    fun dispose() {

    }

    fun retry() {
        initializeStatus = InitializeStatus.INIT
        refresh()
    }

    fun refresh() {
        cursor = null
        loadMore(true)
    }

    fun loadMore(clear: Boolean = false) = viewModelScope.launch {
        loading = true

        try {
            val resp = api.playHistoryModule.cursor(PlayHistoryModule.CursorReq(
                cursor = cursor,
                size = 20
            ))
            if (resp.ok) {
                val data = resp.ok()
                if (data.list.isNotEmpty()) {
                    if (clear) {
                        _history.clear()
                    }
                    _history.addAll(data.list)
                    cursor = data.list.last().playTime
                }
                if (initializeStatus == InitializeStatus.INIT) {
                    initializeStatus = InitializeStatus.LOADED
                }
            } else {
                val err = resp.err()
                global.alert(err.msg)
                if (initializeStatus == InitializeStatus.INIT) {
                    initializeStatus = InitializeStatus.FAILED
                }
                return@launch
            }
        } catch (e: Throwable) {
            Logger.e("recent", "Failed to fetch recent play history", e)
            global.alert(e.message)
            if (initializeStatus == InitializeStatus.INIT) {
                initializeStatus = InitializeStatus.FAILED
            }
            return@launch
        } finally {
            loading = false
        }
    }

    fun play(item: PlayHistoryModule.PlayHistoryItem) {
        global.player.insertToQueue(
            GlobalStore.MusicQueueItem.fromPublicDetail(item.songInfo),
            true,
            false
        )
    }
}
