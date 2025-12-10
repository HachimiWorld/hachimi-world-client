package world.hachimi.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.awt.SwingWindow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.icon_vector
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.koin.core.context.startKoin
import world.hachimi.app.di.appModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.App
import world.hachimi.app.ui.window.WindowFrame
import java.awt.Dimension

internal val LocalWindow = staticCompositionLocalOf<ComposeWindow> { error("Not provided") }

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    System.setProperty("java.net.useSystemProxies", "true")

    val koin = startKoin {
        modules(appModule)
    }

    val global = koin.koin.get<GlobalStore>()
    global.initialize()

    application {
        val windowState = rememberWindowState(size = DpSize(1200.dp, 800.dp))
        if (global.initialized) SwingWindow(
            onCloseRequest = ::exitApplication,
            title = if (hostOs == OS.MacOS) "" else BuildKonfig.APP_NAME,
            state = windowState,
            icon = painterResource(Res.drawable.icon_vector),
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
                if (hostOs == OS.Windows) {
                    val darkMode = global.darkMode ?: isSystemInDarkTheme()
                    WindowFrame(
                        state = windowState,
                        initialDarkMode = darkMode,
                        onCloseRequest = ::exitApplication
                    ) {
                        App()
                    }
                } else {
                    App()
                }
            }
        }
    }
}