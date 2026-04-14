package world.hachimi.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.koinInject
import soup.compose.material.motion.animation.materialSharedAxisZ
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.HandleNavigationRequests
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Navigator
import world.hachimi.app.nav.RootHostNavKey
import world.hachimi.app.nav.Route
import world.hachimi.app.nav.Route.Auth
import world.hachimi.app.nav.Route.ForgetPassword
import world.hachimi.app.ui.auth.AuthScreen
import world.hachimi.app.ui.auth.ForgetPasswordScreen
import world.hachimi.app.ui.component.ClientApiVersionIncompatibleDialog
import world.hachimi.app.ui.component.KidsModeDialog
import world.hachimi.app.ui.component.UpgradeDialog
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.player.PlayerScreen2
import world.hachimi.app.ui.root.RootScreen
import world.hachimi.app.ui.util.setupCoil


val LocalAnimatedVisibilityScope =
    compositionLocalOf<AnimatedVisibilityScope> { error("not provided") }

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> { error("not provided") }

enum class SharedTransitionKeys {
    Cover, Bounds
}

/**
 * A global composition local for the bottom app bar / bottom player insets
 */
val LocalContentInsets = compositionLocalOf { WindowInsets() }
val LocalWindowSize = compositionLocalOf { DpSize.Zero }

@Composable
fun App(
    global: GlobalStore = koinInject(),
    navigator: Navigator = remember { Navigator(Route.Root.Default) }
) {
    setupCoil()
    ProvideLocalWindowSize {
        Surface(
            Modifier.fillMaxSize(),
            color = HachimiTheme.colorScheme.background,
            contentColor = HachimiTheme.colorScheme.onSurface
        ) {
            Box(Modifier.fillMaxSize()) {
                SharedTransitionLayout {
                    CompositionLocalProvider(
                        LocalNavigator provides navigator,
                        LocalSharedTransitionScope provides this
                    ) {
                        HandleNavigationRequests(global.appNavigationRequests)
                        AppNavHost(global)
                        AnimatedVisibility(
                            global.playerExpanded,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                PlayerScreen2()
                            }
                        }
                    }
                }
                SnackbarHost(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp),
                    hostState = global.snackbarHostState,
                )
            }
        }
        GlobalDialogs(global)
    }
}


@Composable
private fun AppNavHost(global: GlobalStore) {
    val navigator = LocalNavigator.current

    NavDisplay(
        backStack = navigator.topLevelBackStack,
        onBack = navigator::back,
        sharedTransitionScope = LocalSharedTransitionScope.current,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = { materialSharedAxisZ(true) },
        popTransitionSpec = { materialSharedAxisZ(false) },
        predictivePopTransitionSpec = { materialSharedAxisZ(false) },
        entryProvider = { key ->
            when (key) {
                RootHostNavKey -> NavEntry(key) {
                    RootScreen()
                }

                is Auth -> NavEntry(key) {
                    AuthScreen(key.initialLogin)
                }

                is ForgetPassword -> NavEntry(key) {
                    ForgetPasswordScreen()
                }

                else -> error("Unknown app route: $key")
            }
        }
    )
}

@Composable
private fun GlobalDialogs(global: GlobalStore) {
    ClientApiVersionIncompatibleDialog(global)
    UpgradeDialog(global)
    if (global.showKidsDialog) KidsModeDialog(
        onDismissRequest = { global.confirmKidsPlay(false) },
        onConfirm = { global.confirmKidsPlay(true) }
    )
}

@Composable
private fun ProvideLocalWindowSize(content: @Composable () -> Unit) {
    BoxWithConstraints {
        CompositionLocalProvider(
            LocalWindowSize provides DpSize(maxWidth, maxHeight),
            content = content
        )
    }
}