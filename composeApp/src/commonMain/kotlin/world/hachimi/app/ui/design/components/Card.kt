package world.hachimi.app.ui.design.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import world.hachimi.app.ui.design.HachimiTheme

val CardShadow = Shadow(
    color = Color.Black.copy(0.06f),
    radius = 16.dp,
    spread = 0.dp,
    offset = DpOffset(x = 0.dp, y = 2.dp)
)

@Composable
fun Card(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    onClick: (() -> Unit)? = null,
    contentColor: Color = HachimiTheme.colorScheme.onSurface,
    content: @Composable BoxScope.() -> Unit
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, HachimiTheme.colorScheme.outline, shape)
                .dropShadow(shape = shape, shadow = CardShadow)
                .clip(shape)
                .hazeEffect(
                    hazeState, style = HazeStyle(
                        backgroundColor = HachimiTheme.colorScheme.surface.copy(1f),
                        blurRadius = 80.dp,
                        tint = HazeTint(HachimiTheme.colorScheme.surface)
                    )
                )
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier.pointerInput(Unit) {}),
            content = content
        )
    }
}

@Composable
fun Card(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    onClick: (() -> Unit)? = null,
    contentColor: Color = HachimiTheme.colorScheme.onSurface,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, HachimiTheme.colorScheme.outline, shape)
            .dropShadow(shape = shape, shadow = CardShadow)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier.pointerInput(Unit) {}),
        contentColor = contentColor,
        shape = shape
    ) {
        Box(propagateMinConstraints = true, content = content)
    }
}