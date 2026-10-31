package world.hachimi.app.model

import world.hachimi.app.api.WebResp
import world.hachimi.app.api.module.UgcPreview
import world.hachimi.app.nav.Route
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UgcPreviewTest {
    @Test
    fun decodesEachKind() {
        val json = """
            [
              { "type": "song", "title": "Song", "cover_url": "https://example.com/s.webp", "display_id": "JM-ABCD-001" },
              { "type": "playlist", "name": "List", "cover_url": null },
              { "type": "user", "username": "A", "avatar_url": "https://example.com/a.webp" },
              { "type": "comment", "content": "Nice", "on": { "type": "song", "title": "Song", "cover_url": "https://example.com/s.webp", "display_id": "JM-ABCD-001" } },
              { "type": "video", "body": "a kind this version doesn't know" },
              { "type": "post", "id": 12, "title": "Post", "cover_url": null },
              { "type": "comment", "content": "Nice", "on": { "type": "post", "id": 12, "title": "Post", "cover_url": "https://example.com/p.webp" } }
            ]
        """.trimIndent()
        val previews = WebResp.json.decodeFromString<List<UgcPreview>>(json)
        assertEquals(UgcPreview.Song("Song", "https://example.com/s.webp", "JM-ABCD-001"), previews[0])
        assertEquals(UgcPreview.Playlist("List", null), previews[1])
        assertEquals(UgcPreview.User("A", "https://example.com/a.webp"), previews[2])
        val comment = previews[3]
        assertEquals("Nice", comment.headline)
        assertEquals("https://example.com/s.webp", comment.imageUrl)
        assertEquals("JM-ABCD-001", comment.songDisplayId)
        assertEquals(UgcPreview.Unknown, previews[4])
        assertEquals(UgcPreview.Post(12, "Post", null), previews[5])
        val postComment = previews[6]
        assertEquals("https://example.com/p.webp", postComment.imageUrl)
        assertNull(postComment.songDisplayId)
        assertEquals(Route.Root.Events.Detail(12, atComments = true), postComment.commentsRoute())
        assertEquals(Route.Root.SongComments("JM-ABCD-001"), comment.commentsRoute())
        assertNull(previews[1].commentsRoute())
    }
}
