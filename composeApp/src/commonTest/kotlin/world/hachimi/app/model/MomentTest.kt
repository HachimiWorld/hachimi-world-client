package world.hachimi.app.model

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import world.hachimi.app.api.WebResp
import world.hachimi.app.api.module.FeedItem
import world.hachimi.app.api.module.FeedModule
import world.hachimi.app.api.module.MomentAttachment
import world.hachimi.app.api.module.MomentModule
import world.hachimi.app.api.module.NotificationModule
import world.hachimi.app.api.module.UgcPreview
import world.hachimi.app.api.ok
import world.hachimi.app.nav.Route
import world.hachimi.app.nav.decodeFromBrowserPathString
import world.hachimi.app.nav.encodeToBrowserPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

class MomentTest {
    /** A page of the square as the server sends it, with kinds and attachments added that this version doesn't know. */
    private val squareJson = """
{
  "ok": true,
  "data": {
    "items": [
      {
        "data": {
          "attachments": [
            {
              "data": {
                "cover_url": null,
                "create_time": "2026-11-08T02:39:32.508500Z",
                "description": null,
                "id": 1,
                "name": "分享的歌单",
                "songs_count": 0,
                "update_time": "2026-11-08T02:39:32.508504Z",
                "user_avatar_url": null,
                "user_id": 100002,
                "user_name": "神人34512117"
              },
              "type": "playlist"
            },
            {
              "type": "playlist",
              "data": null
            },
            {
              "type": "vote",
              "data": {
                "id": 3
              }
            },
            {
              "type": "song",
              "data": {
                "id": "not a song"
              }
            }
          ],
          "author": {
            "avatar_url": null,
            "uid": 100002,
            "username": "神人34512117"
          },
          "comment_count": 0,
          "content": "",
          "create_time": "2026-11-08T02:39:32.546579Z",
          "id": 2,
          "images": [],
          "is_hidden": false,
          "like_count": 0,
          "liked": false
        },
        "kind": "moment",
        "time": "2026-11-08T02:39:32.546579Z"
      },
      {
        "data": {
          "attachments": [
            {
              "data": {
                "audio_url": "https://mock-file-host/songs/c86807f8-b6cf-4128-8e02-be88cbd0224f.mp3",
                "cover_url": "https://mock-file-host/images/cover/55d82263381b45011f63217086632454d3331e73.webp",
                "create_time": "2026-11-08T02:39:27.301511Z",
                "creation_type": 0,
                "description": "description-74aee503769148d99ce69cc52db7245b",
                "display_id": "JM-NUEM-915",
                "duration_seconds": 10,
                "explicit": false,
                "external_links": [],
                "gain": -12.869999885559082,
                "id": 1,
                "is_hidden": false,
                "like_count": 0,
                "lyrics": "lyrics-74aee503769148d99ce69cc52db7245b",
                "origin_infos": [
                  {
                    "artist": "群星",
                    "origin_type": 0,
                    "song_display_id": null,
                    "title": "原作",
                    "url": null
                  }
                ],
                "play_count": 0,
                "production_crew": [],
                "release_time": "2026-11-08T02:39:27.266182Z",
                "subtitle": "subtitle-74aee503769148d99ce69cc52db7245b",
                "tags": [],
                "title": "Shared-74aee503769148d99ce69cc52db7245b",
                "uploader_name": "神人32885592",
                "uploader_uid": 100001
              },
              "type": "song"
            }
          ],
          "author": {
            "avatar_url": null,
            "uid": 100002,
            "username": "神人34512117"
          },
          "comment_count": 0,
          "content": "今天听到一首好歌",
          "create_time": "2026-11-08T02:39:32.069049Z",
          "id": 1,
          "images": [
            {
              "height": 683,
              "thumb_url": "https://mock-file-host/images/moment/thumb/0fbdb1f80da86354b22823b1e51bed8ee9852dc7.webp",
              "url": "https://mock-file-host/images/moment/0fbdb1f80da86354b22823b1e51bed8ee9852dc7.webp",
              "width": 2048
            },
            {
              "height": 200,
              "thumb_url": "https://mock-file-host/images/moment/thumb/2ef25c9e71b23b0ac9b9811f3bd4ecb611ef2139.webp",
              "url": "https://mock-file-host/images/moment/2ef25c9e71b23b0ac9b9811f3bd4ecb611ef2139.webp",
              "width": 300
            }
          ],
          "is_hidden": false,
          "like_count": 0,
          "liked": false
        },
        "kind": "moment",
        "time": "2026-11-08T02:39:32.069049Z"
      },
      {
        "time": "2026-11-08T02:39:31Z",
        "kind": "song",
        "data": {
          "id": 5
        }
      }
    ],
    "next_cursor": "1762569572069049.moment.1"
  }
}
    """.trimIndent()

    @Test
    fun decodesSquarePage() {
        val page = WebResp.json.decodeFromString<WebResp<FeedModule.PageResp, Unit>>(squareJson).ok<FeedModule.PageResp>()
        assertEquals("1762569572069049.moment.1", page.nextCursor)
        assertEquals(3, page.items.size)
        val unknown = assertIs<FeedItem.Unknown>(page.items[2])
        assertEquals("song", unknown.kind)

        val second = assertIs<FeedItem.Moment>(page.items[1])
        assertEquals(Instant.parse("2026-11-08T02:39:32.069049Z"), second.time)
        val moment = second.moment
        assertEquals("今天听到一首好歌", moment.content)
        assertEquals(listOf(2048 to 683, 300 to 200), moment.images.map { it.width to it.height })
        assertEquals(
            "https://mock-file-host/images/moment/thumb/0fbdb1f80da86354b22823b1e51bed8ee9852dc7.webp",
            moment.images[0].thumbUrl,
        )
        val song = assertIs<MomentAttachment.Song>(moment.attachments.single())
        assertEquals("JM-NUEM-915", song.song.displayId)

        val first = assertIs<FeedItem.Moment>(page.items[0]).moment
        assertEquals("", first.content)
        assertEquals(100002, first.author.uid)
        assertEquals(4, first.attachments.size)
        val playlist = assertIs<MomentAttachment.Playlist>(first.attachments[0])
        assertEquals("分享的歌单", playlist.playlist.name)
        assertEquals(MomentAttachment.Unavailable("playlist"), first.attachments[1])
        assertEquals(MomentAttachment.Unknown("vote"), first.attachments[2])
        // A shape this version can't read shows like a type it doesn't know
        assertEquals(MomentAttachment.Unknown("song"), first.attachments[3])
    }

    @Test
    fun encodesAttachmentInput() {
        val req = MomentModule.CreateReq("hi", listOf("a"), listOf(MomentModule.AttachmentInput.song(7)))
        assertEquals(
            """{"content":"hi","image_ids":["a"],"attachments":[{"type":"song","data":{"id":7}}]}""",
            WebResp.json.encodeToString(MomentModule.CreateReq.serializer(), req),
        )
    }

    @Test
    fun previewOpensMomentAtComments() {
        val json = """
            [
              { "type": "moment", "id": 3, "content": "", "image_thumb_urls": ["https://example.com/t.webp"] },
              { "type": "comment", "content": "Nice", "on": { "type": "moment", "id": 3, "content": "hi", "image_thumb_urls": [] } }
            ]
        """.trimIndent()
        val previews = WebResp.json.decodeFromString<List<UgcPreview>>(json)
        assertEquals(UgcPreview.Moment(3, "", listOf("https://example.com/t.webp")), previews[0])
        assertEquals("https://example.com/t.webp", previews[0].imageUrl)
        assertNull(previews[1].imageUrl)
        assertEquals(Route.Root.MomentDetail(3, atComments = true), previews[1].commentsRoute())
    }

    @Test
    fun notificationsOpenMoments() {
        val view = NotificationModule.ContentIntent("moment.view", JsonObject(mapOf("moment_id" to JsonPrimitive(4))))
        assertEquals(Route.Root.MomentDetail(4), view.toTarget()?.route)
        val comment = NotificationModule.ContentIntent(
            "moment.comment.view",
            JsonObject(mapOf("moment_id" to JsonPrimitive(4), "comment_id" to JsonPrimitive(9))),
        )
        assertEquals(Route.Root.MomentDetail(4, atComments = true), comment.toTarget()?.route)
        assertNull(NotificationModule.ContentIntent("moment.view", JsonObject(emptyMap())).toTarget())
    }

    @Test
    fun routesRoundTrip() {
        for (route in listOf(Route.Root.Square, Route.Root.CreateMoment, Route.Root.MomentDetail(5, atComments = true))) {
            val path = encodeToBrowserPath(route).encodeToString()
            assertEquals(route, decodeFromBrowserPathString(path))
        }
    }
}
