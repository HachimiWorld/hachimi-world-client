package world.hachimi.app.model

import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.util.DiffOp
import world.hachimi.app.util.diffSequence
import world.hachimi.app.util.hasChanges

/**
 * What a modification review changes relative to the live song. A null field is unchanged.
 *
 * @since 261016
 */
data class ReviewDiff(
    val title: Change<String>?,
    val subtitle: Change<String>?,
    val description: List<DiffOp<String>>?,
    val cover: Change<String>?,
    val audioReplaced: Boolean,
    val duration: Change<Int>?,
    val creationType: Change<Int>?,
    val explicit: Change<Boolean?>?,
    val originInfos: List<DiffOp<SongModule.CreationTypeInfo>>?,
    val tags: List<DiffOp<SongModule.TagItem>>?,
    val productionCrew: List<DiffOp<SongModule.SongProductionCrew>>?,
    val lyrics: List<DiffOp<String>>?,
    val externalLinks: List<DiffOp<SongModule.ExternalLink>>?,
) {
    data class Change<T>(val before: T, val after: T)

    val isEmpty: Boolean
        get() = title == null && subtitle == null && description == null && cover == null &&
                !audioReplaced && duration == null && creationType == null && explicit == null &&
                originInfos == null && tags == null && productionCrew == null && lyrics == null &&
                externalLinks == null
}

/** @since 261016 */
fun computeReviewDiff(
    live: SongModule.PublicSongDetail,
    proposed: PublishModule.SongPublishReviewData,
): ReviewDiff {
    fun <T> change(before: T, after: T) = if (before != after) ReviewDiff.Change(before, after) else null
    fun <T, K> items(before: List<T>, after: List<T>, key: (T) -> K) =
        diffSequence(before, after, key = key).takeIf { it.hasChanges() }
    fun lines(before: String, after: String) =
        items(before.toLines(), after.toLines()) { it }

    return ReviewDiff(
        title = change(live.title, proposed.title),
        subtitle = change(live.subtitle, proposed.subtitle),
        description = lines(live.description, proposed.description),
        cover = change(live.coverUrl, proposed.coverUrl),
        audioReplaced = live.audioUrl != proposed.audioUrl,
        duration = change(live.durationSeconds, proposed.durationSeconds),
        creationType = change(live.creationType, proposed.creationType),
        explicit = change(live.explicit, proposed.explicit),
        // The review detail does not fill song_display_id reliably, so it is left out of the key.
        originInfos = items(live.originInfos, proposed.originInfos) {
            listOf(it.originType, it.title, it.artist, it.url)
        },
        tags = items(live.tags, proposed.tags) { it.id },
        productionCrew = items(live.productionCrew, proposed.productionCrew) {
            Triple(it.role, it.uid, it.personName)
        },
        lyrics = lines(live.lyrics, proposed.lyrics),
        externalLinks = items(live.externalLinks, proposed.externalLink) { it },
    )
}

private fun String.toLines(): List<String> =
    if (isEmpty()) emptyList() else replace("\r\n", "\n").split('\n')
