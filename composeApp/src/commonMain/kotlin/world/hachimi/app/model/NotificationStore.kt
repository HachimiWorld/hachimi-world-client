package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.err
import world.hachimi.app.api.module.NotificationModule
import world.hachimi.app.api.module.NotificationModule.NotificationItem
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * System notifications of the logged-in account, shared by the entry badge, the inbox and the
 * details. Kept in memory only and cleared when the account changes.
 */
@Singleton
class NotificationStore(
    private val api: ApiClient,
    private val global: GlobalStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Null until loaded for the current account. Keeps the last known value if a refresh fails. */
    var unreadCount by mutableStateOf<Long?>(null)
        private set

    /** Newest first. */
    val items = mutableStateListOf<NotificationItem>()
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var refreshing by mutableStateOf(false)
        private set
    /** A refresh failed while older items are still shown. */
    var refreshFailed by mutableStateOf(false)
        private set
    var hasMore by mutableStateOf(false)
        private set
    var loadingMore by mutableStateOf(false)
        private set
    var loadMoreFailed by mutableStateOf(false)
        private set
    var markingAllRead by mutableStateOf(false)
        private set

    /** Bumped on account change, so responses for the previous account are dropped. */
    private var generation = 0
    private var listJob: Job? = null

    init {
        scope.launch {
            snapshotFlow { global.userInfo?.uid }
                .distinctUntilChanged()
                .collect { uid ->
                    reset()
                    if (uid != null) refreshUnreadCount()
                }
        }
    }

    private fun reset() {
        generation++
        listJob?.cancel()
        listJob = null
        unreadCount = null
        items.clear()
        initializeStatus = InitializeStatus.INIT
        refreshing = false
        refreshFailed = false
        hasMore = false
        loadingMore = false
        loadMoreFailed = false
        markingAllRead = false
    }

    fun refreshUnreadCount() {
        if (!global.isLoggedIn) return
        val gen = generation
        scope.launch {
            try {
                val resp = api.notificationModule.unreadCount()
                if (gen == generation && resp.ok) {
                    unreadCount = resp.ok().unreadCount
                }
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to refresh unread count", e)
            }
        }
    }

    /**
     * Reloads the first page and merges it into the loaded items, so pages loaded further down
     * stay. Keeps the shown items if it fails.
     */
    fun refresh() {
        if (!global.isLoggedIn) return
        val gen = generation
        listJob?.cancel()
        listJob = scope.launch {
            refreshing = true
            loadingMore = false
            try {
                val (page, more) = fetchPage(null)
                if (gen != generation) return@launch
                // Ids are UUIDv7 strings of the same format, so they compare in time order.
                val oldestInPage = page.lastOrNull()?.notificationId
                val olderLoaded = if (more && oldestInPage != null) {
                    items.filter { it.notificationId < oldestInPage }
                } else {
                    emptyList()
                }
                // One snapshot, so the list never shows up empty in between.
                Snapshot.withMutableSnapshot {
                    items.clear()
                    items.addAll(page)
                    items.addAll(olderLoaded)
                    if (olderLoaded.isEmpty()) hasMore = more
                    loadMoreFailed = false
                    refreshFailed = false
                    initializeStatus = InitializeStatus.LOADED
                    refreshing = false
                }
            } catch (e: CancellationException) {
                // Replaced by a newer refresh, or the account changed
                throw e
            } catch (e: Throwable) {
                if (gen != generation) return@launch
                Logger.e(TAG, "Failed to refresh notifications", e)
                if (initializeStatus == InitializeStatus.LOADED) {
                    refreshFailed = true
                } else {
                    initializeStatus = InitializeStatus.FAILED
                }
                refreshing = false
            }
        }
        refreshUnreadCount()
    }

    fun loadMore() {
        if (initializeStatus != InitializeStatus.LOADED || !hasMore || loadingMore || refreshing) return
        val beforeId = items.lastOrNull()?.notificationId ?: return
        val gen = generation
        listJob = scope.launch {
            loadingMore = true
            loadMoreFailed = false
            try {
                val (page, more) = fetchPage(beforeId)
                if (gen != generation) return@launch
                val known = items.mapTo(HashSet()) { it.notificationId }
                items.addAll(page.filter { it.notificationId !in known })
                hasMore = more
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                if (gen != generation) return@launch
                Logger.e(TAG, "Failed to load more notifications", e)
                loadMoreFailed = true
            } finally {
                if (gen == generation) loadingMore = false
            }
        }
    }

    private suspend fun fetchPage(beforeId: String?): Pair<List<NotificationItem>, Boolean> {
        val resp = api.notificationModule.list(NotificationModule.ListReq(beforeId = beforeId, limit = PAGE_SIZE))
        if (!resp.ok) error(resp.err().msg)
        val data = resp.ok()
        return data.items to data.hasMore
    }

    fun find(notificationId: String): NotificationItem? =
        items.firstOrNull { it.notificationId == notificationId }

    /** Fetches one notification, e.g. when opened from a link before the inbox is loaded. */
    suspend fun fetch(notificationId: String): Result<NotificationItem> = runCatching {
        val resp = api.notificationModule.detail(NotificationModule.DetailReq(notificationId))
        if (!resp.ok) throw NotificationException(resp.err())
        resp.ok<NotificationItem>().also(::replace)
    }

    /** Marks it read and updates the inbox and badge with the server's values. */
    suspend fun markRead(notificationId: String): Result<Instant> = runCatching {
        val gen = generation
        val resp = api.notificationModule.markRead(NotificationModule.MarkReadReq(notificationId))
        if (!resp.ok) throw NotificationException(resp.err())
        val data = resp.ok()
        if (gen == generation) {
            updateReadTime(notificationId, data.readTime)
            unreadCount = data.unreadCount
        }
        data.readTime
    }

    fun markAllRead() {
        if (markingAllRead) return
        val gen = generation
        scope.launch {
            markingAllRead = true
            try {
                val resp = api.notificationModule.markAllRead()
                if (gen != generation) return@launch
                if (resp.ok) {
                    unreadCount = resp.ok().unreadCount
                    // Only shows the read state, so the local time is close enough.
                    val now = Clock.System.now()
                    Snapshot.withMutableSnapshot {
                        for (i in items.indices) {
                            if (items[i].readTime == null) items[i] = items[i].copy(readTime = now)
                        }
                    }
                } else {
                    global.alert(resp.err().msg)
                }
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to mark all notifications read", e)
                if (gen == generation) global.alert(e.message)
            } finally {
                if (gen == generation) markingAllRead = false
            }
        }
    }

    private fun replace(item: NotificationItem) {
        val index = items.indexOfFirst { it.notificationId == item.notificationId }
        if (index >= 0) items[index] = item
    }

    private fun updateReadTime(notificationId: String, readTime: Instant) {
        val index = items.indexOfFirst { it.notificationId == notificationId }
        if (index >= 0 && items[index].readTime == null) {
            items[index] = items[index].copy(readTime = readTime)
        }
    }

    companion object {
        private const val TAG = "notification"
        private const val PAGE_SIZE = 20
    }
}

class NotificationException(val error: CommonError) : Exception(error.msg)
