package world.hachimi.app.ui.likes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.likes_rhythm
import hachimiworld.composeapp.generated.resources.likes_rhythm_cd
import hachimiworld.composeapp.generated.resources.likes_total
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.model.RecentLikeViewModel.LikeRhythm
import world.hachimi.app.ui.component.PageCaptionStyle
import world.hachimi.app.ui.component.PageHeading
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme

/**
 * The head of the likes: how many there are, and under it the last 30 days as a row of thin bars,
 * one a day, taller the more songs were liked that day. Today's is in the accent color. Tapping a day
 * with likes jumps to them. The bars show once a week of days is counted.
 *
 * @since 261114
 */
@Composable
internal fun LikeSummary(
    total: Long?,
    rhythm: LikeRhythm?,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    /** Whether to write the counts; false when a [PageHeading] above carries them as its caption. */
    showLine: Boolean = true,
) {
    val counted = rhythm?.takeIf { it.days >= MinDays }
    val line = likeSummaryLine(total, rhythm)
    if (line == null && counted == null) return
    Column(modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        if (showLine && line != null) {
            Text(line, style = PageCaptionStyle.copy(color = HachimiTheme.colorScheme.onSurfaceVariant))
        }
        if (counted != null) {
            // Kept to a phone's width on wide screens, where 30 days spread thin
            RhythmBars(counted, onDayClick, Modifier.padding(top = if (showLine) 12.dp else 4.dp).widthIn(max = 480.dp).fillMaxWidth().height(BarsHeight))
        }
    }
}

/**
 * The counts over the likes: the total, and the days counted with how many were liked in them.
 * @since 261114
 */
@Composable
internal fun likeSummaryLine(total: Long?, rhythm: LikeRhythm?): String? {
    val counted = rhythm?.takeIf { it.days >= MinDays }
    val parts = buildList {
        total?.let { add(stringResource(Res.string.likes_total, it.toInt())) }
        counted?.let { add(stringResource(Res.string.likes_rhythm, it.days, it.liked)) }
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

@Composable
private fun RhythmBars(rhythm: LikeRhythm, onDayClick: (LocalDate) -> Unit, modifier: Modifier) {
    val colors = HachimiTheme.colorScheme
    val description = stringResource(Res.string.likes_rhythm_cd, Slots)
    val onDay by rememberUpdatedState(onDayClick)
    // The row always spans 30 slots, so a shorter count leaves the oldest days empty
    val today = rhythm.firstDay.plus(rhythm.days - 1, DateTimeUnit.DAY)
    val firstSlot = today.minus(Slots - 1, DateTimeUnit.DAY)
    val counts = remember(rhythm) {
        IntArray(Slots) { slot ->
            val index = slot - (Slots - rhythm.days)
            if (index >= 0) rhythm.counts[index] else -1
        }
    }
    // The bars rise once, from the oldest day to today
    // Already risen in a preview, which shows a single frame
    val inspecting = LocalInspectionMode.current
    val rise = remember { Animatable(if (inspecting) 1f else 0f) }
    LaunchedEffect(Unit) { rise.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }

    Canvas(
        modifier
            .semantics { contentDescription = description }
            .pointerInput(rhythm) {
                detectTapGestures { offset ->
                    val slot = (offset.x / (size.width / Slots.toFloat())).toInt().coerceIn(0, Slots - 1)
                    if (counts[slot] > 0) onDay(firstSlot.plus(slot, DateTimeUnit.DAY))
                }
            }
    ) {
        val slotWidth = size.width / Slots
        val barWidth = BarWidth.toPx()
        val dot = Dot.toPx()
        val most = counts.max().coerceAtLeast(1)
        for (slot in 0 until Slots) {
            val count = counts[slot]
            if (count < 0) continue
            val isToday = slot == Slots - 1
            // Each bar starts a little after the one before it
            val grown = ((rise.value * 1.6f) - slot / Slots.toFloat() * 0.6f).coerceIn(0f, 1f)
            if (grown <= 0f) continue
            val centerX = slot * slotWidth + slotWidth / 2f
            if (count == 0) {
                // A day without likes is a dot on the baseline
                drawCircle(
                    color = if (isToday) colors.primary else colors.onSurface.copy(alpha = 0.16f),
                    radius = dot / 2f * grown,
                    center = Offset(centerX, size.height - dot / 2f),
                )
                continue
            }
            // Against the busiest day, which reaches the top
            val height = size.height * (MinBar + (1f - MinBar) * count / most) * grown
            drawRoundRect(
                color = if (isToday) colors.primary else colors.onSurface.copy(alpha = 0.5f),
                topLeft = Offset(centerX - barWidth / 2f, size.height - height),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
    }
}

private const val Slots = 30
private const val MinDays = 7
/** How tall a day with a single like is, against the busiest day, when that's more. */
private const val MinBar = 0.3f
private val BarsHeight = 24.dp
private val BarWidth = 4.dp
private val Dot = 3.dp

private fun previewRhythm(days: Int, vararg liked: Pair<Int, Int>): LikeRhythm {
    val today = LocalDate(2026, 11, 14)
    val counts = IntArray(days)
    // daysAgo to songs
    for ((daysAgo, songs) in liked) counts[days - 1 - daysAgo] = songs
    return LikeRhythm(today.minus(days - 1, DateTimeUnit.DAY), counts.toList())
}

@Composable
private fun PreviewSummaries() {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // A month counted, liked today
        LikeSummary(73, previewRhythm(30, 0 to 1, 4 to 2, 6 to 1, 10 to 3, 13 to 1, 21 to 2, 24 to 1), onDayClick = {})
        // A busy month, nothing today
        LikeSummary(412, previewRhythm(30, *Array(29) { (it + 1) to (it * 7 % 5) }), onDayClick = {})
        // Only twelve days counted so far: the older slots stay empty
        LikeSummary(980, previewRhythm(12, 0 to 4, 1 to 6, 3 to 2, 7 to 5, 11 to 3), onDayClick = {})
        // Too few days to draw: the total alone
        LikeSummary(5, previewRhythm(3, 0 to 2), onDayClick = {})
    }
}

@Preview(widthDp = 360, name = "Light")
@Composable
private fun LikeSummaryLightPreview() {
    PreviewTheme(darkTheme = false, background = true) { PreviewSummaries() }
}

@Preview(widthDp = 360, name = "Dark")
@Composable
private fun LikeSummaryDarkPreview() {
    PreviewTheme(darkTheme = true, background = true) { PreviewSummaries() }
}
