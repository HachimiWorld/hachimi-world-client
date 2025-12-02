package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.HollowIconToggleButton
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Surface

enum class Page {
    Info, Queue, Lyrics
}

@Composable
fun PagerButtons(
    modifier: Modifier = Modifier,
    currentPage: Page?,
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

@Composable
fun PagerButtons2(
    modifier: Modifier = Modifier,
    currentPage: Page?,
    onPageSelect: (Page) -> Unit
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceAround) {
        ToggleButton(
            currentPage == Page.Info,
            { onPageSelect(Page.Info) },
        ) {
            Icon(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), imageVector = Icons.Outlined.Info, contentDescription = "Info")
        }
        ToggleButton(
            currentPage == Page.Queue,
            { onPageSelect(Page.Queue) },
        ) {
            Icon(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), imageVector = Icons.AutoMirrored.Outlined.List, contentDescription = "Info")
        }
        ToggleButton(
            currentPage == Page.Lyrics,
            { onPageSelect(Page.Lyrics) },
        ) {
            Icon(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), imageVector = Icons.AutoMirrored.Outlined.Chat, contentDescription = "Info")
        }
    }
}

@Composable
private fun ToggleButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(50),
    activeColor: Color = HachimiTheme.colorScheme.onSurfaceReverse,
    activeContentColor: Color = HachimiTheme.colorScheme.onSurface,
    inactiveColor: Color = Color.Transparent,
    inactiveContentColor: Color = LocalContentColor.current,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = if (selected) activeColor else inactiveColor,
        contentColor = if (selected) activeContentColor else inactiveContentColor
    ) {
        Box(Modifier.selectable(selected = selected, onClick = onClick, role = Role.Tab)) {
            content()
        }
    }
}