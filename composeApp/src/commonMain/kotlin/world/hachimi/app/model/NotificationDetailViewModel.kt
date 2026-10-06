package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.module.NotificationModule
import world.hachimi.app.api.module.NotificationModule.NotificationItem
import world.hachimi.app.logging.Logger

@KoinViewModel
class NotificationDetailViewModel(
    @InjectedParam
    val notificationId: String,
    private val store: NotificationStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {

    /** Fetched here when the inbox doesn't have it, e.g. opened from a link. */
    private var fetched by mutableStateOf<NotificationItem?>(null)

    /** The inbox copy is preferred so read state stays in sync. */
    val item: NotificationItem?
        get() = store.find(notificationId) ?: fetched

    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    /** The notification isn't the user's, doesn't exist or has expired. */
    var unavailable by mutableStateOf(false)
        private set
    var markReadFailed by mutableStateOf(false)
        private set
    private var markingRead = false

    fun mounted() {
        if (initializeStatus == InitializeStatus.LOADED) return
        if (store.find(notificationId) != null) {
            initializeStatus = InitializeStatus.LOADED
            markReadIfUnread()
        } else {
            load()
        }
    }

    fun retry() {
        if (initializeStatus == InitializeStatus.FAILED) load()
    }

    private fun load() = viewModelScope.launch {
        initializeStatus = InitializeStatus.INIT
        unavailable = false
        store.fetch(notificationId)
            .onSuccess {
                fetched = it
                initializeStatus = InitializeStatus.LOADED
                markReadIfUnread()
            }
            .onFailure { e ->
                Logger.e(TAG, "Failed to load notification $notificationId", e)
                unavailable = (e as? NotificationException)?.error?.code == NotificationModule.ERR_UNAVAILABLE
                initializeStatus = InitializeStatus.FAILED
            }
    }

    /** Opening a notification marks it read. Failing that doesn't block reading it. */
    fun markReadIfUnread() {
        if (item?.readTime != null || markingRead) return
        markingRead = true
        viewModelScope.launch {
            store.markRead(notificationId)
                .onSuccess { readTime ->
                    fetched = fetched?.copy(readTime = readTime)
                    markReadFailed = false
                }
                .onFailure { e ->
                    Logger.e(TAG, "Failed to mark notification $notificationId read", e)
                    markReadFailed = true
                }
            markingRead = false
        }
    }

    companion object {
        private const val TAG = "notification"
    }
}
