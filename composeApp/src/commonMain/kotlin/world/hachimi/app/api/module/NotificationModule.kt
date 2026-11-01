package world.hachimi.app.api.module

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import kotlin.time.Instant

/**
 * System notifications sent by the server to the current user.
 *
 * @since 261005
 */
class NotificationModule(
    private val client: ApiClient
) {
    /** @since 261005 */
    @Serializable
    data class NotificationItem(
        /** UUIDv7. Newer notifications have greater ids. */
        val notificationId: String,
        /** `<domain>.<event>`. Notifications of unknown types are still shown. */
        val type: String,
        val title: String,
        val body: String,
        /** Where the follow-up button goes, or null if there is none. */
        val contentIntent: ContentIntent?,
        /** Null if unread. */
        val readTime: Instant?,
        val createTime: Instant,
    )

    /**
     * Navigation only. `action` is a dot-separated name such as `song.comment.view`, and `data`
     * holds the ids the target page needs.
     *
     * @since 261005
     */
    @Serializable
    data class ContentIntent(
        val action: String,
        val data: JsonObject,
    )

    /** @since 261005 */
    @Serializable
    data class ListReq(
        /** The last [NotificationItem.notificationId] of the previous page. Null for the first page. */
        val beforeId: String? = null,
        /** 1..=50, defaults to 20. */
        val limit: Int? = null,
    )

    /** @since 261005 */
    @Serializable
    data class ListResp(
        /** Newest first. */
        val items: List<NotificationItem>,
        val hasMore: Boolean,
    )

    /** @since 261005 */
    @Serializable
    data class DetailReq(
        val notificationId: String,
    )

    /** @since 261005 */
    @Serializable
    data class UnreadCountResp(
        val unreadCount: Long,
    )

    /** @since 261005 */
    @Serializable
    data class MarkReadReq(
        val notificationId: String,
    )

    /** @since 261005 */
    @Serializable
    data class MarkReadResp(
        val notificationId: String,
        /** The first time it was marked read. */
        val readTime: Instant,
        /** Replaces the badge count. */
        val unreadCount: Long,
    )

    /** @since 261005 */
    @Serializable
    class MarkAllReadReq

    /** @since 261005 */
    @Serializable
    data class MarkAllReadResp(
        val markedCount: Long,
        /** May be non-zero if a notification arrived meanwhile. */
        val unreadCount: Long,
    )

    /** @since 261005 */
    suspend fun list(req: ListReq): WebResult<ListResp> =
        client.get("/notification/list", req)

    /**
     * Errors with `notification_unavailable` if it isn't the user's, doesn't exist or has expired.
     * Doesn't mark it read.
     *
     * @since 261005
     */
    suspend fun detail(req: DetailReq): WebResult<NotificationItem> =
        client.get("/notification/detail", req)

    /** @since 261005 */
    suspend fun unreadCount(): WebResult<UnreadCountResp> =
        client.get("/notification/unread_count")

    /** @since 261005 */
    suspend fun markRead(req: MarkReadReq): WebResult<MarkReadResp> =
        client.post("/notification/mark_read", req)

    /** @since 261005 */
    suspend fun markAllRead(): WebResult<MarkAllReadResp> =
        client.post("/notification/mark_all_read", MarkAllReadReq())

    companion object {
        const val ERR_UNAVAILABLE = "notification_unavailable"
    }
}
