package world.hachimi.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import dev.nucleusframework.composenativetray.tray.api.Tray
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import world.hachimi.app.model.PlayerService

/**
 * Native tray backends exist for these platforms only.
 */
val isNativeTraySupported: Boolean = hostOs == OS.Windows || hostOs == OS.MacOS || hostOs == OS.Linux

/**
 * System tray icon with playback controls.
 *
 * Must be called inside the app's locale environment so menu labels follow the in-app language.
 */
@Composable
fun AppTray(
    icon: Painter,
    player: PlayerService,
    onShowWindow: () -> Unit,
    onExit: () -> Unit,
) {
    Tray(
        icon = icon,
        tooltip = BuildKonfig.APP_NAME,
        // macOS opens the menu on click, as other menu bar items do
        primaryAction = if (hostOs == OS.MacOS) null else onShowWindow,
    ) {
        val state = player.playerState
        if (state.hasSong) {
            val title = state.displayedTitle
            val author = state.displayedAuthor
            Item(label = if (author.isEmpty()) title else "$title - $author", isEnabled = false)
            Item(
                label = stringResource(if (state.isPlaying) ResReexport.app_menu_pause else ResReexport.app_menu_play),
                onClick = { player.playOrPause() }
            )
            Item(label = stringResource(ResReexport.app_menu_previous), onClick = { player.previous() })
            Item(label = stringResource(ResReexport.app_menu_next), onClick = { player.next() })
            Divider()
        }
        Item(label = stringResource(ResReexport.app_menu_show_window), onClick = onShowWindow)
        Item(label = stringResource(ResReexport.app_menu_quit), onClick = onExit)
    }
}
