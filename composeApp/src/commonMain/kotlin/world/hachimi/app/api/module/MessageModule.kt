package world.hachimi.app.api.module

import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import kotlin.time.Instant

/**
 * The message center: unread counts of every kind of message, and received likes. Received likes
 * and new followers are read as a whole; system notifications are in [NotificationModule].
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
    )

    /** @since 261006 */
    @Serializable
    data class MarkReadReq(
        /** [CHANNEL_LIKE] or [CHANNEL_FOLLOW] */
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

    /** @since 261006 */
    suspend fun summary(): WebResult<SummaryResp> =
        client.get("/message/summary")

    /** Marks everything in [MarkReadReq.channel] up to now read. @since 261006 */
    suspend fun markRead(req: MarkReadReq): WebResult<MarkReadResp> =
        client.post("/message/mark_read", req)

    /** @since 261006 */
    suspend fun receivedLikes(req: ReceivedLikesReq): WebResult<ReceivedLikesResp> =
        client.get("/message/received_likes", req)

    companion object {
        const val CHANNEL_LIKE = "like"
        const val CHANNEL_FOLLOW = "follow"
    }
}
