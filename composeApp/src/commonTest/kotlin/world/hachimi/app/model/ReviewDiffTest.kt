package world.hachimi.app.model

import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.util.DiffOp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class ReviewDiffTest {
    private val time = Instant.fromEpochSeconds(0)
    private val tagA = SongModule.TagItem(1, "A", null)
    private val tagB = SongModule.TagItem(2, "B", null)

    private val live = SongModule.PublicSongDetail(
        id = 10, displayId = "JM-ABC-001", title = "Title", subtitle = "", description = "line1\nline2",
        durationSeconds = 120, tags = listOf(tagA), lyrics = "la\nla", audioUrl = "https://a/1.mp3",
        coverUrl = "https://c/1.jpg",
        productionCrew = listOf(SongModule.SongProductionCrew(id = 5, role = "Vocal", uid = 7, personName = null)),
        creationType = 1,
        originInfos = listOf(SongModule.CreationTypeInfo("JM-XYZ-002", "Origin", "Someone", null, 0)),
        uploaderUid = 7, uploaderName = "u", playCount = 0, likeCount = 0, externalLinks = emptyList(),
        createTime = time, releaseTime = time, explicit = false, gain = null,
    )

    private fun proposedFrom(song: SongModule.PublicSongDetail) = PublishModule.SongPublishReviewData(
        reviewId = 1, submitTime = time, reviewTime = null, reviewComment = null, status = 0,
        displayId = song.displayId, title = song.title, subtitle = song.subtitle, description = song.description,
        durationSeconds = song.durationSeconds, lyrics = song.lyrics, uploaderUid = song.uploaderUid,
        uploaderName = song.uploaderName, audioUrl = song.audioUrl, coverUrl = song.coverUrl, tags = song.tags,
        // Row ids and origin display ids differ between the two endpoints even when nothing changed.
        productionCrew = song.productionCrew.map { it.copy(id = 0) },
        creationType = song.creationType,
        originInfos = song.originInfos.map { it.copy(songDisplayId = null) },
        externalLink = song.externalLinks, explicit = song.explicit,
    )

    @Test
    fun unchangedReviewIsEmpty() {
        assertTrue(computeReviewDiff(live, proposedFrom(live)).isEmpty)
    }

    @Test
    fun reportsChangedFieldsOnly() {
        val proposed = proposedFrom(live).copy(
            title = "New title",
            description = "line1\nline2 edited",
            tags = listOf(tagA, tagB),
            audioUrl = "https://a/2.mp3",
        )
        val diff = computeReviewDiff(live, proposed)

        assertEquals(ReviewDiff.Change("Title", "New title"), diff.title)
        assertEquals(
            listOf(DiffOp.Kind.SAME, DiffOp.Kind.REMOVED, DiffOp.Kind.ADDED),
            diff.description!!.map { it.kind }
        )
        assertEquals(listOf(DiffOp(DiffOp.Kind.SAME, tagA), DiffOp(DiffOp.Kind.ADDED, tagB)), diff.tags)
        assertTrue(diff.audioReplaced)
        assertNull(diff.subtitle)
        assertNull(diff.lyrics)
        assertNull(diff.productionCrew)
        assertNull(diff.originInfos)
        assertFalse(diff.isEmpty)
    }
}
