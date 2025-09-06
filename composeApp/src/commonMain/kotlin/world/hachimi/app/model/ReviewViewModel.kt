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
import world.hachimi.app.nav.Route

class ReviewViewModel(
    private val api: ApiClient,
    private val global: GlobalStore
): ViewModel(CoroutineScope(Dispatchers.IO)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var loading by mutableStateOf(false)
        private set
    var isContributor by mutableStateOf(false)
        private set
    val items = mutableStateListOf<PublishModule.SongPublishReviewBrief>()
    var total by mutableStateOf(0L)
        private set
    var totalPage by mutableStateOf(0)
        private set
    var currentPage by mutableStateOf(0)
        private set
    var pageSize by mutableStateOf(10)
        private set

    fun mounted() {
        refresh()
    }

    fun dispose() {

    }

    fun refresh() = viewModelScope.launch {
        loading = true
        try {
            val resp = api.publishModule.pageContributor(PublishModule.PageReq(currentPage.toLong(), pageSize.toLong()))
            if (resp.ok) {
                val data = resp.ok()
                isContributor = true
                total = data.total
                items.clear()
                items.addAll(data.data)
                totalPage = (total / pageSize).toInt() + if (total % pageSize > 0) 1 else 0
                initializeStatus = InitializeStatus.LOADED
            } else {
                val data = resp.err()
                if (data.code == "permission_denied") {
                    isContributor = false
                    initializeStatus = InitializeStatus.LOADED
                } else {
                    global.alert(data.msg)
                    initializeStatus = InitializeStatus.FAILED
                }
            }
        } catch (e: Exception) {
            Logger.e("review", "Failed to fetch review", e)
            global.alert(e.message)
            initializeStatus = InitializeStatus.FAILED
        } finally {
            loading = false
        }
    }

    fun goToPage(pageIndex: Int) {
        currentPage = pageIndex
        items.clear()
        refresh()
    }

    fun detail(item: PublishModule.SongPublishReviewBrief) {
        global.nav.push(Route.Root.ContributorCenter.ReviewDetail(item.reviewId))
    }

    fun updatePageSize(pageSize: Int) {
        this.pageSize = pageSize
        goToPage(0)
    }
}