package world.hachimi.app.api.module

import io.ktor.client.content.ProgressListener
import io.ktor.client.plugins.onUpload
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.io.Source
import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import world.hachimi.app.api.module.SongModule.CreationTypeInfo
import world.hachimi.app.api.module.SongModule.ExternalLink
import kotlin.time.Instant

class PublishModule(
    private val client: ApiClient
) {
    @Serializable
    data class UploadImageResp(
        val tempId: String
    )

    suspend fun uploadCoverImage(
        filename: String,
        source: Source,
        listener: ProgressListener? = null
    ): WebResult<UploadImageResp> {
        return client.postWith("/publish/upload_cover_image") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "image",
                            InputProvider { source },
                            headersOf(HttpHeaders.ContentDisposition, "filename=\"${filename}\"")
                        )
                    }
                )
            )
            onUpload(listener)
        }
    }

    @Serializable
    data class UploadAudioFileResp(
        val tempId: String,
        val durationSecs: Long,
        val title: String?,
        val bitrate: String?,
        val artist: String?,
    )

    suspend fun uploadAudioFile(
        filename: String,
        source: Source,
        listener: ProgressListener? = null
    ): WebResult<UploadAudioFileResp> {
        return client.postWith("/publish/upload_audio_file") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "audio",
                            InputProvider { source },
                            headersOf(HttpHeaders.ContentDisposition, "filename=\"${filename}\"")
                        )
                    }
                )
            )
            timeout {
                socketTimeoutMillis = 60_000
                connectTimeoutMillis = 60_000
                requestTimeoutMillis = 60_000
            }
            onUpload(listener)
        }
    }


    @Serializable
    data class PublishReq(
        val songTempId: String,
        val coverTempId: String,
        val title: String,
        val subtitle: String,
        val description: String,
        val lyrics: String,
        val tagIds: List<Long>,
        val creationInfo: CreationInfo,
        val productionCrew: List<ProductionItem>,
        val externalLinks: List<ExternalLink>,
        /**
         * @since 251105
         */
        val explicit: Boolean?,
        /**
         * @since 251114, should be required in new client.
         */
        val jmid: String?,
        /**
         * @since 251114
         */
        val comment: String?,
    ) {
        @Serializable
        data class CreationInfo(
            // 0: original, 1: derivative work, 2: tertiary work
            val creationType: Int,
            val originInfo: CreationTypeInfo?,
            val derivativeInfo: CreationTypeInfo?,
        )

        @Serializable
        data class ProductionItem(
            val role: String,
            val uid: Long?,
            val name: String?,
        )
    }

    @Serializable
    data class PublishResp(
        val songDisplayId: String
    )

    suspend fun publish(req: PublishReq): WebResult<PublishResp> =
        client.post("/publish/publish", req)


    @Serializable
    data class ModifyReq(
        val songId: Long,
        val songTempId: String?,
        val coverTempId: String?,
        val title: String,
        val subtitle: String,
        val description: String,
        val lyrics: String,
        val tagIds: List<Long>,
        val creationInfo: PublishReq.CreationInfo,
        val productionCrew: List<PublishReq.ProductionItem>,
        val externalLinks: List<ExternalLink>,
        val explicit: Boolean,
        val comment: String?,
    )

    @Serializable
    data class ModifyResp(
        val reviewId: Long
    )

    suspend fun modify(req: ModifyReq): WebResult<ModifyResp> =
        client.post("/publish/modify", req)

    @Serializable
    data class PageReq(
        val pageIndex: Long,
        val pageSize: Long,
    )

    @Serializable
    data class PageResp(
        val data: List<SongPublishReviewBrief>,
        val pageIndex: Long,
        val pageSize: Long,
        val total: Long,
    )

    @Serializable
    data class SongPublishReviewBrief(
        val reviewId: Long,
        val displayId: String,
        val title: String,
        val subtitle: String,
        val artist: String,
        val coverUrl: String,
        val submitTime: Instant,
        val reviewTime: Instant?,
        val status: Int,
        val type: Int
    ) {
        companion object {
            const val STATUS_PENDING = 0
            const val STATUS_APPROVED = 1
            const val STATUS_REJECTED = 2
            const val TYPE_CREATION = 0
            const val TYPE_MODIFICATION = 1
        }
    }


    suspend fun reviewPage(req: PageReq): WebResult<PageResp> =
        client.get("/publish/review/page", req)

    suspend fun reviewPageContributor(req: PageReq): WebResult<PageResp> =
        client.get("/publish/review/page_contributor", req)

    @Serializable
    data class DetailReq(
        val reviewId: Long
    )

    @Serializable
    data class SongPublishReviewData(
        val reviewId: Long,
        val submitTime: Instant,
        val reviewTime: Instant?,
        val reviewComment: String?,
        val status: Int,
        val displayId: String,
        val title: String,
        val subtitle: String,
        val description: String,
        val durationSeconds: Int,
        val lyrics: String,
        val uploaderUid: Long,
        val uploaderName: String,
        val audioUrl: String,
        val coverUrl: String,
        val tags: List<SongModule.TagItem>,
        val productionCrew: List<SongModule.SongProductionCrew>,
        val creationType: Int,
        val originInfos: List<CreationTypeInfo>,
        val externalLink: List<ExternalLink>,
        val explicit: Boolean?
    ) {
        companion object {
            const val CREATION_TYPE_ORIGINAL = 0
            const val CREATION_TYPE_DERIVATION = 1
            const val CREATION_TYPE_DERIVATION_OF_DERIVATION = 2
        }
    }


    suspend fun reviewDetail(req: DetailReq): WebResult<SongPublishReviewData> =
        client.get("/publish/review/detail", req)

    @Serializable
    data class RejectReviewReq(
        val reviewId: Long,
        val comment: String
    )

    suspend fun reviewReject(req: RejectReviewReq): WebResult<Unit> =
        client.post("/publish/review/reject", req)

    @Serializable
    data class ApproveReviewReq(
        val reviewId: Long,
        val comment: String?
    )

    suspend fun reviewApprove(req: ApproveReviewReq): WebResult<Unit> =
        client.post("/publish/review/approve", req)

    @Serializable
    data class ReviewModifyReq(
        val reviewId: Long,
        val songTempId: String?,
        val coverTempId: String?,
        val title: String,
        val subtitle: String,
        val description: String,
        val lyrics: String,
        val tagIds: List<Long>,
        val creationInfo: PublishReq.CreationInfo,
        val productionCrew: List<PublishReq.ProductionItem>,
        val externalLinks: List<ExternalLink>,
        val explicit: Boolean,
        val comment: String?,
    )

    /** @since 260406 */
    suspend fun reviewModify(req: ReviewModifyReq): WebResult<Unit> =
        client.post("/publish/review/modify", req)

    @Serializable
    data class ReviewCommentCreateReq(
        val reviewId: Long,
        val content: String,
    )

    /** @since 260406 */
    suspend fun reviewCommentCreate(req: ReviewCommentCreateReq): WebResult<Unit> =
        client.post("/publish/review/comment/create", req)

    @Serializable
    data class ReviewCommentListReq(
        val reviewId: Long,
        val pageIndex: Long,
        val pageSize: Long,
    )

    @Serializable
    data class ReviewCommentListResp(
        val data: List<ReviewCommentItem>,
        val pageIndex: Long,
        val pageSize: Long,
        val total: Long,
    )

    @Serializable
    data class ReviewCommentItem(
        val id: Long,
        val reviewId: Long,
        val author: UserModule.PublicUserProfile?,
        val content: String,
        val createTime: Instant,
        val updateTime: Instant,
    )

    /** @since 260406 */
    suspend fun reviewCommentList(req: ReviewCommentListReq): WebResult<ReviewCommentListResp> =
        client.get("/publish/review/comment/list", req)

    @Serializable
    data class ReviewCommentDeleteReq(
        val commentId: Long,
    )

    /** @since 260406 */
    suspend fun reviewCommentDelete(req: ReviewCommentDeleteReq): WebResult<Unit> =
        client.post("/publish/review/comment/delete", req)

    @Serializable
    data class ReviewHistoryListReq(
        val reviewId: Long,
        val pageIndex: Long,
        val pageSize: Long,
    )

    @Serializable
    data class ReviewHistoryListResp(
        val data: List<ReviewHistoryItem>,
        val pageIndex: Long,
        val pageSize: Long,
        val total: Long,
    )

    @Serializable
    data class ReviewHistoryItem(
        val id: Long,
        val reviewId: Long,
        val actionType: Int,
        val note: String?,
        val author: UserModule.PublicUserProfile?,
        val createTime: Instant,
        val snapshot: SongPublishReviewData?,
    )

    /** @since 260406 */
    suspend fun reviewHistoryList(req: ReviewHistoryListReq): WebResult<ReviewHistoryListResp> =
        client.get("/publish/review/history/list", req)

    @Serializable
    data class ChangeJmidReq(
        val songId: Long,
        val oldJmid: String,
        val newJmid: String,
    )

    suspend fun changeJmid(req: ChangeJmidReq): WebResult<Unit> =
        client.post("/publish/change_jmid", req)

    @Serializable
    data class JmidCheckPReq(
        val jmidPrefix: String,
    )

    @Serializable
    data class JmidCheckPResp(
        val result: Boolean,
    )

    suspend fun jmidCheckPrefix(req: JmidCheckPReq): WebResult<JmidCheckPResp> =
        client.get("/publish/jmid/check_prefix", req)

    @Serializable
    data class JmidCheckReq(
        val jmid: String,
    )

    @Serializable
    data class JmidCheckResp(
        val result: Boolean,
    )

    suspend fun jmidCheck(req: JmidCheckReq): WebResult<JmidCheckResp> =
        client.get("/publish/jmid/check", req)

    @Serializable
    data class JmidMineResp(
        val jmidPrefix: String?,
    )

    suspend fun jmidMine(): WebResult<JmidMineResp> =
        client.get("/publish/jmid/mine")

    @Serializable
    data class JmidNextResp(
        val jmid: String
    )

    suspend fun jmidGetNext(): WebResult<JmidNextResp> =
        client.get("/publish/jmid/get_next")
}