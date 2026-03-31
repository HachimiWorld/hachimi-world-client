package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.VersionModule
import world.hachimi.app.api.ok
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger

class ChangelogViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {

    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set

    var loading by mutableStateOf(false)
        private set

    var items by mutableStateOf<List<VersionModule.LatestVersionResp>>(emptyList())
        private set

    private var pageIndex: Int = 0
    private var pageSize: Int = 20

    var loadingMore by mutableStateOf(false)
        private set

    var noMoreData by mutableStateOf(false)
        private set

    private var loadingJob: Job? = null
    private val loadingMutex = Mutex()

    fun mounted() {
        if (initializeStatus == InitializeStatus.INIT) {
            refresh()
        }
    }

    fun dispose() {}

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
                    val variant = getPlatform().variant
                    val resp = api.versionModule.page(
                        VersionModule.PageVersionReq(variant, pageIndex.toLong(), pageSize.toLong())
                    )
                    if (resp.ok) {
                        val data = resp.ok()
                        items = data.data
                        noMoreData = data.data.size < pageSize || (pageIndex.toLong() + 1) * pageSize >= data.total
                        if (initializeStatus == InitializeStatus.INIT) initializeStatus =
                            InitializeStatus.LOADED
                    } else {
                        global.alert(resp.err().msg)
                        if (initializeStatus == InitializeStatus.INIT) initializeStatus =
                            InitializeStatus.FAILED
                    }
                } catch (e: CancellationException) {
                    Logger.i("changelog", "Loading job was cancelled")
                } catch (e: Throwable) {
                    Logger.e("changelog", "Failed to refresh changelog", e)
                    global.alert(e.message)
                    if (initializeStatus == InitializeStatus.INIT) initializeStatus =
                        InitializeStatus.FAILED
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
                    val variant = getPlatform().variant
                    val resp = api.versionModule.page(
                        VersionModule.PageVersionReq(variant, nextPageIndex.toLong(), pageSize.toLong())
                    )
                    if (resp.ok) {
                        val data = resp.ok()
                        pageIndex = nextPageIndex
                        items = items + data.data

                        if (data.data.size < pageSize || (nextPageIndex.toLong() + 1) * pageSize >= data.total) {
                            noMoreData = true
                        }
                    } else {
                        global.alert(resp.err().msg)
                    }
                } catch (e: Throwable) {
                    Logger.e("changelog", "Failed to load more changelog", e)
                    global.alert(e.message)
                } finally {
                    loadingMore = false
                }
            }
        }
    }
}

