package world.hachimi.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.Route.*
import world.hachimi.app.ui.auth.AuthScreen
import world.hachimi.app.ui.auth.ForgetPasswordScreen
import world.hachimi.app.ui.component.KidsModeDialog
import world.hachimi.app.ui.component.UpgradeDialog
import world.hachimi.app.ui.player.PlayerScreen
import world.hachimi.app.ui.root.RootScreen
import world.hachimi.app.ui.theme.AppTheme

val LocalDarkMode = compositionLocalOf { false }

val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope> { error("not provided") }

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> { error("not provided") }

enum class SharedTransitionKeys {
    Cover, Bounds
}

@Composable
fun App() {
    setupCoil()

    val global = koinInject<GlobalStore>()
    val rootDestination = global.nav.backStack.last()
    val darkMode = global.darkMode ?: isSystemInDarkTheme()

    CompositionLocalProvider(LocalDarkMode provides darkMode) {
        AppTheme(darkTheme = LocalDarkMode.current) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Box(Modifier.fillMaxSize()) {
                    SharedTransitionLayout {
                        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                            Content(rootDestination)
                            AnimatedVisibility(global.playerExpanded, enter = slideInVertically(initialOffsetY = { it }), exit = slideOutVertically(targetOffsetY = { it })) {
                                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                    PlayerScreen()
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
            ClientApiVersionIncompatibleDialog(global)
            UpgradeDialog(global)
            if (global.showKidsDialog) KidsModeDialog(
                onDismissRequest = { global.confirmKidsPlay(false) },
                onConfirm = { global.confirmKidsPlay(true) }
            )
        }
    }
}


@Composable
private fun Content(rootDestination: Any) {
    when (rootDestination) {
        is Root -> RootScreen(rootDestination)
        is Auth -> AuthScreen(rootDestination.initialLogin)
        is ForgetPassword -> ForgetPasswordScreen()
    }
}

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
private fun ClientApiVersionIncompatibleDialog(global: GlobalStore) {
    if (global.showApiVersionIncompatible) {
        AlertDialog(
            modifier = Modifier.width(280.dp),
            title = {
                Text("客户端版本过低")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("您的客户端已低于服务器支持的最低版本，请更新客户端至最新版本，否则将无法使用！")
                    HorizontalDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Client API Ver: ${global.clientApiVersion}", style = MaterialTheme.typography.bodySmall)
                        Text("Server API Ver: ${global.serverVersion}", style = MaterialTheme.typography.bodySmall)
                        Text("Server Min API Ver: ${global.serverMinVersion}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            icon = {
                Icon(Icons.Default.Warning, "Warning")
            },
            onDismissRequest = {
                // Do nothing
            },
            confirmButton = {
                // No confirm button
            },
        )
    }
}

@Composable
private fun UpgradeDialog(global: GlobalStore) {
    if (global.showUpdateDialog) UpgradeDialog(
        currentVersion = global.currentVersion,
        newVersion = global.newVersionInfo!!.versionName,
        changelog = global.newVersionInfo!!.changelog,
        onDismiss = {
            global.dismissUpgrade()
        },
        onConfirm = {
            global.confirmUpgrade()
        }
    )
}