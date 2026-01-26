package world.hachimi.app.ui.player.footer.components

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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

val FooterContainerConerSize = 24.dp

@Composable
fun Container(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val animatedVisibility = LocalAnimatedVisibilityScope.current
    val roundedCornerAnimation by animatedVisibility.transition.animateDp(label = "rounded corner") { enterExitState ->
        when (enterExitState) {
            EnterExitState.PreEnter -> 0.dp
            EnterExitState.Visible -> FooterContainerConerSize
            EnterExitState.PostExit -> 0.dp
        }
    }
    val contentAlpha by animatedVisibility.transition.animateFloat(label = "content alpha") { enterExitState ->
        when (enterExitState) {
            EnterExitState.PreEnter -> 0f
            EnterExitState.Visible -> 1f
            EnterExitState.PostExit -> 0f
        }
    }
    val shape = RoundedCornerShape(roundedCornerAnimation)
    Box(
        modifier = modifier
            .then(
                with(LocalSharedTransitionScope.current) {
                    Modifier.sharedBounds(
                        rememberSharedContentState(SharedTransitionKeys.Bounds),
                        LocalAnimatedVisibilityScope.current,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                        // Applying fading transition will conflicts with the haze layer.
                        // So, we do not use enter/exit transition here.
                        // The fading effect from the full screen player is good enough.
                        enter = EnterTransition.None,
                        exit = ExitTransition.None,
                        // Set zIndex to 0f ensure that the fullscreen player backgroun is rendered over the haze layer.
                        zIndexInOverlay = 0f,
                        clipInOverlayDuringTransition = OverlayClip(shape)
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
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier.pointerInput(Unit) {})
            .graphicsLayer {
                // Apply the fading effect to the content (this does not affect the haze layer),
                // as a workaround for the bug mentioned above
                alpha = contentAlpha
            },
        content = content
    )
}
