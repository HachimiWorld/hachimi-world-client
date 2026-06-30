package world.hachimi.app.ui.design.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.theme.PreviewTheme

private val TrackWidth: Dp = 36.dp
private val TrackHeight: Dp = 20.dp
private val ThumbDiameter: Dp = 16.dp
private val ThumbCheckedOffsetX: Dp = 18.dp
private val ThumbUncheckedOffsetX: Dp = 2.dp
private const val AnimationDurationMs = 300

/**
 * A switch component styled with HachimiTheme colors.
 * Pure Canvas implementation — no Material3 dependency.
 */
@Composable
fun Switcher(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colorScheme = HachimiTheme.colorScheme

    val trackColor by animateColorAsState(
        targetValue = if (checked) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.12f),
        animationSpec = tween(durationMillis = AnimationDurationMs),
    )
    val thumbOffsetX by animateFloatAsState(
        targetValue = if (checked) ThumbCheckedOffsetX.value else ThumbUncheckedOffsetX.value,
        animationSpec = tween(durationMillis = AnimationDurationMs),
    )
    val alpha = if (enabled) 1f else 0.38f

    Canvas(
        modifier = modifier
            .size(TrackWidth, TrackHeight)
            .clip(shape = RoundedCornerShape(percent = 50))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
    ) {
        val trackHeightPx = TrackHeight.toPx()
        val cornerRadiusPx = trackHeightPx / 2f

        // Track
        drawRoundRect(
            color = trackColor.copy(alpha = alpha),
            topLeft = Offset(0f, 0f),
            size = Size(TrackWidth.toPx(), trackHeightPx),
            cornerRadius = CornerRadius(cornerRadiusPx),
        )

        // Thumb shadow — draw a slightly offset, blurred circle behind the thumb
        val thumbRadius = ThumbDiameter.toPx() / 2f
        val thumbCenterY = trackHeightPx / 2f
        val thumbCenterX = thumbOffsetX * density + thumbRadius

        drawCircle(
            color = Color.Black.copy(alpha = 0.2f * alpha),
            radius = thumbRadius + 1.dp.toPx(),
            center = Offset(thumbCenterX + 1.dp.toPx(), thumbCenterY + 1.dp.toPx()),
        )

        // Thumb
        drawCircle(
            color = colorScheme.onSurfaceReverse.copy(alpha = alpha),
            radius = thumbRadius,
            center = Offset(thumbCenterX, thumbCenterY),
        )
    }
}

@Preview
@Composable
private fun PreviewSwitcherChecked() {
    PreviewTheme(background = true) {
        Switcher(checked = true, onCheckedChange = {})
    }
}

@Preview
@Composable
private fun PreviewSwitcherUnchecked() {
    PreviewTheme(background = true) {
        Switcher(checked = false, onCheckedChange = {})
    }
}

@Preview
@Composable
private fun PreviewSwitcherDisabled() {
    PreviewTheme(background = true) {
        Column(Modifier.padding(16.dp)) {
            Switcher(checked = true, onCheckedChange = {}, enabled = false)
            Spacer(Modifier.height(8.dp))
            Switcher(checked = false, onCheckedChange = {}, enabled = false)
        }
    }
}
