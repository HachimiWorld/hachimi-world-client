package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_done
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.api.ok
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger

class ReviewDetailViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var reviewId by mutableStateOf(0L)
        private set
    var loading by mutableStateOf(false)
        private set
    var data by mutableStateOf<PublishModule.SongPublishReviewData?>(null)
        private set
    var commentInput by mutableStateOf("")
    var operating by mutableStateOf(false)
        private set

    fun mounted(reviewId: Long) {
        if (this.reviewId != reviewId) {
            initializeStatus = InitializeStatus.INIT
        }
        this.reviewId = reviewId
        refresh()
    }

    fun dispose() {

    }

    fun refresh() = viewModelScope.launch {
        loading = true
        try {
            val resp = api.publishModule.reviewDetail(PublishModule.DetailReq(reviewId))
            if (resp.ok) {
                data = resp.ok()
                initializeStatus = InitializeStatus.LOADED
            } else {
                global.alert(resp.err().msg)
                initializeStatus = InitializeStatus.FAILED
            }
        } catch (e: Throwable) {
            Logger.e("review_detail", "Failed to fetch review detail", e)
            global.alert(e.message)
            initializeStatus = InitializeStatus.FAILED
        } finally {
            loading = false
        }
    }

    fun download() {
        data?.let {
            getPlatform().openUrl(it.audioUrl)
        }
    }

    fun approve() = viewModelScope.launch {
        operating = true
        try {
            val resp = api.publishModule.reviewApprove(
                PublishModule.ApproveReviewReq(
                reviewId, commentInput.takeIf { it.isNotBlank() }
            ))
            if (resp.ok) {
                global.alert(Res.string.common_done)
                commentInput = ""
                refresh()
            } else {
                global.alert(resp.err().msg)
            }
        } catch (e: Throwable) {
            Logger.e("review_detail", "Failed to approve review", e)
            global.alert(e.message)
        } finally {
            operating = false
        }
    }

    fun reject() = viewModelScope.launch {
        operating = true
        try {
            val resp = api.publishModule.reviewReject(
                PublishModule.RejectReviewReq(
                    reviewId, commentInput
                ))
            if (resp.ok) {
                global.alert(Res.string.common_done)
                commentInput = ""
                refresh()
            } else {
                global.alert(resp.err().msg)
            }
        } catch (e: Throwable) {
            Logger.e("review_detail", "Failed to approve review", e)
            global.alert(e.message)
        } finally {
            operating = false
        }
    }
}