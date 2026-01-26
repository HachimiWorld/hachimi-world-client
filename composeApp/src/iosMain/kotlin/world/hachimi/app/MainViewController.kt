package world.hachimi.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.window.ComposeUIViewController
import org.koin.compose.koinInject
import world.hachimi.app.i18n.AppEnvironment
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.App
import world.hachimi.app.ui.theme.AppTheme

fun MainViewController() = ComposeUIViewController {
    val global = koinInject<GlobalStore>()
    AppTheme(global.darkMode ?: isSystemInDarkTheme()) {
        AppEnvironment(global.locale) {
            App(global)
        }
    }
}