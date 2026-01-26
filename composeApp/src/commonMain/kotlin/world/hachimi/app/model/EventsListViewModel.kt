package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.PostModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

class EventsListViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {

    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set

    var loading by mutableStateOf(false)
        private set

    var items by mutableStateOf<List<PostModule.PostDetail>>(emptyList())
        private set

    private var pageIndex: Int = 0
    private var pageSize: Int = 20
    var total by mutableStateOf(0L)
        private set

    var loadingMore by mutableStateOf(false)
        private set

    var noMoreData by mutableStateOf(false)
        private set

    private var loadingJob: Job? = null
    private val loadingMutex = Mutex()

    fun mounted() {
        if (initializeStatus == InitializeStatus.INIT) {
            refresh()
        } else {
            refresh()
        }
    }

    fun dispose() {

    }

    fun retry() {
        if (initializeStatus == InitializeStatus.FAILED) {
            initializeStatus = InitializeStatus.INIT
            refresh()
        }
    }

    fun refresh() = viewModelScope.launch {
        loadingMutex.withLock {
            loadingJob?.cancelAndJoin()
            loadingJob = launch {
                loading = true
                loadingMore = false
                noMoreData = false
                pageIndex = 0
                try {
                    val resp = api.postModule.page(PostModule.PageReq(pageIndex, pageSize))
                    if (resp.ok) {
                        val data = resp.ok()
                        items = data.posts
                        // no total so we detect more by if the page is fully filled
                        total = items.size.toLong()
                        noMoreData = data.posts.size < pageSize
                        if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.LOADED
                    } else {
                        global.alert(resp.err().msg)
                        if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.FAILED
                    }
                } catch (e: CancellationException) {
                    Logger.i("events", "Loading job was cancelled")
                } catch (e: Throwable) {
                    Logger.e("events", "Failed to refresh events(posts)", e)
                    global.alert(e.message)
                    if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.FAILED
                } finally {
                    loading = false
                }
            }
        }
    }

    fun loadMore() = viewModelScope.launch {
        if (loading || loadingMore || noMoreData) return@launch

        loadingMutex.withLock {
            if (loading || loadingMore || noMoreData) return@withLock

            loadingJob?.cancelAndJoin()
            loadingJob = launch {
                loadingMore = true
                try {
                    val nextPageIndex = pageIndex + 1
                    val resp = api.postModule.page(PostModule.PageReq(nextPageIndex, pageSize))
                    if (resp.ok) {
                        val data = resp.ok()
                        pageIndex = nextPageIndex
                        items = items + data.posts
                        total = items.size.toLong()

                        if (data.posts.size < pageSize) {
                            noMoreData = true
                        }
                    } else {
                        global.alert(resp.err().msg)
                    }
                } catch (e: Throwable) {
                    Logger.e("events", "Failed to load more events(posts)", e)
                    global.alert(e.message)
                } finally {
                    loadingMore = false
                }
            }
        }
    }
}
