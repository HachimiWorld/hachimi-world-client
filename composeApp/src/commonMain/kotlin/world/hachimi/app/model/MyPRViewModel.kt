package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

class MyPRViewModel(
    private val global: GlobalStore,
    private val api: ApiClient
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var loading by mutableStateOf(false)
        private set
    var total by mutableStateOf(0L)
        private set
    private val _items = mutableStateListOf<PublishModule.SongPublishReviewBrief>()
    val items: List<PublishModule.SongPublishReviewBrief> = _items

    private var page: Int = 0
    private var pageSize: Int = 10
    var loadingMore by mutableStateOf(false)
        private set
    var noMoreData by mutableStateOf(false)
        private set

    fun mounted() {
        if (initializeStatus == InitializeStatus.INIT) {
            refresh()
        } else {
            // TODO[opt](UX): Should we add button to manually refresh?
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
        loading = true
        try {
            val resp = api.publishModule.reviewPage(PublishModule.PageReq(0, pageSize.toLong()))
            if (resp.ok) {
                val data = resp.ok()
                total = data.total
                _items.clear()
                _items.addAll(data.data)
                if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.LOADED
            } else {
                val err = resp.err()
                global.alert(err.msg)
                if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.FAILED
            }
        } catch (e: Throwable) {
            global.alert(e.message)
            Logger.e("my_artwork", "Failed to fetch my artwork", e)
            if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.FAILED
        } finally {
            loading = false
        }
    }

    fun loadMore() = viewModelScope.launch {
        loading = true
        try {
            val nextPageIndex = page + 1
            val resp = api.publishModule.reviewPage(PublishModule.PageReq(nextPageIndex.toLong(), pageSize.toLong()))
            if (resp.ok) {
                val data = resp.ok()
                total = data.total
                _items.addAll(data.data)
                page = nextPageIndex
                if (data.data.size < data.pageSize) {
                    noMoreData = true
                }
            } else {
                val err = resp.err()
                global.alert(err.msg)
            }
        } catch (e: Throwable) {
            global.alert(e.message)
            Logger.e("my_artwork", "Failed to fetch my artwork", e)
        } finally {
            loading = false
        }
    }
}