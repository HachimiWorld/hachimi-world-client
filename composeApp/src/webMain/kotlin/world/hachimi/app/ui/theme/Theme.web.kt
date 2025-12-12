package world.hachimi.app.ui.theme

import androidx.compose.runtime.Composable

object WebSystemUIController : SystemUIController {
    override fun setSystemBarsTheme(darkTheme: Boolean) {
    }
}

@Composable
actual fun rememberSystemUIController(): SystemUIController {
    return WebSystemUIController
}