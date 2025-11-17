package world.hachimi.app.api.module

import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import kotlin.time.Instant

class PublishModule(
    private val client: ApiClient
) {
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
        val creationInfo: SongModule.PublishReq.CreationInfo,
        val productionCrew: List<SongModule.PublishReq.ProductionItem>,
        val externalLinks: List<SongModule.ExternalLink>,
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
        val originInfos: List<SongModule.CreationTypeInfo>,
        val externalLink: List<SongModule.ExternalLink>,
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