package world.hachimi.app.ui

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import org.koin.compose.koinInject
import soup.compose.material.motion.animation.materialSharedAxisZ
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.Route.Auth
import world.hachimi.app.nav.Route.ForgetPassword
import world.hachimi.app.nav.Route.Root
import world.hachimi.app.ui.auth.AuthScreen
import world.hachimi.app.ui.auth.ForgetPasswordScreen
import world.hachimi.app.ui.component.ClientApiVersionIncompatibleDialog
import world.hachimi.app.ui.component.KidsModeDialog
import world.hachimi.app.ui.component.UpgradeDialog
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.player.PlayerScreen2
import world.hachimi.app.ui.root.RootScreen


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
fun App(global: GlobalStore = koinInject()) {
    setupCoil()
    ProvideLocalWindowSize {
        Surface(
            Modifier.fillMaxSize(),
            color = HachimiTheme.colorScheme.background,
            contentColor = HachimiTheme.colorScheme.onSurface
        ) {
            Box(Modifier.fillMaxSize()) {
                SharedTransitionLayout {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        Content(global.nav.backStack)
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
private fun Content(
    backstack: List<Any>
) {
    val rootDestination = backstack.last()
    AnimatedContent(
        targetState = rootDestination,
        transitionSpec = {
            // TODO(nav): We should decide the `forward` based on the backstack or destination depth
            if (initialState is Root) materialSharedAxisZ(true)
            else if (initialState is Auth && targetState is ForgetPassword) materialSharedAxisZ(true)
            else materialSharedAxisZ(false)
        },
        contentKey = {
            when (it) {
                is Root -> "root"
                is Auth -> "auth"
                is ForgetPassword -> "forget_password"
                else -> it
            }
        }
    ) { rootDestination ->
        when (rootDestination) {
            is Root -> RootScreen(rootDestination)
            is Auth -> AuthScreen(rootDestination.initialLogin)
            is ForgetPassword -> ForgetPasswordScreen()
        }
    }
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

@Suppress("ComposableNaming")
@Composable
fun setupCoil() {
    // Let coil support PlatformFile
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                addPlatformFileSupport()
            }
            .build()
    }
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