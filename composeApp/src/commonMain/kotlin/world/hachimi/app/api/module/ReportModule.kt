package world.hachimi.app.api.module

import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import kotlin.time.Instant

/**
 * Reports. Each reported target has one case, reused forever; reports on it merge into the case.
 *
 * @since 261008
 */
class ReportModule(
    private val client: ApiClient
) {
    /** @since 261008 */
    @Serializable
    data class SubmitReq(
        /** [TARGET_SONG], [TARGET_PLAYLIST] or [TARGET_USER] */
        val targetType: String,
        val targetId: Long,
        /** One of [REASONS] */
        val reason: String,
        /** Required for [REASON_OTHER], up to 500 characters. */
        val detail: String?,
    )

    /** @since 261008 */
    @Serializable
    data class SubmitResp(
        val reportId: Long,
        /** Reviewed before and further reports are ignored; recorded anyway. */
        val alreadyReviewed: Boolean,
    )

    /** @since 261008 */
    @Serializable
    data class QueueReq(
        /** [STATUS_PENDING] (default) or [STATUS_RESOLVED] */
        val status: String? = null,
        /** [CaseItem.lastReportTime] of the previous page's last item. */
        val beforeTime: Instant? = null,
        /** [CaseItem.caseId] of the previous page's last item. */
        val beforeId: Long? = null,
        /** 1..=50, defaults to 20. */
        val limit: Int? = null,
    )

    /** @since 261008 */
    @Serializable
    data class QueueResp(
        /** Most recently reported first. */
        val items: List<CaseItem>,
        val hasMore: Boolean,
    )

    /** @since 261008 */
    @Serializable
    data class CaseItem(
        val caseId: Long,
        val targetType: String,
        val targetId: Long,
        /** Null if the target was deleted. */
        val target: TargetInfo?,
        /** [STATUS_PENDING] or [STATUS_RESOLVED] */
        val status: String,
        val pendingCount: Int,
        /** Pending reports per reason, most first. */
        val reasons: List<ReasonCount>,
        val lastReportTime: Instant,
    )

    /** @since 261008 */
    @Serializable
    data class TargetInfo(
        /** Song title, playlist name or username. */
        val title: String,
        val coverUrl: String?,
        /** Song display id; empty for other kinds. */
        val displayId: String,
        val owner: UserBrief?,
    )

    /** @since 261008 */
    @Serializable
    data class ReasonCount(
        val reason: String,
        val count: Long,
    )

    /** @since 261008 */
    @Serializable
    data class UserBrief(
        val uid: Long,
        val username: String,
        val avatarUrl: String?,
    )

    /** @since 261008 */
    @Serializable
    data class CaseReq(
        val targetType: String,
        val targetId: Long,
    )

    /** @since 261008 */
    @Serializable
    data class CaseResp(
        val case: CaseItem,
        /** Oldest first. */
        val pendingReports: List<ReportItem>,
        /** Past decisions, newest first. */
        val actions: List<ActionItem>,
        /** Verdicts a contributor can choose now; empty when there is nothing to decide on. */
        val verdicts: List<String>,
        /**
         * Content actions a contributor can take now, each with the verdict it goes with.
         *
         * @since 261008
         */
        val contentActions: List<ContentActionOption> = emptyList(),
    )

    /** @since 261008 */
    @Serializable
    data class ContentActionOption(
        /** Declared by the target's kind, such as `hide` or `reset_bio`. */
        val action: String,
        val verdict: String,
        /** Acts against the content, so the owner must be told why. */
        val penalty: Boolean = false,
    )

    /** @since 261008 */
    @Serializable
    data class ReportItem(
        val reportId: Long,
        val reporter: UserBrief?,
        val reason: String,
        val detail: String?,
        val createTime: Instant,
    )

    /** @since 261008 */
    @Serializable
    data class ActionItem(
        val actionId: Long,
        val operator: UserBrief?,
        /** [VERDICT_AGREE], [VERDICT_DISAGREE] or [VERDICT_IGNORE] */
        val verdict: String,
        /** Internal note. */
        val note: String?,
        val ignoreReports: Boolean,
        /** @since 261008 */
        val contentActions: List<String> = emptyList(),
        /** Why, as told to the owner. @since 261008 */
        val authorReason: String? = null,
        val createTime: Instant,
    )

    /** @since 261008 */
    @Serializable
    data class ResolveReq(
        val targetType: String,
        val targetId: Long,
        /** One of [CaseResp.verdicts] */
        val verdict: String,
        /** Internal, up to 1000 characters. */
        val note: String?,
        /** From [CaseResp.contentActions] for [verdict]. @since 261008 */
        val contentActions: List<String>,
        /** Shown to the owner, up to 500 characters; required for penalties. @since 261008 */
        val authorReason: String?,
        /** Record further reports without reopening the case. */
        val ignoreReports: Boolean,
        /** The last pending report seen; later ones stay pending. 0 if there was none. */
        val upToReportId: Long,
    )

    /** @since 261008 */
    @Serializable
    data class ResolveResp(
        val actionId: Long,
        /** [STATUS_PENDING] if reports arrived after [ResolveReq.upToReportId]. */
        val status: String,
        val pendingCount: Int,
    )

    /** @since 261008 */
    @Serializable
    data class HiddenReasonReq(
        val targetType: String,
        val targetId: Long,
    )

    /** @since 261008 */
    @Serializable
    data class HiddenReasonResp(
        /** Whether the user's song or playlist is hidden. */
        val hidden: Boolean,
        val reason: String?,
        val hideTime: Instant?,
    )

    /** @since 261008 */
    suspend fun submit(req: SubmitReq): WebResult<SubmitResp> =
        client.post("/report/submit", req)

    /** Committee and contributors only. @since 261008 */
    suspend fun queue(req: QueueReq): WebResult<QueueResp> =
        client.get("/report/queue", req)

    /** Committee and contributors only. @since 261008 */
    suspend fun case(req: CaseReq): WebResult<CaseResp> =
        client.get("/report/case", req)

    /** Contributors only. @since 261008 */
    suspend fun resolve(req: ResolveReq): WebResult<ResolveResp> =
        client.post("/report/resolve", req)

    /** For the owner: whether their song or playlist is hidden, and why. @since 261008 */
    suspend fun hiddenReason(req: HiddenReasonReq): WebResult<HiddenReasonResp> =
        client.get("/report/hidden_reason", req)

    companion object {
        const val TARGET_SONG = "song"
        const val TARGET_PLAYLIST = "playlist"
        const val TARGET_USER = "user"

        const val REASON_OTHER = "other"
        val REASONS = listOf("spam", "abuse", "illegal", "nsfw", "copyright", REASON_OTHER)

        const val STATUS_PENDING = "pending"
        const val STATUS_RESOLVED = "resolved"

        const val VERDICT_AGREE = "agree"
        const val VERDICT_DISAGREE = "disagree"
        const val VERDICT_IGNORE = "ignore"
    }
}
