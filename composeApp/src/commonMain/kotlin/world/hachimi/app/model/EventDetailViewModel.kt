package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.PostModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

private const val TAG = "event_detail"

class EventDetailViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {

    var initStat by mutableStateOf(InitializeStatus.INIT)
        private set

    var eventId by mutableStateOf<Long?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    var data by mutableStateOf<PostModule.PostDetail?>(null)
        private set

    fun mounted(eventId: Long) {
        if (this.eventId != eventId) {
            initStat = InitializeStatus.INIT
        }
        this.eventId = eventId
        refresh()
    }

    fun dispose() {
        // no-op
    }

    fun retry() {
        initStat = InitializeStatus.INIT
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        val id = eventId ?: return@launch
        loading = true
        try {
            val resp = api.postModule.detail(PostModule.PostIdReq(postId = id))
            if (resp.ok) {
                data = resp.ok()
                initStat = InitializeStatus.LOADED
            } else {
                global.alert(resp.err().msg)
                initStat = InitializeStatus.FAILED
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to load event detail", e)
            global.alert(e.message)
            initStat = InitializeStatus.FAILED
        } finally {
            loading = false
        }
    }
}
