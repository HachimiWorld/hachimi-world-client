package world.hachimi.app.model

import androidx.compose.runtime.getValue
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

class ReviewHistoryViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
    }

    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var reviewId by mutableStateOf<Long?>(null)
        private set
    var loading by mutableStateOf(false)
        private set
    var items by mutableStateOf<List<PublishModule.ReviewHistoryItem>>(emptyList())
        private set
    var total by mutableStateOf(0L)
        private set
    var currentPage by mutableStateOf(0)
        private set
    var pageSize by mutableStateOf(DEFAULT_PAGE_SIZE)
        private set

    fun mounted(reviewId: Long) {
        if (this.reviewId != reviewId) {
            this.reviewId = reviewId
            initializeStatus = InitializeStatus.INIT
            currentPage = 0
            pageSize = DEFAULT_PAGE_SIZE
            refresh()
        } else if (initializeStatus == InitializeStatus.INIT || initializeStatus == InitializeStatus.FAILED) {
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

    fun goToPage(page: Int) {
        if (page != currentPage) {
            currentPage = page
            refresh()
        }
    }

    fun updatePageSize(size: Int) {
        if (pageSize != size) {
            pageSize = size
            currentPage = 0
            refresh()
        }
    }

    fun refresh() = viewModelScope.launch {
        val id = reviewId ?: return@launch
        loading = true
        try {
            val resp = api.publishModule.reviewHistoryList(
                PublishModule.ReviewHistoryListReq(
                    reviewId = id,
                    pageIndex = currentPage.toLong(),
                    pageSize = pageSize.toLong(),
                )
            )
            if (resp.ok) {
                val data = resp.ok()
                items = data.data
                total = data.total
                currentPage = data.pageIndex.toInt()
                pageSize = data.pageSize.toInt()
                initializeStatus = InitializeStatus.LOADED
            } else {
                global.alert(resp.err().msg)
                initializeStatus = InitializeStatus.FAILED
            }
        } catch (e: Throwable) {
            Logger.e("review_history", "Failed to fetch review history", e)
            global.alert(e.message)
            initializeStatus = InitializeStatus.FAILED
        } finally {
            loading = false
        }
    }
}

