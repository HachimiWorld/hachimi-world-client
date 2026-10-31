package world.hachimi.app.api.module

import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import kotlin.time.Instant

/**
 * Comments on songs, two levels deep: comments on the song, and replies under them.
 *
 * @since 261021
 */
class CommentModule(
    private val client: ApiClient
) {
    /** @since 261021 */
    @Serializable
    data class CommentUser(
        val uid: Long,
        val username: String,
        val avatarUrl: String?,
    )

    /**
     * A comment as shown to the current user.
     *
     * @since 261021
     */
    @Serializable
    data class CommentItem(
        val id: Long,
        val targetType: String,
        val targetId: Long,
        /** Null for a top-level comment. */
        val rootId: Long?,
        /** Null for a top-level comment. */
        val replyToId: Long?,
        /** Whom a reply to another reply answers; null otherwise. */
        val replyToUser: CommentUser?,
        /** Null when [removed]. */
        val author: CommentUser?,
        /** Empty when [removed]. */
        val content: String,
        /** Deleted or hidden, kept only so its replies have a place. */
        val removed: Boolean,
        val likeCount: Int,
        val liked: Boolean,
        val createTime: Instant,
    )

    /** @since 261021 */
    @Serializable
    data class CommentThread(
        val comment: CommentItem,
        /** The first few, oldest first. */
        val replies: List<CommentItem>,
        val replyCount: Long,
    )

    /** @since 261021 */
    @Serializable
    data class ListReq(
        /** [TARGET_SONG] */
        val targetType: String,
        /** The song id, not its display id. */
        val targetId: Long,
        /** [ORDER_NEWEST] (default) or [ORDER_OLDEST] */
        val order: String? = null,
        /** [CommentItem.createTime] of the previous page's last thread. */
        val afterTime: Instant? = null,
        /** [CommentItem.id] of the previous page's last thread. */
        val afterCommentId: Long? = null,
        /** 1..=50, defaults to 20. */
        val limit: Int? = null,
    )

    /** @since 261021 */
    @Serializable
    data class ListResp(
        /** Visible comments and replies. */
        val total: Long,
        val threads: List<CommentThread>,
        val hasMore: Boolean,
    )

    /** @since 261021 */
    @Serializable
    data class HotReq(
        /** [TARGET_SONG] */
        val targetType: String,
        /** The song id, not its display id. */
        val targetId: Long,
    )

    /** @since 261021 */
    @Serializable
    data class HotResp(
        /** Most liked first, up to 10 with at least 3 likes each. They're in [list] too. */
        val threads: List<CommentThread>,
    )

    /** @since 261021 */
    @Serializable
    data class RepliesReq(
        val rootId: Long,
        val pageIndex: Int? = null,
        /** 1..=50, defaults to 20. */
        val pageSize: Int? = null,
    )

    /** @since 261021 */
    @Serializable
    data class RepliesResp(
        val total: Long,
        /** Oldest first. */
        val replies: List<CommentItem>,
    )

    /** @since 261021 */
    @Serializable
    data class CreateReq(
        val targetType: String,
        val targetId: Long,
        /** Up to [CONTENT_MAX_CHARS] characters. */
        val content: String,
        /** The comment to reply to; null to comment on the target. */
        val replyToId: Long?,
    )

    /** @since 261021 */
    @Serializable
    data class CreateResp(
        /** Null if it couldn't be checked now and waits to be checked again, seen by no one until it passes. */
        val comment: CommentItem?,
    )

    /** @since 261021 */
    @Serializable
    data class CommentIdReq(
        val commentId: Long,
    )

    /** Pass [auth] when logged in, to see which comments the user liked. @since 261021 */
    suspend fun list(req: ListReq, auth: Boolean = false): WebResult<ListResp> =
        client.get("/comment/list", req, auth = auth)

    /** Pass [auth] when logged in, to see which comments the user liked. @since 261021 */
    suspend fun hot(req: HotReq, auth: Boolean = false): WebResult<HotResp> =
        client.get("/comment/hot", req, auth = auth)

    /** @since 261021 */
    suspend fun replies(req: RepliesReq, auth: Boolean = false): WebResult<RepliesResp> =
        client.get("/comment/replies", req, auth = auth)

    /** @since 261021 */
    suspend fun create(req: CreateReq): WebResult<CreateResp> =
        client.post("/comment/create", req)

    /** For the author, or the owner of what it's on. @since 261021 */
    suspend fun delete(req: CommentIdReq): WebResult<Unit> =
        client.post("/comment/delete", req)

    /** @since 261021 */
    suspend fun like(req: CommentIdReq): WebResult<Unit> =
        client.post("/comment/like", req)

    /** @since 261021 */
    suspend fun unlike(req: CommentIdReq): WebResult<Unit> =
        client.post("/comment/unlike", req)

    companion object {
        const val TARGET_SONG = "song"

        const val ORDER_NEWEST = "newest"
        const val ORDER_OLDEST = "oldest"

        const val CONTENT_MAX_CHARS = 500
    }
}
