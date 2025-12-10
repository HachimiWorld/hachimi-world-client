package world.hachimi.app.ui.player

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.player.fullscreen.CompactPlayerScreen2
import world.hachimi.app.ui.player.fullscreen.ExpandedPlayerScreen2
import world.hachimi.app.ui.player.fullscreen.components.BackgroundContainer
import world.hachimi.app.ui.player.fullscreen.components.rememberAsyncPainterAndColor
import world.hachimi.app.util.PlatformBackHandler
import world.hachimi.app.util.WindowSize

@Composable
fun PlayerScreen2() {
    val global: GlobalStore = koinInject()
    val (painter, dominantColor) = rememberAsyncPainterAndColor(global.player.playerState.displayedCover)

    PlatformBackHandler {
        global.shrinkPlayer()
    }
    BackgroundContainer(
        painter = painter,
        dominantColor = dominantColor
    ) {
        if (LocalWindowSize.current.width < WindowSize.MEDIUM) CompactPlayerScreen2()
        else ExpandedPlayerScreen2()
    }
}