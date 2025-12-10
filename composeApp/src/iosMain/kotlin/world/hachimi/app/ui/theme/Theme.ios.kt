package world.hachimi.app.ui.theme

import androidx.compose.runtime.Composable
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.setStatusBarStyle

object IosSystemUIController : SystemUIController {
    override fun setSystemBarsTheme(darkTheme: Boolean) {
        UIApplication.sharedApplication.setStatusBarStyle(
            if (darkTheme) UIStatusBarStyleLightContent else UIStatusBarStyleDarkContent
        )
    }
}

@Composable
actual fun rememberSystemUIController(): SystemUIController {
    return IosSystemUIController
}