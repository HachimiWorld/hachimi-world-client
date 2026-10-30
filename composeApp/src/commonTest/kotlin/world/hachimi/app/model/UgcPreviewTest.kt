package world.hachimi.app.model

import world.hachimi.app.api.WebResp
import world.hachimi.app.api.module.UgcPreview
import kotlin.test.Test
import kotlin.test.assertEquals

class UgcPreviewTest {
    @Test
    fun decodesEachKind() {
        val json = """
            [
              { "type": "song", "title": "Song", "cover_url": "https://example.com/s.webp", "display_id": "JM-ABCD-001" },
              { "type": "playlist", "name": "List", "cover_url": null },
              { "type": "user", "username": "A", "avatar_url": "https://example.com/a.webp" },
              { "type": "comment", "content": "Nice", "on": { "type": "song", "title": "Song", "cover_url": "https://example.com/s.webp", "display_id": "JM-ABCD-001" } },
              { "type": "post", "body": "a kind this version doesn't know" }
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
    }
}
