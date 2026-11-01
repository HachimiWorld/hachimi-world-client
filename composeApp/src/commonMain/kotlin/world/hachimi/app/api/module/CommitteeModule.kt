package world.hachimi.app.api.module

import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import world.hachimi.app.api.module.ReportModule.UserBrief
import kotlin.time.Instant

/**
 * The committee: members read the report queue; contributors also decide on reports and appoint
 * members.
 *
 * @since 261008
 */
class CommitteeModule(
    private val client: ApiClient
) {
    /** @since 261008 */
    @Serializable
    data class MeResp(
        /** Committee members and contributors. */
        val canView: Boolean,
        /** Contributors. */
        val canResolve: Boolean,
    )

    /** @since 261008 */
    @Serializable
    data class MembersResp(
        /** Oldest appointment first. */
        val members: List<MemberItem>,
    )

    /** @since 261008 */
    @Serializable
    data class MemberItem(
        val user: UserBrief,
        val appointedBy: UserBrief?,
        val appointTime: Instant,
    )

    /** @since 261008 */
    @Serializable
    data class MemberReq(
        val uid: Long,
    )

    /** @since 261008 */
    suspend fun me(): WebResult<MeResp> =
        client.get("/committee/me")

    /** @since 261008 */
    suspend fun members(): WebResult<MembersResp> =
        client.get("/committee/members")

    /** Contributors only. @since 261008 */
    suspend fun appoint(req: MemberReq): WebResult<Unit> =
        client.post("/committee/appoint", req)

    /** Contributors only. @since 261008 */
    suspend fun revoke(req: MemberReq): WebResult<Unit> =
        client.post("/committee/revoke", req)
}
