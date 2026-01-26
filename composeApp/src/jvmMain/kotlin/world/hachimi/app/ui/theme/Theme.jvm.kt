package world.hachimi.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.awt.ComposeWindow
import world.hachimi.app.LocalWindow
import world.hachimi.app.ui.design.HachimiPalette
import world.hachimi.app.ui.window.LocalWindowFrameState
import world.hachimi.app.ui.window.WindowFrameState
import java.awt.Color

class JvmSystemUIController(
    private val window: ComposeWindow,
    private val windowFrameState: WindowFrameState?
): SystemUIController {
    override fun setSystemBarsTheme(darkTheme: Boolean) {
        // FIXME: Not work on Windows
        val bg = if (darkTheme) HachimiPalette.backgroundDark else HachimiPalette.backgroundLight
        window.background = Color(bg.red, bg.green, bg.blue, bg.alpha)
        windowFrameState?.darkMode = darkTheme
    }
}

@Composable
actual fun rememberSystemUIController(): SystemUIController {
    val window = LocalWindow.current
    val windowFrame = LocalWindowFrameState.current
    return remember(window) { JvmSystemUIController(window, windowFrame) }
}