package world.hachimi.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.App
import world.hachimi.app.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Force the 3-button navigation bar to be transparent
            // See: https://developer.android.com/develop/ui/views/layout/edge-to-edge#create-transparent
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)

        FileKit.init(this)

        setContent {
            val global = koinInject<GlobalStore>()

            BackHandler {
                if (global.nav.backStack.size > 1) {
                    global.nav.back()
                } else {
                    finish()
                }
            }

            AppTheme(global.darkMode ?: isSystemInDarkTheme()) {
                App(global)
            }
        }
    }
}