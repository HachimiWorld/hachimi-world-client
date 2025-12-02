package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.components.HollowIconToggleButton

enum class Page {
    Info, Queue, Lyrics
}

@Composable
fun PagerButtons(
    modifier: Modifier = Modifier,
    currentPage: Page,
    onPageSelect: (Page) -> Unit
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        HollowIconToggleButton(
            currentPage == Page.Info,
            { onPageSelect(Page.Info) },
            icon = Icons.Outlined.Info,
            "Info"
        )
        HollowIconToggleButton(
            currentPage == Page.Queue,
            { onPageSelect(Page.Queue) },
            icon = Icons.AutoMirrored.Outlined.List,
            "Music Queue"
        )
        HollowIconToggleButton(
            currentPage == Page.Lyrics,
            { onPageSelect(Page.Lyrics) },
            icon = Icons.AutoMirrored.Outlined.Chat,
            "Lyrics"
        )
    }
}