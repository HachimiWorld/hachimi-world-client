package world.hachimi.app.api.module

import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import kotlin.time.Instant

/**
 * The message center: unread counts of every kind of message, received likes and received replies.
 * Received likes, new followers and replies are read as a whole; system notifications are in
 * [NotificationModule].
 *
 * @since 261006
 */
class MessageModule(
    private val client: ApiClient
) {
    /** @since 261006 */
    @Serializable
    data class SummaryResp(
        /** Sum of the other unread counts, for the entry badge. */
        val totalUnread: Long,
        val systemUnread: Long,
        val likeUnread: Long,
        val followUnread: Long,
        /** Likes after this are unread. */
        val likeReadTime: Instant,
        /** Followers who followed after this are new. */
        val followReadTime: Instant,
        /** @since 261021 */
        val replyUnread: Long = 0,
        /** Replies after this are unread. Null from servers before 261021. @since 261021 */
        val replyReadTime: Instant? = null,
    )

    /** @since 261006 */
    @Serializable
    data class MarkReadReq(
        /** [CHANNEL_LIKE], [CHANNEL_FOLLOW] or [CHANNEL_REPLY] */
        val channel: String,
    )

    /** @since 261006 */
    @Serializable
    data class MarkReadResp(
        val readTime: Instant,
    )

    /** @since 261006 */
    @Serializable
    data class ReceivedLikesReq(
        /** [ReceivedLikeItem.latestLikeTime] of the previous page's last item. */
        val beforeTime: Instant? = null,
        /** [ReceivedLikeItem.songId] of the previous page's last item. */
        val beforeSongId: Long? = null,
        /** 1..=50, defaults to 20. */
        val limit: Int? = null,
    )

    /** @since 261006 */
    @Serializable
    data class ReceivedLikesResp(
        /** Most recently liked first. */
        val items: List<ReceivedLikeItem>,
        val hasMore: Boolean,
    )

    /**
     * The likes one of the user's songs received from others.
     *
     * @since 261006
     */
    @Serializable
    data class ReceivedLikeItem(
        val songId: Long,
        val songDisplayId: String,
        val songTitle: String,
        val coverUrl: String,
        /** All likes from others, not only unread ones. */
        val likeCount: Long,
        val latestLikeTime: Instant,
        /** The latest few, newest first. */
        val latestLikers: List<Liker>,
    )

    /** @since 261006 */
    @Serializable
    data class Liker(
        val uid: Long,
        val username: String,
        val avatarUrl: String?,
    )

    /** @since 261021 */
    @Serializable
    data class ReceivedRepliesReq(
        /** [CommentModule.CommentItem.createTime] of the previous page's last comment. */
        val beforeTime: Instant? = null,
        /** [CommentModule.CommentItem.id] of the previous page's last comment. */
        val beforeCommentId: Long? = null,
        /** 1..=50, defaults to 20. */
        val limit: Int? = null,
    )

    /** @since 261021 */
    @Serializable
    data class ReceivedRepliesResp(
        /** Newest first. */
        val items: List<ReceivedReplyItem>,
        val hasMore: Boolean,
    )

    /**
     * A comment replying to the user, or on their song.
     *
     * @since 261021
     */
    @Serializable
    data class ReceivedReplyItem(
        val comment: CommentModule.CommentItem,
        val target: ReplyTarget,
        /** The user's comment it replies to; null for a comment on their song, or if it was deleted. */
        val repliedContent: String?,
    )

    /** What a received reply is on. @since 261021 */
    @Serializable
    data class ReplyTarget(
        /** [CommentModule.TARGET_SONG] */
        val targetType: String,
        val targetId: Long,
        val preview: UgcPreview,
    )

    /** @since 261006 */
    suspend fun summary(): WebResult<SummaryResp> =
        client.get("/message/summary")

    /** Marks everything in [MarkReadReq.channel] up to now read. @since 261006 */
    suspend fun markRead(req: MarkReadReq): WebResult<MarkReadResp> =
        client.post("/message/mark_read", req)

    /** @since 261006 */
    suspend fun receivedLikes(req: ReceivedLikesReq): WebResult<ReceivedLikesResp> =
        client.get("/message/received_likes", req)

    /** @since 261021 */
    suspend fun receivedReplies(req: ReceivedRepliesReq): WebResult<ReceivedRepliesResp> =
        client.get("/message/received_replies", req)

    companion object {
        const val CHANNEL_LIKE = "like"
        const val CHANNEL_FOLLOW = "follow"
        /** @since 261021 */
        const val CHANNEL_REPLY = "reply"
    }
}
