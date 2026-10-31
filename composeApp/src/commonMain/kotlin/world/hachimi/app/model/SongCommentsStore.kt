package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.koin.core.annotation.Singleton
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.err
import world.hachimi.app.api.module.CommentModule
import world.hachimi.app.api.module.CommentModule.CommentItem
import world.hachimi.app.api.module.CommentModule.CommentThread
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

/**
 * The comments of the song in the player, shared by the compact and expanded player. It follows
 * the song the comments tab is bound to with [bind], and keeps what the user is typing.
 */
@Singleton
class SongCommentsStore(
    private val api: ApiClient,
    private val global: GlobalStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var songId by mutableStateOf<Long?>(null)
        private set
    /** The song's uploader, who can delete any comment under it. */
    private var uploaderUid: Long? = null

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
    /** The comment being replied to, or null to comment on the song. */
    var replyTo by mutableStateOf<CommentItem?>(null)
        private set
    var sending by mutableStateOf(false)
        private set

    /** Set when something asked the player to show the comments tab; the player clears it. */
    var pendingOpen by mutableStateOf(false)

    /** Shows the comments of [songId], loading them if it's a different song. */
    fun bind(songId: Long, uploaderUid: Long) {
        this.uploaderUid = uploaderUid
        if (this.songId == songId && initializeStatus != InitializeStatus.FAILED) return
        if (this.songId != songId) {
            input = ""
            replyTo = null
            closeThread()
        }
        this.songId = songId
        load()
    }

    fun changeOrder(order: String) {
        if (this.order == order) return
        this.order = order
        if (songId != null) load()
    }

    fun retry() {
        if (songId != null) load()
    }

    private fun load() {
        val songId = songId ?: return
        loadJob?.cancel()
        hotThreads.clear()
        threads.clear()
        total = 0
        hasMore = false
        loadMoreFailed = false
        loadingMore = false
        initializeStatus = InitializeStatus.INIT
        loadJob = scope.launch {
            try {
                // Both at once; a failure of either fails the load, rethrown here
                val (hot, data) = coroutineScope {
                    val hot = async { fetchHot(songId) }
                    val data = fetch(songId, after = null)
                    hot.await() to data
                }
                hotThreads.addAll(hot.threads)
                threads.addAll(data.threads)
                total = data.total
                hasMore = data.hasMore
                initializeStatus = InitializeStatus.LOADED
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load comments", e)
                initializeStatus = InitializeStatus.FAILED
            }
        }
    }

    fun loadMore() {
        val songId = songId ?: return
        if (initializeStatus != InitializeStatus.LOADED || !hasMore || loadingMore) return
        loadingMore = true
        loadMoreFailed = false
        loadJob = scope.launch {
            try {
                val data = fetch(songId, after = threads.lastOrNull()?.comment)
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
    private suspend fun fetch(songId: Long, after: CommentItem?): CommentModule.ListResp {
        val resp = api.commentModule.list(
            CommentModule.ListReq(CommentModule.TARGET_SONG, songId, order, after?.createTime, after?.id, PAGE_SIZE),
            auth = global.isLoggedIn,
        )
        if (!resp.ok) error(resp.err().msg)
        return resp.ok()
    }

    private suspend fun fetchHot(songId: Long): CommentModule.HotResp {
        val resp = api.commentModule.hot(CommentModule.HotReq(CommentModule.TARGET_SONG, songId), auth = global.isLoggedIn)
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
        openThread = null
        replies.clear()
        repliesStatus = InitializeStatus.INIT
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
        val songId = songId ?: return
        val content = input.trim()
        if (content.isEmpty() || sending || !requireLogin()) return
        val replyTo = replyTo
        sending = true
        scope.launch {
            try {
                val resp = api.commentModule.create(
                    CommentModule.CreateReq(CommentModule.TARGET_SONG, songId, content, replyTo?.id)
                )
                if (!resp.ok) {
                    alertError(resp.err(), Res.string.comment_send_failed)
                    return@launch
                }
                val item = resp.ok().comment
                if (this@SongCommentsStore.songId != songId) return@launch
                input = ""
                this@SongCommentsStore.replyTo = null
                if (item != null) added(item) else global.alert(Res.string.comment_sent_pending)
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

    /** The author can delete their comment, and the uploader any comment under their song. */
    fun canDelete(item: CommentItem): Boolean {
        val uid = global.userInfo?.uid ?: return false
        return !item.removed && (item.author?.uid == uid || uploaderUid == uid)
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

    /**
     * Plays the song (unless it's already playing) and opens its comments in the full player, as
     * the message center and notifications do.
     */
    fun openSongComments(songDisplayId: String) {
        val playing = global.player.playerState.songInfo?.displayId
        if (playing != songDisplayId) {
            global.player.insertToQueueWithFetch(songDisplayId, instantPlay = true, append = false)
        }
        closeThread()
        pendingOpen = true
        global.expandPlayer()
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
