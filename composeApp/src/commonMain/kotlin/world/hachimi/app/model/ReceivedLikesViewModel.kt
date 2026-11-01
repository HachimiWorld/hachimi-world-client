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
import world.hachimi.app.api.module.MessageModule.ReceivedLikeItem
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import kotlin.time.Instant

@KoinViewModel
class ReceivedLikesViewModel(
    private val api: ApiClient,
    private val messageCenter: MessageCenterStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {

    /** Most recently liked first. */
    val items = mutableStateListOf<ReceivedLikeItem>()
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var hasMore by mutableStateOf(false)
        private set
    var loadingMore by mutableStateOf(false)
        private set
    var loadMoreFailed by mutableStateOf(false)
        private set
    /** Items liked after this are new. Null if unknown, then nothing is marked new. */
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
                newSince = messageCenter.openChannel(MessageModule.CHANNEL_LIKE)
                val (page, more) = fetch(null)
                items.clear()
                items.addAll(page)
                hasMore = more
                initializeStatus = InitializeStatus.LOADED
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load received likes", e)
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
                val known = items.mapTo(HashSet()) { it.songId }
                items.addAll(page.filter { it.songId !in known })
                hasMore = more
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load more received likes", e)
                loadMoreFailed = true
            } finally {
                loadingMore = false
            }
        }
    }

    private suspend fun fetch(after: ReceivedLikeItem?): Pair<List<ReceivedLikeItem>, Boolean> {
        val resp = api.messageModule.receivedLikes(
            MessageModule.ReceivedLikesReq(
                beforeTime = after?.latestLikeTime,
                beforeSongId = after?.songId,
                limit = PAGE_SIZE,
            )
        )
        if (!resp.ok) error(resp.err().msg)
        val data = resp.ok()
        return data.items to data.hasMore
    }

    fun isNew(item: ReceivedLikeItem): Boolean = newSince?.let { item.latestLikeTime > it } ?: false

    companion object {
        private const val TAG = "received_likes"
        private const val PAGE_SIZE = 20
    }
}
