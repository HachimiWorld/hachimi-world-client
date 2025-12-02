package world.hachimi.app.ui.player.footer

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import world.hachimi.app.ui.LocalAnimatedVisibilityScope
import world.hachimi.app.ui.LocalSharedTransitionScope
import world.hachimi.app.ui.SharedTransitionKeys
import world.hachimi.app.ui.design.HachimiTheme

@Composable
fun Container(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .then(
                with(LocalSharedTransitionScope.current) {
                    Modifier.sharedBounds(
                        rememberSharedContentState(SharedTransitionKeys.Bounds),
                        LocalAnimatedVisibilityScope.current,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                        exit = fadeOut()
                    )
                }
            )
            .fillMaxWidth()
            .border(1.dp, HachimiTheme.colorScheme.outline, shape)
            .dropShadow(
                shape = shape,
                shadow = Shadow(color = Color.Black.copy(0.06f), radius = 16.dp, spread = 0.dp),
            )
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
