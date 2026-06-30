package world.hachimi.app.ui.design.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.theme.PreviewTheme

object PlaceholderDefaults {
    const val SHORT_TEXT = "████████"
    const val MEDIUM_TEXT = "████████████████████"
}

fun placeholderValue(
    visible: Boolean,
    value: String,
    fallback: String = PlaceholderDefaults.SHORT_TEXT,
): String = if (visible) fallback else value

@Composable
fun rememberShimmerBrush(): Brush {
    val colorScheme = HachimiTheme.colorScheme
    val baseColor = colorScheme.onSurface.copy(alpha = 0.06f)
    val highlightColor = colorScheme.onSurface.copy(alpha = 0.12f)

    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_offset",
    )

    return remember(shimmerOffset, baseColor, highlightColor) {
        Brush.linearGradient(
            colors = listOf(baseColor, highlightColor, baseColor),
            start = Offset(shimmerOffset - 200f, 0f),
            end = Offset(shimmerOffset, 0f),
        )
    }
}

/**
 * Draws a shimmer overlay matching this composable's measured size.
 * The child is still laid out (preserving dimensions) but hidden while [visible] is true.
 */
fun Modifier.placeholder(
    visible: Boolean,
    shape: Shape = RoundedCornerShape(4.dp),
): Modifier = composed {
    if (!visible) return@composed this

    val brush = rememberShimmerBrush()
    clip(shape).drawWithContent {
        drawRect(brush = brush, size = size)
    }
}

@Preview
@Composable
private fun PreviewPlaceholder() {
    PreviewTheme(background = true) {
        Text(
            text = PlaceholderDefaults.SHORT_TEXT,
            modifier = Modifier.placeholder(visible = true),
        )
    }
}

@Preview
@Composable
private fun PreviewPlaceholderBox() {
    PreviewTheme(background = true) {
        Box(Modifier.size(120.dp, 16.dp).placeholder(visible = true))
    }
}