package world.hachimi.app.model

import world.hachimi.app.api.WebResp
import world.hachimi.app.api.module.CommentModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CommentTest {
    @Test
    fun decodesCommentList() {
        val json = """
            {
              "total": 2,
              "threads": [
                {
                  "comment": {
                    "id": 10,
                    "target_type": "song",
                    "target_id": 7,
                    "root_id": null,
                    "reply_to_id": null,
                    "reply_to_user": null,
                    "author": null,
                    "content": "",
                    "removed": true,
                    "like_count": 3,
                    "liked": false,
                    "create_time": "2026-10-21T08:00:00Z"
                  },
                  "replies": [
                    {
                      "id": 11,
                      "target_type": "song",
                      "target_id": 7,
                      "root_id": 10,
                      "reply_to_id": 10,
                      "reply_to_user": null,
                      "author": { "uid": 100001, "username": "A", "avatar_url": null },
                      "content": "Mine",
                      "removed": false,
                      "like_count": 0,
                      "liked": false,
                      "create_time": "2026-10-21T09:00:00Z"
                    }
                  ],
                  "reply_count": 1
                }
              ],
              "has_more": true
            }
        """.trimIndent()
        val resp = WebResp.json.decodeFromString<CommentModule.ListResp>(json)
        assertTrue(resp.hasMore)
        val thread = resp.threads.single()
        assertTrue(thread.comment.removed)
        assertNull(thread.comment.author)
        assertEquals("Mine", thread.replies.single().content)
        assertEquals(1, thread.replyCount)
    }

    @Test
    fun knownErrorsAreLocalized() {
        for (code in listOf("comment_rejected", "comment_too_long", "rate_limited", "user_banned")) {
            assertTrue(commentErrorMessage(code) != null, code)
        }
        assertNull(commentErrorMessage("internal_error"))
    }
}
