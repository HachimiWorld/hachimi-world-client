package world.hachimi.app.ui.player.components

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_close
import hachimiworld.composeapp.generated.resources.moment_share
import hachimiworld.composeapp.generated.resources.player_share_copied_clipboard
import hachimiworld.composeapp.generated.resources.player_share_text
import hachimiworld.composeapp.generated.resources.share
import hachimiworld.composeapp.generated.resources.share_share_music_title
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.message.components.RemoteImage
import world.hachimi.app.ui.theme.PreviewTheme

/** Where a song goes, beside the platform's own share sheet: above the song, so what's shared is clear. */
@Composable
fun ShareDialog(
    onDismissRequest: () -> Unit,
    jmid: String,
    title: String,
    author: String,
    /** Shown above the options. @since 261114 */
    coverUrl: String? = null,
    /** Writes a moment sharing the song; null hides the option. @since 261114 */
    onShareToMoment: (() -> Unit)? = null,
) {
    val ctx = LocalPlatformContext.current
    val text = stringResource(Res.string.player_share_text, title, author, "https://hachimi.world/song/${jmid.lowercase()}")
    var copied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(stringResource(Res.string.share_share_music_title))
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RemoteImage(coverUrl, Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)))
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(
                            title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            author,
                            fontSize = 12.sp,
                            color = HachimiTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    if (onShareToMoment != null) ShareOption(
                        icon = Icons.Outlined.DynamicFeed,
                        label = stringResource(Res.string.moment_share),
                        onClick = onShareToMoment,
                    )
                    ShareOption(
                        icon = Icons.Default.Share,
                        label = stringResource(Res.string.share),
                        onClick = {
                            share(ctx, text)
                            copied = true
                        },
                    )
                }
                if (copied) Text(
                    stringResource(Res.string.player_share_copied_clipboard),
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 12.sp,
                    color = HachimiTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.common_close))
            }
        }
    )
}

/** One way to share, as an icon on a tile with its name under it. */
@Composable
private fun ShareOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(HachimiTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, label, Modifier.size(24.dp), tint = HachimiTheme.colorScheme.onSurface)
        }
        Text(
            label,
            Modifier.padding(top = 8.dp),
            fontSize = 12.sp,
            color = HachimiTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

expect fun share(context: PlatformContext, text: String): Int

@Composable
@Preview
private fun Preview() {
    PreviewTheme(background = false) {
        ShareDialog(
            onDismissRequest = {},
            jmid = "JM-ABCD-001",
            title = "Test Song",
            author = "Test Author",
            onShareToMoment = {},
        )
    }
}
