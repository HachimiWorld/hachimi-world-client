package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.MessageModule
import world.hachimi.app.api.module.MessageModule.ReceivedReplyItem
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import kotlin.time.Instant

@KoinViewModel
class ReceivedRepliesViewModel(
    private val api: ApiClient,
    private val messageCenter: MessageCenterStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {

    /** Newest first. */
    val items = mutableStateListOf<ReceivedReplyItem>()
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var hasMore by mutableStateOf(false)
        private set
    var loadingMore by mutableStateOf(false)
        private set
    var loadMoreFailed by mutableStateOf(false)
        private set
    /** Replies after this are new. Null if unknown, then nothing is marked new. */
    var newSince by mutableStateOf<Instant?>(null)
        private set

    private var loadJob: Job? = null

    fun mounted() {
        if (initializeStatus == InitializeStatus.INIT && loadJob == null) load()
    }

    fun retry() {
        if (initializeStatus == InitializeStatus.FAILED) load()
    }

    private fun load() {
        loadJob = viewModelScope.launch {
            initializeStatus = InitializeStatus.INIT
            try {
                newSince = messageCenter.openChannel(MessageModule.CHANNEL_REPLY)
                val (page, more) = fetch(null)
                items.clear()
                items.addAll(page)
                hasMore = more
                initializeStatus = InitializeStatus.LOADED
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load received replies", e)
                initializeStatus = InitializeStatus.FAILED
            }
        }
    }

    fun loadMore() {
        if (initializeStatus != InitializeStatus.LOADED || !hasMore || loadingMore) return
        val last = items.lastOrNull() ?: return
        viewModelScope.launch {
            loadingMore = true
            loadMoreFailed = false
            try {
                val (page, more) = fetch(last)
                val known = items.mapTo(HashSet()) { it.comment.id }
                items.addAll(page.filter { it.comment.id !in known })
                hasMore = more
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load more received replies", e)
                loadMoreFailed = true
            } finally {
                loadingMore = false
            }
        }
    }

    private suspend fun fetch(after: ReceivedReplyItem?): Pair<List<ReceivedReplyItem>, Boolean> {
        val resp = api.messageModule.receivedReplies(
            MessageModule.ReceivedRepliesReq(
                beforeTime = after?.comment?.createTime,
                beforeCommentId = after?.comment?.id,
                limit = PAGE_SIZE,
            )
        )
        if (!resp.ok) error(resp.err().msg)
        val data = resp.ok()
        return data.items to data.hasMore
    }

    fun isNew(item: ReceivedReplyItem): Boolean = newSince?.let { item.comment.createTime > it } ?: false

    companion object {
        private const val TAG = "received_replies"
        private const val PAGE_SIZE = 20
    }
}
