package world.hachimi.app.model

import world.hachimi.app.api.WebResp
import world.hachimi.app.api.module.MessageModule
import world.hachimi.app.nav.Route
import world.hachimi.app.nav.decodeFromBrowserPathString
import world.hachimi.app.nav.encodeToBrowserPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class MessageTest {
    @Test
    fun decodesSummary() {
        val json = """
            {
              "total_unread": 15,
              "system_unread": 1,
              "like_unread": 12,
              "follow_unread": 2,
              "like_read_time": "2026-10-01T00:00:00Z",
              "follow_read_time": "2026-09-06T09:42:08.288510Z"
            }
        """.trimIndent()
        val resp = WebResp.json.decodeFromString<MessageModule.SummaryResp>(json)
        assertEquals(15, resp.totalUnread)
        assertEquals(12, resp.likeUnread)
        assertEquals(Instant.parse("2026-10-01T00:00:00Z"), resp.likeReadTime)
    }

    @Test
    fun decodesReceivedLikes() {
        val json = """
            {
              "items": [
                {
                  "song_id": 7,
                  "song_display_id": "JM-ABCD-001",
                  "song_title": "Song",
                  "cover_url": "https://example.com/cover.webp",
                  "like_count": 12,
                  "latest_like_time": "2026-10-06T08:00:00Z",
                  "latest_likers": [
                    { "uid": 100001, "username": "A", "avatar_url": null },
                    { "uid": 100002, "username": "B", "avatar_url": "https://example.com/b.webp" }
                  ]
                }
              ],
              "has_more": true
            }
        """.trimIndent()
        val resp = WebResp.json.decodeFromString<MessageModule.ReceivedLikesResp>(json)
        val item = resp.items.single()
        assertEquals("JM-ABCD-001", item.songDisplayId)
        assertEquals(listOf("A", "B"), item.latestLikers.map { it.username })
        assertEquals(null, item.latestLikers[0].avatarUrl)
    }

    @Test
    fun messageRoutesRoundTrip() {
        for (route in listOf(Route.Root.Messages, Route.Root.ReceivedLikes, Route.Root.NewFollowers)) {
            assertEquals(route, decodeFromBrowserPathString(encodeToBrowserPath(route).encodeToString()))
        }
    }
}
