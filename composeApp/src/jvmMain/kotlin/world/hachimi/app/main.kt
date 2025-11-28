package world.hachimi.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.awt.SwingWindow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.icon_vector
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.koin.core.context.startKoin
import world.hachimi.app.di.appModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.App
import java.awt.Dimension

internal val LocalWindow = staticCompositionLocalOf<ComposeWindow> { error("Not provided") }

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    System.setProperty("apple.awt.application.appearance", "system")
    System.setProperty("java.net.useSystemProxies", "true")

    val koin = startKoin {
        modules(appModule)
    }

    val global = koin.koin.get<GlobalStore>()
    global.initialize()

    application {
        SwingWindow(
            onCloseRequest = ::exitApplication,
            title = if (hostOs == OS.MacOS) "" else BuildKonfig.APP_NAME,
            state = WindowState(
                size = DpSize(1200.dp, 800.dp)
            ),
            icon = painterResource(Res.drawable.icon_vector),
            init = { window ->
                window.minimumSize = Dimension(360, 700)
                if (hostOs == OS.MacOS) {
                    with(window.rootPane) {
                        putClientProperty("apple.awt.transparentTitleBar", true)
                        putClientProperty("apple.awt.fullWindowContent", true)
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalWindow provides window) {
                if (global.initialized) {
                    App()
                } else {
                    // TODO: Add splash screen
                    Box(Modifier.fillMaxSize())
                }
            }
        }
    }
}

