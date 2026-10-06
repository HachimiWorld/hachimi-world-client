package world.hachimi.app.model

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import world.hachimi.app.api.WebResp
import world.hachimi.app.api.module.NotificationModule
import world.hachimi.app.nav.Route
import world.hachimi.app.nav.decodeFromBrowserPathString
import world.hachimi.app.nav.encodeToBrowserPath
import world.hachimi.app.ui.notification.components.formatBadgeCount
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NotificationTest {
    @Test
    fun knownActionMapsToRoute() {
        val intent = NotificationModule.ContentIntent("profile.edit", JsonObject(emptyMap()))
        assertEquals(Route.Root.EditProfile, intent.toTarget()?.route)
    }

    @Test
    fun reviewActionMapsToReviewDetail() {
        val intent = NotificationModule.ContentIntent(
            "creation.review.view",
            JsonObject(mapOf("review_id" to JsonPrimitive(42))),
        )
        assertEquals(Route.Root.CreationCenter.ReviewDetail(42), intent.toTarget()?.route)
    }

    @Test
    fun reviewActionWithoutIdHasNoTarget() {
        val missing = NotificationModule.ContentIntent("creation.review.view", JsonObject(emptyMap()))
        assertNull(missing.toTarget())
        val wrongType = NotificationModule.ContentIntent(
            "creation.review.view",
            JsonObject(mapOf("review_id" to JsonPrimitive("abc"))),
        )
        assertNull(wrongType.toTarget())
    }

    @Test
    fun unknownActionHasNoTarget() {
        val intent = NotificationModule.ContentIntent(
            "song.comment.view",
            JsonObject(mapOf("song_id" to JsonPrimitive(1))),
        )
        assertNull(intent.toTarget())
    }

    @Test
    fun badgeCountCapsAt99() {
        assertEquals("1", formatBadgeCount(1))
        assertEquals("99", formatBadgeCount(99))
        assertEquals("99+", formatBadgeCount(100))
    }

    @Test
    fun decodesListResponse() {
        val json = """
            {
              "items": [
                {
                  "notification_id": "0199b5e2-5c1a-7d3e-8f00-000000000001",
                  "type": "test.sample",
                  "title": "Title",
                  "body": "Body",
                  "content_intent": { "action": "song.comment.view", "data": { "song_id": 1, "comment_id": 2 } },
                  "read_time": null,
                  "create_time": "2026-10-05T06:30:00Z",
                  "some_future_field": 1
                },
                {
                  "notification_id": "0199b5e2-5c1a-7d3e-8f00-000000000000",
                  "type": "unknown.type",
                  "title": "Title",
                  "body": "Body",
                  "content_intent": null,
                  "read_time": "2026-10-05T06:31:00Z",
                  "create_time": "2026-10-05T06:30:00Z"
                }
              ],
              "has_more": false
            }
        """.trimIndent()
        val resp = WebResp.json.decodeFromString<NotificationModule.ListResp>(json)
        val first = resp.items[0]
        assertEquals("song.comment.view", first.contentIntent?.action)
        // Keys inside data are kept as sent, not renamed by the naming strategy
        assertEquals(JsonPrimitive(2), first.contentIntent?.data?.get("comment_id"))
        assertNull(first.readTime)
        assertNull(resp.items[1].contentIntent)
    }

    @Test
    fun detailRouteRoundTrips() {
        val route = Route.Root.NotificationDetail("0199b5e2-5c1a-7d3e-8f00-000000000001")
        val path = encodeToBrowserPath(route).encodeToString()
        assertEquals(route, decodeFromBrowserPathString(path))
    }
}
