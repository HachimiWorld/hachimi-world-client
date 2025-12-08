package world.hachimi.app.ui.player

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.player.fullscreen.CompactPlayerScreen2
import world.hachimi.app.ui.player.fullscreen.ExpandedPlayerScreen2
import world.hachimi.app.util.PlatformBackHandler
import world.hachimi.app.util.WindowSize

@Composable
fun PlayerScreen2() {
    val global: GlobalStore = koinInject()
    PlatformBackHandler {
        global.shrinkPlayer()
    }
    BoxWithConstraints {
        if (maxWidth < WindowSize.EXPANDED) CompactPlayerScreen2()
        else ExpandedPlayerScreen2()
    }
}