package world.hachimi.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.awt.SwingWindow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.isTraySupported
import androidx.compose.ui.window.rememberTrayState
import androidx.compose.ui.window.rememberWindowState
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.icon_vector
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.koin.core.context.startKoin
import world.hachimi.app.di.appModule
import world.hachimi.app.i18n.AppEnvironment
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.App
import world.hachimi.app.ui.component.CloseAskDialog
import world.hachimi.app.ui.theme.JvmTheme
import world.hachimi.app.ui.window.WindowFrame
import java.awt.Dimension

internal val LocalWindow = staticCompositionLocalOf<ComposeWindow> { error("Not provided") }

@OptIn(ExperimentalComposeUiApi::class, DelicateCoroutinesApi::class)
fun main() {
    System.setProperty("java.net.useSystemProxies", "true")

    val koin = startKoin {
        modules(appModule)
    }

    val global = koin.koin.get<GlobalStore>()
    global.initialize()

    application {
        val icon = painterResource(Res.drawable.icon_vector)
        var showWindow by remember { mutableStateOf(true) }
        val trayState = rememberTrayState()
        var showCloseAskDialog by remember { mutableStateOf(false) }

        if (isTraySupported) Tray(
            icon = icon, state = trayState,
            onAction = { showWindow = true },
            menu = {
                Item("Show Window", onClick = { showWindow = true })
                Separator()
                Item("Exit", onClick = ::exitApplication)
            }
        )

        val windowState = rememberWindowState(size = DpSize(1200.dp, 800.dp))

        fun onCloseRequest() {
            if (isTraySupported) showCloseAskDialog = true
            else exitApplication()
        }

        if (showWindow) SwingWindow(
            onCloseRequest = ::onCloseRequest,
            title = if (hostOs == OS.MacOS) "" else BuildKonfig.APP_NAME,
            state = windowState,
            icon = icon,
            init = { window ->
                window.minimumSize = Dimension(360, 700)
                if (hostOs == OS.MacOS) {
                    with(window.rootPane) {
                        putClientProperty("apple.awt.application.appearance", "system")
                        putClientProperty("apple.awt.transparentTitleBar", true)
                        putClientProperty("apple.awt.fullWindowContent", true)
                        putClientProperty("apple.awt.windowTitleVisible", false)
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalWindow provides window) {
                val darkMode = global.darkMode ?: isSystemInDarkTheme()
                JvmTheme(darkMode = darkMode) {
                    PlatformWindowFrame(
                        windowState,
                        darkMode,
                        ::onCloseRequest
                    ) {
                        // Apply locale environment before rendering App
                        AppEnvironment(global.locale) {
                            App(global)
                        }
                        if (showCloseAskDialog) CloseAskDialog(
                            onCancel = {
                                showCloseAskDialog = false
                            },
                            onMinimizeClick = {
                                showWindow = false
                                GlobalScope.launch { // May be useless
                                    delay(1000)
                                    System.gc()
                                }
                                showCloseAskDialog = false
                            },
                            onQuitClick = {
                                exitApplication()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformWindowFrame(
    windowState: WindowState,
    darkMode: Boolean,
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    if (hostOs == OS.Windows) {
        WindowFrame(
            state = windowState,
            initialDarkMode = darkMode,
            onCloseRequest = onCloseRequest
        ) {
            content()
        }
    } else {
        content()
    }
}