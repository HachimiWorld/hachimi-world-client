package world.hachimi.app.ui.player.fullscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import world.hachimi.app.ui.LocalAnimatedVisibilityScope
import world.hachimi.app.ui.LocalSharedTransitionScope
import world.hachimi.app.ui.SharedTransitionKeys
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.DiffusionBackground
import world.hachimi.app.ui.design.components.LocalContentColor

@Composable
fun BackgroundContainer(
    painter: Painter,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        Modifier
            .then(
                with(LocalSharedTransitionScope.current) {
                    Modifier.sharedBounds(
                        rememberSharedContentState(SharedTransitionKeys.Bounds),
                        LocalAnimatedVisibilityScope.current
                    )
                }
            )
            .fillMaxSize()
            .background(HachimiTheme.colorScheme.background)
    ) {
        // Background
        DiffusionBackground(
            modifier = Modifier.fillMaxSize(),
            painter = painter
        )
        // Use a singleton Box container to provide content.
        // And use `pointerInput` to intercept unconsumed touch events.
        // This will enable the MinimumInteractiveComponentSize to improve the touching user experience.
        CompositionLocalProvider(LocalContentColor provides HachimiTheme.colorScheme.onSurfaceReverse) {
            Box(
                modifier = Modifier
                    .semantics { isTraversalGroup = true }
                    .pointerInput(Unit) {},
                content = content
            )
        }
    }
}