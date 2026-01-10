package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.LocalAnimatedVisibilityScope
import world.hachimi.app.ui.LocalSharedTransitionScope
import world.hachimi.app.ui.SharedTransitionKeys
import world.hachimi.app.ui.design.HachimiPalette
import world.hachimi.app.ui.design.LocalColorScheme
import world.hachimi.app.ui.design.components.DiffusionBackground
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.isDiffusionBackgroundSupported
import world.hachimi.app.ui.design.hachimiDarkScheme
import world.hachimi.app.ui.design.hachimiLightScheme
import world.hachimi.app.ui.player.footer.components.FooterContainerConerSize
import world.hachimi.app.ui.theme.LocalDarkMode

@Composable
fun BackgroundContainer(
    painter: Painter?,
    dominantColor: Color,
    content: @Composable BoxScope.() -> Unit
) {
    val darkTheme = dominantColor.luminance() < 0.5f

    val animatedVisibility = LocalAnimatedVisibilityScope.current
    val roundedCornerAnimation by animatedVisibility.transition.animateDp(label = "rounded corner") { enterExitState ->
        when (enterExitState) {
            EnterExitState.PreEnter -> FooterContainerConerSize
            EnterExitState.Visible -> 0.dp
            EnterExitState.PostExit -> 24.dp
        }
    }
    Box(
        Modifier
            .then(
                with(LocalSharedTransitionScope.current) {
                    Modifier.sharedBounds(
                        rememberSharedContentState(SharedTransitionKeys.Bounds),
                        animatedVisibility,
                        clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(roundedCornerAnimation)),
                        // Ensure the background be displayed over the haze layer
                        zIndexInOverlay = 1f
                    )
                }
            )
            .fillMaxSize()
            .background(animateColorAsState(dominantColor).value)
    ) {
        // Background
        Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = painter,
        ) { painter ->
            if (painter != null && isDiffusionBackgroundSupported()) {
                DiffusionBackground(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter
                )
            } else Spacer(Modifier.fillMaxSize())
        }


        // Use a singleton Box container to provide content.
        // And use `pointerInput` to intercept unconsumed touch events.
        // This will enable the MinimumInteractiveComponentSize to improve the touching user experience.
        CompositionLocalProvider(
            LocalDarkMode provides darkTheme,
            LocalContentColor provides if (darkTheme) HachimiPalette.onSurfaceDark else HachimiPalette.onSurfaceLight,
            LocalColorScheme provides if (darkTheme) hachimiDarkScheme else hachimiLightScheme
        ) {
            Box(
                modifier = Modifier
                    .semantics { isTraversalGroup = true }
                    .pointerInput(Unit) {},
                content = content
            )
        }
    }
}