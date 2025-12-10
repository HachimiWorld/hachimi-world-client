package world.hachimi.app.ui.player

import androidx.compose.animation.EnterExitState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.LocalAnimatedVisibilityScope
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.player.fullscreen.CompactPlayerScreen2
import world.hachimi.app.ui.player.fullscreen.ExpandedPlayerScreen2
import world.hachimi.app.ui.player.fullscreen.components.BackgroundContainer
import world.hachimi.app.ui.player.fullscreen.components.rememberAsyncPainterAndColor
import world.hachimi.app.ui.theme.LocalDarkMode
import world.hachimi.app.ui.theme.rememberSystemUIController
import world.hachimi.app.util.PlatformBackHandler
import world.hachimi.app.util.WindowSize

@Composable
fun PlayerScreen2() {
    val global: GlobalStore = koinInject()
    val (painter, dominantColor) = rememberAsyncPainterAndColor(global.player.playerState.displayedCover)
    val systemUIController = rememberSystemUIController()
    val parentDarkMode = LocalDarkMode.current
    val animatedVisibility = LocalAnimatedVisibilityScope.current

    PlatformBackHandler {
        global.shrinkPlayer()
    }

    BackgroundContainer(
        painter = painter,
        dominantColor = dominantColor
    ) {
        val contentDarkMode = LocalDarkMode.current

        LaunchedEffect(animatedVisibility.transition.currentState, animatedVisibility.transition.targetState) {
            if (
                animatedVisibility.transition.currentState == EnterExitState.Visible &&
                animatedVisibility.transition.targetState == EnterExitState.Visible
            ) {
                // Entered
                systemUIController.setSystemBarsTheme(contentDarkMode)
            } else if (animatedVisibility.transition.targetState == EnterExitState.PostExit) {
                // Exiting
                systemUIController.setSystemBarsTheme(parentDarkMode)
            }
        }

        // The code below is useful when AnimatedVisibilityScope is not available
        // But this screen requires AnimatedVisibilityScope
        /*DisposableEffect(systemUIController, contentDarkMode) {
            systemUIController.setSystemBarsTheme(contentDarkMode)
            onDispose {
                // Player exit, restore the dark mode settings
                systemUIController.setSystemBarsTheme(parentDarkMode)
            }
        }*/
        if (LocalWindowSize.current.width < WindowSize.MEDIUM) CompactPlayerScreen2()
        else ExpandedPlayerScreen2()
    }
}