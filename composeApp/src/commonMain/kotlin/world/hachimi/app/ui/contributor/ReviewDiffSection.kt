package world.hachimi.app.ui.contributor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.review_audio
import hachimiworld.composeapp.generated.resources.review_cover
import hachimiworld.composeapp.generated.resources.review_creation_type
import hachimiworld.composeapp.generated.resources.review_creation_type_derivation
import hachimiworld.composeapp.generated.resources.review_creation_type_derivation_of_derivation
import hachimiworld.composeapp.generated.resources.review_creation_type_original
import hachimiworld.composeapp.generated.resources.review_description_label
import hachimiworld.composeapp.generated.resources.review_diff_audio_replaced
import hachimiworld.composeapp.generated.resources.review_diff_baseline
import hachimiworld.composeapp.generated.resources.review_diff_collapsed
import hachimiworld.composeapp.generated.resources.review_diff_live
import hachimiworld.composeapp.generated.resources.review_diff_no_change
import hachimiworld.composeapp.generated.resources.review_diff_proposed
import hachimiworld.composeapp.generated.resources.review_diff_title
import hachimiworld.composeapp.generated.resources.review_duration
import hachimiworld.composeapp.generated.resources.review_empty_value
import hachimiworld.composeapp.generated.resources.review_explicit
import hachimiworld.composeapp.generated.resources.review_explicit_no
import hachimiworld.composeapp.generated.resources.review_explicit_unmarked
import hachimiworld.composeapp.generated.resources.review_explicit_yes
import hachimiworld.composeapp.generated.resources.review_external_links
import hachimiworld.composeapp.generated.resources.review_lyrics
import hachimiworld.composeapp.generated.resources.review_origin_type
import hachimiworld.composeapp.generated.resources.review_origin_type_derivation
import hachimiworld.composeapp.generated.resources.review_origin_type_original
import hachimiworld.composeapp.generated.resources.review_production_crew
import hachimiworld.composeapp.generated.resources.review_subtitle_label
import hachimiworld.composeapp.generated.resources.review_tags
import hachimiworld.composeapp.generated.resources.review_title_label
import hachimiworld.composeapp.generated.resources.review_unknown
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.model.ReviewDiff
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.DiffOp
import world.hachimi.app.util.formatSongDuration
import kotlin.time.Duration.Companion.seconds

private const val CONTEXT_LINES = 2
private val MARKER_WIDTH = 12.dp

@Composable
internal fun ReviewDiffSection(diff: ReviewDiff) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(Res.string.review_diff_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(Res.string.review_diff_baseline),
            style = MaterialTheme.typography.bodySmall,
            color = HachimiTheme.colorScheme.onSurfaceVariant,
        )
        if (diff.isEmpty) {
            Text(stringResource(Res.string.review_diff_no_change), style = MaterialTheme.typography.bodyMedium)
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DiffContent(diff)
                }
            }
        }
    }
}

@Composable
private fun DiffContent(diff: ReviewDiff) {
    diff.title?.let { ValueField(stringResource(Res.string.review_title_label), it) }
    diff.subtitle?.let { ValueField(stringResource(Res.string.review_subtitle_label), it) }
    diff.description?.let { LinesField(stringResource(Res.string.review_description_label), it) }
    diff.cover?.let { CoverField(it) }
    if (diff.audioReplaced) {
        DiffField(stringResource(Res.string.review_audio)) {
            Text(stringResource(Res.string.review_diff_audio_replaced), style = MaterialTheme.typography.bodySmall)
        }
    }
    diff.duration?.let {
        ValueField(
            stringResource(Res.string.review_duration),
            ReviewDiff.Change(formatSongDuration(it.before.seconds), formatSongDuration(it.after.seconds))
        )
    }
    diff.creationType?.let {
        ValueField(
            stringResource(Res.string.review_creation_type),
            ReviewDiff.Change(creationTypeText(it.before), creationTypeText(it.after))
        )
    }
    diff.explicit?.let {
        ValueField(
            stringResource(Res.string.review_explicit),
            ReviewDiff.Change(explicitText(it.before), explicitText(it.after))
        )
    }
    diff.originInfos?.let { ops ->
        val empty = stringResource(Res.string.review_empty_value)
        ItemsField(stringResource(Res.string.review_origin_type), ops) {
            listOf(originTypeText(it.originType), it.title ?: empty, it.artist ?: empty, it.url ?: empty)
                .joinToString(" · ")
        }
    }
    diff.tags?.let { ops -> ItemsField(stringResource(Res.string.review_tags), ops) { it.name } }
    diff.productionCrew?.let { ops ->
        ItemsField(stringResource(Res.string.review_production_crew), ops) { crewText(it) }
    }
    diff.lyrics?.let { LinesField(stringResource(Res.string.review_lyrics), it) }
    diff.externalLinks?.let { ops ->
        ItemsField(stringResource(Res.string.review_external_links), ops) { "${it.platform} · ${it.url}" }
    }
}

@Composable
private fun DiffField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        content()
    }
}

@Composable
private fun ValueField(label: String, change: ReviewDiff.Change<String>) {
    val empty = stringResource(Res.string.review_empty_value)
    DiffField(label) {
        SelectionContainer {
            Column {
                DiffLine(DiffOp.Kind.REMOVED, change.before.ifEmpty { empty })
                DiffLine(DiffOp.Kind.ADDED, change.after.ifEmpty { empty })
            }
        }
    }
}

@Composable
private fun <T> ItemsField(label: String, ops: List<DiffOp<T>>, text: @Composable (T) -> String) {
    DiffField(label) {
        SelectionContainer {
            Column {
                ops.fastForEach { DiffLine(it.kind, text(it.value)) }
            }
        }
    }
}

/** Shows changed lines with a little context and folds long unchanged runs. */
@Composable
private fun LinesField(label: String, ops: List<DiffOp<String>>) {
    val visible = BooleanArray(ops.size)
    ops.forEachIndexed { index, op ->
        if (op.kind != DiffOp.Kind.SAME) {
            for (i in (index - CONTEXT_LINES).coerceAtLeast(0)..(index + CONTEXT_LINES).coerceAtMost(ops.lastIndex)) {
                visible[i] = true
            }
        }
    }
    DiffField(label) {
        SelectionContainer {
            Column {
                var index = 0
                while (index < ops.size) {
                    if (visible[index]) {
                        DiffLine(ops[index].kind, ops[index].value)
                        index++
                    } else {
                        val start = index
                        while (index < ops.size && !visible[index]) index++
                        Text(
                            stringResource(Res.string.review_diff_collapsed, index - start),
                            modifier = Modifier.padding(vertical = 4.dp).padding(start = 16.dp + MARKER_WIDTH),
                            style = MaterialTheme.typography.labelSmall,
                            color = HachimiTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiffLine(kind: DiffOp.Kind, text: String) {
    val colors = HachimiTheme.colorScheme
    val (marker, background) = when (kind) {
        DiffOp.Kind.SAME -> " " to Color.Transparent
        DiffOp.Kind.REMOVED -> "−" to colors.primaryContainer
        DiffOp.Kind.ADDED -> "+" to colors.secondaryContainer
    }
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(background, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            marker,
            modifier = Modifier.width(MARKER_WIDTH),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = if (kind == DiffOp.Kind.SAME) colors.onSurfaceVariant else colors.onSurface,
        )
    }
}

@Composable
private fun CoverField(change: ReviewDiff.Change<String>) {
    DiffField(stringResource(Res.string.review_cover)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
            CoverThumb(stringResource(Res.string.review_diff_live), change.before)
            CoverThumb(stringResource(Res.string.review_diff_proposed), change.after)
        }
    }
}

@Composable
private fun CoverThumb(label: String, url: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(Modifier.size(96.dp)) {
            AsyncImage(
                ImageRequest.Builder(LocalPlatformContext.current)
                    .httpHeaders(CoilHeaders)
                    .data(url)
                    .crossfade(true)
                    .build(), null, Modifier.size(96.dp)
            )
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = HachimiTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun creationTypeText(type: Int): String = when (type) {
    PublishModule.SongPublishReviewData.CREATION_TYPE_ORIGINAL -> stringResource(Res.string.review_creation_type_original)
    PublishModule.SongPublishReviewData.CREATION_TYPE_DERIVATION -> stringResource(Res.string.review_creation_type_derivation)
    PublishModule.SongPublishReviewData.CREATION_TYPE_DERIVATION_OF_DERIVATION ->
        stringResource(Res.string.review_creation_type_derivation_of_derivation)
    else -> stringResource(Res.string.review_unknown)
}

@Composable
internal fun explicitText(explicit: Boolean?): String = when (explicit) {
    true -> stringResource(Res.string.review_explicit_yes)
    false -> stringResource(Res.string.review_explicit_no)
    null -> stringResource(Res.string.review_explicit_unmarked)
}

@Composable
internal fun originTypeText(type: Int): String = when (type) {
    PublishModule.SongPublishReviewData.CREATION_TYPE_ORIGINAL -> stringResource(Res.string.review_origin_type_original)
    PublishModule.SongPublishReviewData.CREATION_TYPE_DERIVATION -> stringResource(Res.string.review_origin_type_derivation)
    else -> stringResource(Res.string.review_unknown)
}

private fun crewText(crew: SongModule.SongProductionCrew): String = buildString {
    append(crew.role)
    crew.personName?.let { append(" · ").append(it) }
    crew.uid?.let { append(" · UID ").append(it) }
}
