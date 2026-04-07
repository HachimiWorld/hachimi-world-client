package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_done
import hachimiworld.composeapp.generated.resources.review_discussion_content_empty
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
    companion object {
        private const val DEFAULT_COMMENT_PAGE_SIZE = 20
    }

    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var reviewId by mutableStateOf(0L)
        private set
    var loading by mutableStateOf(false)
        private set
    var data by mutableStateOf<PublishModule.SongPublishReviewData?>(null)
        private set
    var reviewCommentInput by mutableStateOf("")
    var operating by mutableStateOf(false)
        private set
    var isContributor by mutableStateOf(false)
        private set
    var comments by mutableStateOf<List<PublishModule.ReviewCommentItem>>(emptyList())
        private set
    var commentsLoading by mutableStateOf(false)
        private set
    var commentsSubmitting by mutableStateOf(false)
        private set
    var deletingCommentId by mutableStateOf<Long?>(null)
        private set
    var commentInput by mutableStateOf("")
    var commentPageIndex by mutableStateOf(0)
        private set
    var commentPageSize by mutableStateOf(DEFAULT_COMMENT_PAGE_SIZE)
        private set
    var commentTotal by mutableStateOf(0L)
        private set

    fun mounted(reviewId: Long) {
        if (
            initializeStatus == InitializeStatus.INIT ||
            this.reviewId != reviewId
        ) {
            initializeStatus = InitializeStatus.INIT
            this.reviewId = reviewId
            resetCommentState()
            fetchIsContributor()
            refresh()
        }
    }

    fun dispose() {

    }

    private fun resetCommentState() {
        comments = emptyList()
        commentsLoading = false
        commentsSubmitting = false
        deletingCommentId = null
        commentInput = ""
        commentPageIndex = 0
        commentPageSize = DEFAULT_COMMENT_PAGE_SIZE
        commentTotal = 0L
    }

    private fun fetchIsContributor() = viewModelScope.launch{
        try {
            val resp = api.contributorModule.check()
            isContributor = resp.ok().isContributor
        } catch (e: Throwable) {
            Logger.e("review_detail", "Failed to check maintainer status", e)
            global.alert(e.message)
            isContributor = false
        }
    }

    fun refresh() = viewModelScope.launch {
        loading = true
        try {
            val resp = api.publishModule.reviewDetail(PublishModule.DetailReq(reviewId))
            if (resp.ok) {
                data = resp.ok()
                loadComments(pageIndex = commentPageIndex, pageSize = commentPageSize, showAlert = true)
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

    fun updateCommentPage(pageIndex: Int, pageSize: Int) = viewModelScope.launch {
        loadComments(pageIndex = pageIndex, pageSize = pageSize, showAlert = true)
    }

    fun canDeleteComment(item: PublishModule.ReviewCommentItem): Boolean {
        val currentUid = global.userInfo?.uid
        return isContributor || (currentUid != null && item.author?.uid == currentUid)
    }

    fun createComment() = viewModelScope.launch {
        val content = commentInput.trim()
        if (content.isBlank()) {
            global.alert(Res.string.review_discussion_content_empty)
            return@launch
        }
        commentsSubmitting = true
        try {
            val resp = api.publishModule.reviewCommentCreate(
                PublishModule.ReviewCommentCreateReq(
                    reviewId = reviewId,
                    content = content
                )
            )
            if (resp.ok) {
                global.alert(Res.string.common_done)
                commentInput = ""
                loadComments(pageIndex = 0, pageSize = commentPageSize, showAlert = true)
            } else {
                global.alert(resp.err().msg)
            }
        } catch (e: Throwable) {
            Logger.e("review_detail", "Failed to create review comment", e)
            global.alert(e.message)
        } finally {
            commentsSubmitting = false
        }
    }

    fun deleteComment(commentId: Long) = viewModelScope.launch {
        deletingCommentId = commentId
        try {
            val resp = api.publishModule.reviewCommentDelete(
                PublishModule.ReviewCommentDeleteReq(commentId = commentId)
            )
            if (resp.ok) {
                global.alert(Res.string.common_done)
                val targetPage = if (comments.size == 1 && commentPageIndex > 0) {
                    commentPageIndex - 1
                } else {
                    commentPageIndex
                }
                loadComments(pageIndex = targetPage, pageSize = commentPageSize, showAlert = true)
            } else {
                global.alert(resp.err().msg)
            }
        } catch (e: Throwable) {
            Logger.e("review_detail", "Failed to delete review comment", e)
            global.alert(e.message)
        } finally {
            deletingCommentId = null
        }
    }

    private suspend fun loadComments(
        pageIndex: Int,
        pageSize: Int,
        showAlert: Boolean,
    ) {
        commentsLoading = true
        try {
            val resp = api.publishModule.reviewCommentList(
                PublishModule.ReviewCommentListReq(
                    reviewId = reviewId,
                    pageIndex = pageIndex.toLong(),
                    pageSize = pageSize.toLong(),
                )
            )
            if (resp.ok) {
                val result = resp.ok()
                comments = result.data
                commentPageIndex = result.pageIndex.toInt()
                commentPageSize = result.pageSize.toInt()
                commentTotal = result.total
            } else if (showAlert) {
                global.alert(resp.err().msg)
            }
        } catch (e: Throwable) {
            Logger.e("review_detail", "Failed to load review comments", e)
            if (showAlert) {
                global.alert(e.message)
            }
        } finally {
            commentsLoading = false
        }
    }

    fun approve() = viewModelScope.launch {
        operating = true
        try {
            val resp = api.publishModule.reviewApprove(
                PublishModule.ApproveReviewReq(
                    reviewId, reviewCommentInput.takeIf { it.isNotBlank() }
                ))
            if (resp.ok) {
                global.alert(Res.string.common_done)
                reviewCommentInput = ""
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
                    reviewId, reviewCommentInput
                )
            )
            if (resp.ok) {
                global.alert(Res.string.common_done)
                reviewCommentInput = ""
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