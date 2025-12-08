package world.hachimi.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import world.hachimi.app.LocalWindow
import java.awt.Color

@Composable
internal actual fun SystemAppearance(darkTheme: Boolean) {
    val window = LocalWindow.current
    LaunchedEffect(darkTheme) {
        // FIXME: Not work on Windows
        val bg = if (darkTheme) backgroundDark else backgroundLight
        window.background = Color(bg.red, bg.green, bg.blue, bg.alpha)
    }
}