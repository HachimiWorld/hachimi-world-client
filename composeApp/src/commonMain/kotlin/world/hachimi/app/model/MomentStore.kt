package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.moment_delete_failed
import hachimiworld.composeapp.generated.resources.moment_deleted
import hachimiworld.composeapp.generated.resources.moment_like_failed
import hachimiworld.composeapp.generated.resources.need_login_message
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.MomentModule
import world.hachimi.app.api.module.MomentModule.MomentItem
import world.hachimi.app.logging.Logger

/** A change to a moment, for every list showing it. @since 261106 */
sealed interface MomentChange {
    data class Updated(val item: MomentItem) : MomentChange
    data class Deleted(val id: Long) : MomentChange
    /** The user just posted it. */
    data class Posted(val item: MomentItem) : MomentChange
}

/**
 * What the user does to moments, wherever they're shown: liking and deleting. Each change goes out
 * on [changes], so the square, a user's page and the detail page all show it.
 *
 * @since 261106
 */
@Singleton
class MomentStore(
    private val api: ApiClient,
    private val global: GlobalStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _changes = MutableSharedFlow<MomentChange>(extraBufferCapacity = 16)
    val changes: SharedFlow<MomentChange> = _changes

    fun isMine(item: MomentItem): Boolean = global.userInfo?.uid == item.author.uid

    /** Shows the change right away, and takes it back if the server says no. */
    fun toggleLike(item: MomentItem) {
        if (!global.isLoggedIn) {
            global.alert(Res.string.need_login_message)
            return
        }
        val liked = !item.liked
        val changed = item.copy(liked = liked, likeCount = (item.likeCount + if (liked) 1 else -1).coerceAtLeast(0))
        _changes.tryEmit(MomentChange.Updated(changed))
        scope.launch {
            val req = MomentModule.MomentIdReq(item.id)
            val ok = try {
                val resp = if (liked) api.momentModule.like(req) else api.momentModule.unlike(req)
                if (!resp.ok) global.alert(resp.err().msg)
                resp.ok
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to like a moment", e)
                global.alert(Res.string.moment_like_failed)
                false
            }
            if (!ok) _changes.tryEmit(MomentChange.Updated(item))
        }
    }

    fun delete(item: MomentItem, onDeleted: () -> Unit = {}) {
        scope.launch {
            try {
                val resp = api.momentModule.delete(MomentModule.MomentIdReq(item.id))
                if (resp.ok) {
                    _changes.tryEmit(MomentChange.Deleted(item.id))
                    global.alert(Res.string.moment_deleted)
                    onDeleted()
                } else {
                    global.alert(resp.err().msg)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to delete a moment", e)
                global.alert(Res.string.moment_delete_failed)
            }
        }
    }

    /** Called once the user's new moment is published. */
    fun posted(item: MomentItem) {
        _changes.tryEmit(MomentChange.Posted(item))
    }

    /** Called when a moment loaded fresh, so lists showing an older copy catch up. */
    fun refreshed(item: MomentItem) {
        _changes.tryEmit(MomentChange.Updated(item))
    }

    companion object {
        private const val TAG = "moments"
    }
}

/** One page of moments, and where the next starts. */
data class MomentPage(val items: List<MomentItem>, val nextCursor: String?)

/** Thrown by a [MomentPager]'s loader when the server answers with an error. */
class MomentLoadException(message: String) : Exception(message)

/**
 * A list of moments loaded page by page with a cursor, kept in step with [MomentStore.changes].
 *
 * @param changes Usually [MomentStore.changes].
 * @param load Loads the page after `cursor`, the first page for null. Throws [MomentLoadException]
 * when the server says no.
 * @param acceptsPosted Whether a moment the user just posted belongs at the top of this list.
 * @since 261106
 */
class MomentPager(
    private val scope: CoroutineScope,
    changes: Flow<MomentChange>,
    /** Tells the user that loading failed while a list is shown. */
    private val alert: (String?) -> Unit,
    private val acceptsPosted: (MomentItem) -> Boolean = { false },
    private val load: suspend (cursor: String?) -> MomentPage,
) {
    constructor(
        scope: CoroutineScope,
        global: GlobalStore,
        store: MomentStore,
        acceptsPosted: (MomentItem) -> Boolean = { false },
        load: suspend (cursor: String?) -> MomentPage,
    ) : this(scope, store.changes, { global.alert(it) }, acceptsPosted, load)

    val items = mutableStateListOf<MomentItem>()
    var status by mutableStateOf(InitializeStatus.INIT)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var loadingMore by mutableStateOf(false)
        private set
    var loadMoreFailed by mutableStateOf(false)
        private set
    var hasMore by mutableStateOf(false)
        private set
    private var cursor: String? = null
    private var job: Job? = null

    init {
        scope.launch {
            changes.collect { change ->
                when (change) {
                    is MomentChange.Updated -> {
                        val index = items.indexOfFirst { it.id == change.item.id }
                        if (index >= 0) items[index] = change.item
                    }
                    is MomentChange.Deleted -> items.removeAll { it.id == change.id }
                    is MomentChange.Posted -> if (acceptsPosted(change.item) && items.none { it.id == change.item.id }) {
                        items.add(0, change.item)
                    }
                }
            }
        }
    }

    /** Loads the first page if nothing's loaded yet. */
    fun ensureLoaded() {
        if (status != InitializeStatus.LOADED && job?.isActive != true) refresh()
    }

    /** Loads the first page again, keeping what's shown until it arrives. */
    fun refresh() {
        job?.cancel()
        job = scope.launch {
            refreshing = true
            try {
                val page = load(null)
                items.clear()
                items.addAll(page.items)
                cursor = page.nextCursor
                hasMore = page.nextCursor != null
                loadMoreFailed = false
                status = InitializeStatus.LOADED
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load moments", e)
                if (status == InitializeStatus.LOADED) alert(e.message) else status = InitializeStatus.FAILED
            } finally {
                refreshing = false
                loadingMore = false
            }
        }
    }

    fun retry() {
        status = InitializeStatus.INIT
        refresh()
    }

    fun loadMore() {
        val after = cursor ?: return
        if (status != InitializeStatus.LOADED || loadingMore || refreshing || !hasMore) return
        job = scope.launch {
            loadingMore = true
            loadMoreFailed = false
            try {
                val page = load(after)
                val known = items.mapTo(HashSet()) { it.id }
                items.addAll(page.items.filter { it.id !in known })
                cursor = page.nextCursor
                hasMore = page.nextCursor != null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load more moments", e)
                loadMoreFailed = true
            } finally {
                loadingMore = false
            }
        }
    }

    /** Empties the list, for when what it lists changes. */
    fun reset() {
        job?.cancel()
        items.clear()
        cursor = null
        hasMore = false
        loadMoreFailed = false
        loadingMore = false
        refreshing = false
        status = InitializeStatus.INIT
    }

    companion object {
        private const val TAG = "moments"
    }
}
