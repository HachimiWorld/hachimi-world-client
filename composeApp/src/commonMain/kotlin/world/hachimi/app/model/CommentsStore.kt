package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.comment_delete_failed
import hachimiworld.composeapp.generated.resources.comment_like_failed
import hachimiworld.composeapp.generated.resources.comment_send_failed
import hachimiworld.composeapp.generated.resources.comment_sent_pending
import hachimiworld.composeapp.generated.resources.need_login_message
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.koin.core.annotation.Singleton
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.err
import world.hachimi.app.api.module.CommentModule
import world.hachimi.app.api.module.CommentModule.CommentItem
import world.hachimi.app.api.module.CommentModule.CommentThread
import world.hachimi.app.api.module.UgcPreview
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import world.hachimi.app.nav.Route

/**
 * What comments are on.
 *
 * @since 261031
 */
data class CommentTarget(
    /** [CommentModule.TARGET_SONG] or [CommentModule.TARGET_POST] */
    val type: String,
    val id: Long,
    /** Can delete any comment under it. */
    val ownerUid: Long,
)

/**
 * The comments of one song or post, for the page showing them. The page names the target with
 * [open]; the comments and what the user is typing stay until another target is opened.
 */
@Singleton
class CommentsStore(
    private val api: ApiClient,
    private val global: GlobalStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** @since 261031 */
    var target by mutableStateOf<CommentTarget?>(null)
        private set

    /** [CommentModule.ORDER_NEWEST] or [CommentModule.ORDER_OLDEST], for [threads]. */
    var order by mutableStateOf(CommentModule.ORDER_NEWEST)
        private set
    /** The most liked comments, shown above [threads]; each is in [threads] too. */
    val hotThreads = mutableStateListOf<CommentThread>()
    /** All comments by time, in [order]. */
    val threads = mutableStateListOf<CommentThread>()
    /** Published comments and replies. */
    var total by mutableStateOf(0L)
        private set
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var hasMore by mutableStateOf(false)
        private set
    var loadingMore by mutableStateOf(false)
        private set
    var loadMoreFailed by mutableStateOf(false)
        private set
    private var loadJob: Job? = null

    /** The thread whose replies are open, or null for the comment list. */
    var openThread by mutableStateOf<CommentThread?>(null)
        private set
    val replies = mutableStateListOf<CommentItem>()
    var repliesTotal by mutableStateOf(0L)
        private set
    var repliesStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var repliesHasMore by mutableStateOf(false)
        private set
    var repliesLoadingMore by mutableStateOf(false)
        private set
    private var repliesPageIndex = 0
    private var repliesJob: Job? = null

    var input by mutableStateOf("")
    /** The comment being replied to, or null to comment on the target. */
    var replyTo by mutableStateOf<CommentItem?>(null)
        private set
    var sending by mutableStateOf(false)
        private set
    private val _sent = MutableSharedFlow<CommentItem>(extraBufferCapacity = 1)
    /** Each comment the user just sent, once it's in the lists, so the page can bring it into view. @since 261031 */
    val sent: SharedFlow<CommentItem> = _sent

    /** Shows the comments of the target, loading them if it's a different one. @since 261031 */
    fun open(target: CommentTarget) {
        if (this.target == target && initializeStatus != InitializeStatus.FAILED) return
        if (this.target != target) {
            input = ""
            replyTo = null
            closeThread()
        }
        this.target = target
        load()
    }

    fun changeOrder(order: String) {
        if (this.order == order) return
        this.order = order
        // The list stays until the new order arrives, so it doesn't collapse under the user
        if (target != null) load(keepList = initializeStatus == InitializeStatus.LOADED)
    }

    fun retry() {
        if (target != null) load()
    }

    private fun load(keepList: Boolean = false) {
        val target = target ?: return
        loadJob?.cancel()
        hasMore = false
        loadMoreFailed = false
        loadingMore = false
        if (!keepList) {
            hotThreads.clear()
            threads.clear()
            total = 0
            initializeStatus = InitializeStatus.INIT
        }
        loadJob = scope.launch {
            try {
                // Both at once; a failure of either fails the load, rethrown here
                val (hot, data) = coroutineScope {
                    val hot = async { fetchHot(target) }
                    val data = fetch(target, after = null)
                    hot.await() to data
                }
                // All at once, or the list shows empty for a frame when it's replaced
                Snapshot.withMutableSnapshot {
                    hotThreads.clear()
                    hotThreads.addAll(hot.threads)
                    threads.clear()
                    threads.addAll(data.threads)
                    total = data.total
                    hasMore = data.hasMore
                    initializeStatus = InitializeStatus.LOADED
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load comments", e)
                initializeStatus = InitializeStatus.FAILED
            }
        }
    }

    fun loadMore() {
        val target = target ?: return
        if (initializeStatus != InitializeStatus.LOADED || !hasMore || loadingMore) return
        loadingMore = true
        loadMoreFailed = false
        loadJob = scope.launch {
            try {
                val data = fetch(target, after = threads.lastOrNull()?.comment)
                threads.addAll(data.threads)
                total = data.total
                hasMore = data.hasMore
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load more comments", e)
                loadMoreFailed = true
            } finally {
                loadingMore = false
            }
        }
    }

    /** A page of [threads], after the thread [after]. */
    private suspend fun fetch(target: CommentTarget, after: CommentItem?): CommentModule.ListResp {
        val resp = api.commentModule.list(
            CommentModule.ListReq(target.type, target.id, order, after?.createTime, after?.id, PAGE_SIZE),
            auth = global.isLoggedIn,
        )
        if (!resp.ok) error(resp.err().msg)
        return resp.ok()
    }

    private suspend fun fetchHot(target: CommentTarget): CommentModule.HotResp {
        val resp = api.commentModule.hot(CommentModule.HotReq(target.type, target.id), auth = global.isLoggedIn)
        if (!resp.ok) error(resp.err().msg)
        return resp.ok()
    }

    fun openReplies(thread: CommentThread) {
        openThread = thread
        loadReplies(thread.comment.id)
    }

    fun closeThread() {
        val rootId = openThread?.comment?.id
        repliesJob?.cancel()
        // The replies stay for the view animating out; opening a thread resets them
        openThread = null
        if (rootId != null && (replyTo?.id == rootId || replyTo?.rootId == rootId)) replyTo = null
    }

    fun retryReplies() {
        openThread?.let { loadReplies(it.comment.id) }
    }

    private fun loadReplies(rootId: Long) {
        repliesJob?.cancel()
        replies.clear()
        repliesHasMore = false
        repliesLoadingMore = false
        repliesStatus = InitializeStatus.INIT
        repliesJob = scope.launch {
            try {
                val data = fetchReplies(rootId, 0)
                replies.addAll(data.replies)
                repliesTotal = data.total
                repliesPageIndex = 0
                repliesHasMore = data.replies.size >= PAGE_SIZE
                repliesStatus = InitializeStatus.LOADED
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load replies", e)
                repliesStatus = InitializeStatus.FAILED
            }
        }
    }

    fun loadMoreReplies() {
        val rootId = openThread?.comment?.id ?: return
        if (repliesStatus != InitializeStatus.LOADED || !repliesHasMore || repliesLoadingMore) return
        repliesLoadingMore = true
        repliesJob = scope.launch {
            try {
                val data = fetchReplies(rootId, repliesPageIndex + 1)
                val known = replies.mapTo(HashSet()) { it.id }
                replies.addAll(data.replies.filter { it.id !in known })
                repliesTotal = data.total
                repliesPageIndex++
                repliesHasMore = data.replies.size >= PAGE_SIZE
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load more replies", e)
            } finally {
                repliesLoadingMore = false
            }
        }
    }

    private suspend fun fetchReplies(rootId: Long, page: Int): CommentModule.RepliesResp {
        val resp = api.commentModule.replies(CommentModule.RepliesReq(rootId, page, PAGE_SIZE), auth = global.isLoggedIn)
        if (!resp.ok) error(resp.err().msg)
        return resp.ok()
    }

    fun startReply(item: CommentItem) {
        if (!requireLogin()) return
        replyTo = item
    }

    fun cancelReply() {
        replyTo = null
    }

    fun send() {
        val target = target ?: return
        val content = input.trim()
        if (content.isEmpty() || sending || !requireLogin()) return
        val replyTo = replyTo
        sending = true
        scope.launch {
            try {
                val resp = api.commentModule.create(
                    CommentModule.CreateReq(target.type, target.id, content, replyTo?.id)
                )
                if (!resp.ok) {
                    alertError(resp.err(), Res.string.comment_send_failed)
                    return@launch
                }
                val item = resp.ok().comment
                if (this@CommentsStore.target != target) return@launch
                input = ""
                this@CommentsStore.replyTo = null
                if (item != null) {
                    added(item)
                    _sent.tryEmit(item)
                    global.events.send(AppEvent.CommentPosted)
                } else {
                    global.alert(Res.string.comment_sent_pending)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to send comment", e)
                global.alert(Res.string.comment_send_failed)
            } finally {
                sending = false
            }
        }
    }

    /** Puts a comment the user just sent where they'll see it. */
    private fun added(item: CommentItem) {
        val rootId = item.rootId
        total++
        if (rootId == null) {
            val thread = CommentThread(item, emptyList(), 0)
            // Oldest first, it belongs at the end, shown once the pages before it are loaded
            if (order == CommentModule.ORDER_NEWEST) threads.add(0, thread)
            else if (!hasMore) threads.add(thread)
            return
        }
        updateThread(rootId) {
            it.copy(
                replies = if (it.replies.size < FIRST_REPLIES) it.replies + item else it.replies,
                replyCount = it.replyCount + 1,
            )
        }
        if (openThread?.comment?.id == rootId) {
            if (!repliesHasMore) replies.add(item)
            repliesTotal++
        }
    }

    fun toggleLike(item: CommentItem) {
        if (item.removed || !requireLogin()) return
        val liked = !item.liked
        update(item.id) { it.copy(liked = liked, likeCount = (it.likeCount + if (liked) 1 else -1).coerceAtLeast(0)) }
        scope.launch {
            try {
                val req = CommentModule.CommentIdReq(item.id)
                val resp = if (liked) api.commentModule.like(req) else api.commentModule.unlike(req)
                if (!resp.ok) {
                    update(item.id) { it.copy(liked = item.liked, likeCount = item.likeCount) }
                    alertError(resp.err(), Res.string.comment_like_failed)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to like comment", e)
                update(item.id) { it.copy(liked = item.liked, likeCount = item.likeCount) }
                global.alert(Res.string.comment_like_failed)
            }
        }
    }

    fun isMine(item: CommentItem): Boolean =
        item.author != null && item.author.uid == global.userInfo?.uid

    /** The author can delete their comment, and the owner any comment under what they own. */
    fun canDelete(item: CommentItem): Boolean {
        val uid = global.userInfo?.uid ?: return false
        return !item.removed && (item.author?.uid == uid || target?.ownerUid == uid)
    }

    fun delete(item: CommentItem) {
        scope.launch {
            try {
                val resp = api.commentModule.delete(CommentModule.CommentIdReq(item.id))
                if (!resp.ok) {
                    alertError(resp.err(), Res.string.comment_delete_failed)
                    return@launch
                }
                removed(item)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to delete comment", e)
                global.alert(Res.string.comment_delete_failed)
            }
        }
    }

    /** Takes a deleted comment out of the list; a top-level one with replies stays as removed. */
    private fun removed(item: CommentItem) {
        total = (total - 1).coerceAtLeast(0)
        if (replyTo?.id == item.id) replyTo = null
        val rootId = item.rootId
        if (rootId == null) {
            // Not hot anymore; in the list, one with replies stays as removed
            hotThreads.removeAll { it.comment.id == item.id }
            val index = threads.indexOfFirst { it.comment.id == item.id }
            if (index >= 0) {
                val thread = threads[index]
                if (thread.replyCount > 0) threads[index] = thread.copy(comment = item.asRemoved())
                else threads.removeAt(index)
            }
            if (openThread?.comment?.id == item.id) openThread = openThread?.copy(comment = item.asRemoved())
            return
        }
        updateThread(rootId) {
            it.copy(
                replies = it.replies.filter { reply -> reply.id != item.id },
                replyCount = (it.replyCount - 1).coerceAtLeast(0),
            )
        }
        if (replies.removeAll { it.id == item.id }) repliesTotal = (repliesTotal - 1).coerceAtLeast(0)
    }

    private fun CommentItem.asRemoved() = copy(removed = true, author = null, content = "")

    /** Applies [change] to the thread of [rootId] in both lists. */
    private fun updateThread(rootId: Long, change: (CommentThread) -> CommentThread) {
        for (list in listOf(hotThreads, threads)) {
            val index = list.indexOfFirst { it.comment.id == rootId }
            if (index >= 0) list[index] = change(list[index])
        }
    }

    /** Applies [change] to the comment wherever it's shown. */
    private fun update(id: Long, change: (CommentItem) -> CommentItem) {
        for (list in listOf(hotThreads, threads)) {
            list.forEachIndexed { index, thread ->
                if (thread.comment.id == id) {
                    list[index] = thread.copy(comment = change(thread.comment))
                } else if (thread.replies.any { it.id == id }) {
                    list[index] = thread.copy(replies = thread.replies.map { if (it.id == id) change(it) else it })
                }
            }
        }
        openThread?.let { if (it.comment.id == id) openThread = it.copy(comment = change(it.comment)) }
        replies.forEachIndexed { index, reply -> if (reply.id == id) replies[index] = change(reply) }
    }

    private fun requireLogin(): Boolean {
        if (global.isLoggedIn) return true
        global.alert(Res.string.need_login_message)
        return false
    }

    private fun alertError(err: CommonError, fallback: StringResource) {
        val message = commentErrorMessage(err.code)
        when {
            message != null -> global.alert(message)
            err.msg.isNotBlank() -> global.alert(err.msg)
            else -> global.alert(fallback)
        }
    }

    companion object {
        private const val TAG = "comments"
        private const val PAGE_SIZE = 20
        /** How many replies the list shows under each comment, as the server sends. */
        private const val FIRST_REPLIES = 3
    }
}

/** The page with the comments on this song or post, or on what this comment is on. @since 261031 */
fun UgcPreview.commentsRoute(): Route? = when (this) {
    is UgcPreview.Song -> Route.Root.SongComments(displayId)
    is UgcPreview.Post -> Route.Root.Events.Detail(id, atComments = true)
    is UgcPreview.Comment -> on?.commentsRoute()
    else -> null
}
