package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.vectorResource
import world.hachimi.app.ui.component.AmbientChip
import world.hachimi.app.ui.component.Chip
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.util.PlatformIcons

@Composable
fun PVChip(
    platform: String,
    onClick: () -> Unit
) {
    Chip(onClick = onClick) {
        Icon(
            imageVector = when (platform) {
                "bilibili" -> vectorResource(PlatformIcons.bilibili)
                else -> Icons.Default.MusicVideo
            },
            contentDescription = platform
        )
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Filled.ArrowOutward, "Open in new tab", tint = LocalContentColor.current.copy(0.6f))
    }
}

@Composable
fun AmbientPVChip(
    platform: String,
    onClick: () -> Unit
) {
    AmbientChip(onClick = onClick) {
        Icon(
            imageVector = when (platform) {
                "bilibili" -> vectorResource(PlatformIcons.bilibili)
                else -> Icons.Default.MusicVideo
            },
            contentDescription = platform
        )
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Filled.ArrowOutward, "Open in new tab", tint = LocalContentColor.current.copy(0.6f))
    }
}