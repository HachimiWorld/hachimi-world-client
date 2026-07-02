package world.hachimi.app.ui.design.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.theme.PreviewTheme

private val OuterDiameter: Dp = 24.dp
private val InnerDiameter: Dp = 12.dp
private val StrokeWidth: Dp = 2.dp
private const val AnimationDurationMs = 200

/**
 * A radio button component styled with HachimiTheme colors.
 * Pure Canvas implementation — no Material3 dependency.
 */
@Composable
fun RadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colorScheme = HachimiTheme.colorScheme

    val innerScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(durationMillis = AnimationDurationMs),
    )
    val alpha = if (enabled) 1f else 0.38f

    Canvas(
        modifier = modifier
            .size(OuterDiameter)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        val strokePx = StrokeWidth.toPx()
        val radius = OuterDiameter.toPx() / 2f
        val center = Offset(radius, radius)

        // Outer circle border
        val borderColor = if (innerScale > 0.01f) {
            colorScheme.primary.copy(alpha = alpha)
        } else {
            colorScheme.onSurfaceVariant.copy(alpha = 0.6f * alpha)
        }
        drawCircle(
            color = borderColor,
            radius = radius - strokePx / 2f,
            center = center,
            style = Stroke(width = strokePx),
        )

        // Inner filled circle
        if (innerScale > 0.01f) {
            drawCircle(
                color = colorScheme.primary.copy(alpha = alpha),
                radius = (InnerDiameter.toPx() / 2f) * innerScale,
                center = center,
            )
        }
    }
}

@Composable
fun RadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = onClick,
                indication = null,
                interactionSource = null
            )
            .padding(4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick, enabled = enabled)
        Spacer(modifier = Modifier.size(8.dp))
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.labelLarge
        ) {
            label()
        }
    }
}

@Preview
@Composable
private fun PreviewRadioButtonSelected() {
    PreviewTheme(background = true) {
        RadioButton(selected = true, onClick = {})
    }
}

@Preview
@Composable
private fun PreviewRadioButtonUnselected() {
    PreviewTheme(background = true) {
        RadioButton(selected = false, onClick = {})
    }
}

@Preview
@Composable
private fun PreviewRadioButtonDisabled() {
    PreviewTheme(background = true) {
        Column(Modifier.padding(16.dp)) {
            RadioButton(selected = true, onClick = {}, enabled = false)
            Spacer(Modifier.height(8.dp))
            RadioButton(selected = false, onClick = {}, enabled = false)
        }
    }
}
