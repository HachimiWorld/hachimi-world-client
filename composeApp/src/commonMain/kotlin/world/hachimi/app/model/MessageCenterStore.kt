package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.MessageModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import kotlin.time.Instant

/**
 * Unread counts of every kind of message for the entry badge and the message center, and the
 * read times of the kinds read as a whole (received likes, new followers, replies). System notifications
 * keep their own count in [NotificationStore], which this fills from the summary.
 */
@Singleton
class MessageCenterStore(
    private val api: ApiClient,
    private val global: GlobalStore,
    private val notifications: NotificationStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var likeUnread by mutableStateOf<Long?>(null)
        private set
    var followUnread by mutableStateOf<Long?>(null)
        private set
    var replyUnread by mutableStateOf<Long?>(null)
        private set
    private var likeReadTime: Instant? = null
    private var followReadTime: Instant? = null
    private var replyReadTime: Instant? = null

    /** Null until the summary has loaded for the current account. */
    val totalUnread: Long?
        get() {
            val system = notifications.unreadCount ?: return null
            val like = likeUnread ?: return null
            val follow = followUnread ?: return null
            val reply = replyUnread ?: return null
            return system + like + follow + reply
        }

    /** Bumped on account change, so responses for the previous account are dropped. */
    private var generation = 0

    init {
        scope.launch {
            snapshotFlow { global.userInfo?.uid }
                .distinctUntilChanged()
                .collect { uid ->
                    reset()
                    if (uid != null) refreshSummary()
                }
        }
    }

    private fun reset() {
        generation++
        likeUnread = null
        followUnread = null
        replyUnread = null
        likeReadTime = null
        followReadTime = null
        replyReadTime = null
    }

    fun refreshSummary() {
        if (!global.isLoggedIn) return
        scope.launch {
            try {
                loadSummary()
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to refresh message summary", e)
            }
        }
    }

    private suspend fun loadSummary() {
        val gen = generation
        val resp = api.messageModule.summary()
        if (!resp.ok) error(resp.err().msg)
        val data = resp.ok()
        if (gen != generation) return
        Snapshot.withMutableSnapshot {
            likeUnread = data.likeUnread
            followUnread = data.followUnread
            likeReadTime = data.likeReadTime
            followReadTime = data.followReadTime
            replyUnread = data.replyUnread
            replyReadTime = data.replyReadTime
            notifications.updateUnreadCount(data.systemUnread)
        }
    }

    /**
     * Called when the user opens received likes, new followers or replies: marks everything up to now
     * read and returns the read time from before, so the page can tell which items are new.
     * Returns null if the previous read time couldn't be loaded.
     */
    suspend fun openChannel(channel: String): Instant? {
        val gen = generation
        if (readTimeOf(channel) == null) {
            try {
                loadSummary()
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load message summary", e)
            }
        }
        val previous = readTimeOf(channel)
        try {
            val resp = api.messageModule.markRead(MessageModule.MarkReadReq(channel))
            if (resp.ok && gen == generation) {
                val readTime = resp.ok().readTime
                Snapshot.withMutableSnapshot {
                    when (channel) {
                        MessageModule.CHANNEL_LIKE -> {
                            likeReadTime = readTime
                            likeUnread = 0
                        }
                        MessageModule.CHANNEL_FOLLOW -> {
                            followReadTime = readTime
                            followUnread = 0
                        }
                        MessageModule.CHANNEL_REPLY -> {
                            replyReadTime = readTime
                            replyUnread = 0
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to mark $channel read", e)
        }
        return previous
    }

    private fun readTimeOf(channel: String): Instant? = when (channel) {
        MessageModule.CHANNEL_LIKE -> likeReadTime
        MessageModule.CHANNEL_FOLLOW -> followReadTime
        MessageModule.CHANNEL_REPLY -> replyReadTime
        else -> null
    }

    companion object {
        private const val TAG = "message"
    }
}
