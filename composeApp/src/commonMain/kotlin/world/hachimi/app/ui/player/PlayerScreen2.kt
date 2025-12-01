package world.hachimi.app.ui.player

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.util.PlatformBackHandler

@Composable
fun PlayerScreen2() {
    val global: GlobalStore = koinInject()
    PlatformBackHandler {
        global.shrinkPlayer()
    }
    ExpandedPlayerScreen2()
}