package world.hachimi.app.ui.likes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Text

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
) {
    val counted = rhythm?.takeIf { it.days >= MinDays }
    val line = buildList {
        total?.let { add(stringResource(Res.string.likes_total, it.toInt())) }
        counted?.let { add(stringResource(Res.string.likes_rhythm, it.days, it.liked)) }
    }
    if (line.isEmpty()) return
    Column(modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(
            line.joinToString(" · "),
            color = HachimiTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
        if (counted != null) {
            RhythmBars(counted, onDayClick, Modifier.padding(top = 12.dp).fillMaxWidth().height(BarsHeight))
        }
    }
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
    val rise = remember { Animatable(0f) }
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
