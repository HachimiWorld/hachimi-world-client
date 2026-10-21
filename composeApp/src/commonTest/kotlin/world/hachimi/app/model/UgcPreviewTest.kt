package world.hachimi.app.model

import world.hachimi.app.api.WebResp
import world.hachimi.app.api.module.ReportModule
import world.hachimi.app.api.module.UgcPreview
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

    @Test
    fun reportTargetFallsBackToLegacyFields() {
        val old = WebResp.json.decodeFromString<ReportModule.TargetInfo>(
            """{ "title": "Song", "cover_url": null, "display_id": "JM-ABCD-001", "owner": null }"""
        )
        assertEquals("Song", old.headline)
        assertEquals("JM-ABCD-001", old.songDisplayId)

        val new = WebResp.json.decodeFromString<ReportModule.TargetInfo>(
            """
            {
              "preview": { "type": "comment", "content": "Ad", "on": null },
              "title": "Ad", "cover_url": null, "display_id": "", "owner": null
            }
            """.trimIndent()
        )
        assertEquals("Ad", new.headline)
        assertNull(new.songDisplayId)
    }
}
