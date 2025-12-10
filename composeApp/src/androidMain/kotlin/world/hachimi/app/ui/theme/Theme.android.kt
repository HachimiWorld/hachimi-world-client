package world.hachimi.app.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import world.hachimi.app.logging.Logger

fun setSystemBarTheme(view: View, darkTheme: Boolean) {
    val window = (view.context as Activity).window
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
    }
    WindowInsetsControllerCompat(window, window.decorView).apply {
        isAppearanceLightStatusBars = !darkTheme
        isAppearanceLightNavigationBars = !darkTheme
    }
}

class AndroidSystemUIController(private val view: View) : SystemUIController {
    override fun setSystemBarsTheme(darkTheme: Boolean) {
        Logger.d("AndroidSystemUIController", "setSystemBarsTheme $darkTheme")
        setSystemBarTheme(view, darkTheme)
    }
}

@Composable
actual fun rememberSystemUIController(): SystemUIController {
    val view = LocalView.current
    return remember(view) { AndroidSystemUIController(view) }
}