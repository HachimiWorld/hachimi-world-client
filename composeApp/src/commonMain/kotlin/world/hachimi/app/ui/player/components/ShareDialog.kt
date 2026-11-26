package world.hachimi.app.ui.player.components

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_close
import hachimiworld.composeapp.generated.resources.moment_share
import hachimiworld.composeapp.generated.resources.player_share_copied_clipboard
import hachimiworld.composeapp.generated.resources.player_share_text
import hachimiworld.composeapp.generated.resources.share
import hachimiworld.composeapp.generated.resources.share_copy_link
import hachimiworld.composeapp.generated.resources.share_save_poster
import hachimiworld.composeapp.generated.resources.share_share_music_title
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.Link
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.ModalBottomSheet
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.message.components.RemoteImage
import world.hachimi.app.ui.share.SharePosterDialog
import world.hachimi.app.ui.share.copyToClipboard
import world.hachimi.app.ui.share.songLink
import world.hachimi.app.ui.share.systemShareSupported
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.ui.util.WindowSize

/**
 * Where a song goes: a sheet on phones, a dialog on wider windows. Above the options sits the song itself, so what
 * is shared stays clear.
 */
@Composable
fun ShareDialog(
    onDismissRequest: () -> Unit,
    jmid: String,
    title: String,
    author: String,
    /** Shown above the options, and on the poster. @since 261114 */
    coverUrl: String? = null,
    /** Writes a moment sharing the song; null hides the option. @since 261114 */
    onShareToMoment: (() -> Unit)? = null,
) {
    val ctx = LocalPlatformContext.current
    val link = songLink(jmid)
    val text = stringResource(Res.string.player_share_text, title, author, link)
    var copied by remember { mutableStateOf(false) }
    var posterShown by remember { mutableStateOf(false) }

    val body: @Composable () -> Unit = {
        ShareSheetBody(
            context = ctx,
            title = title,
            author = author,
            coverUrl = coverUrl,
            link = link,
            shareText = text,
            copied = copied,
            onCopied = { copied = true },
            onShareToMoment = onShareToMoment,
            onSavePoster = { posterShown = true },
        )
    }

    if (LocalWindowSize.current.width < WindowSize.COMPACT) {
        ModalBottomSheet(onDismissRequest = onDismissRequest) {
            Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 16.dp)) {
                ShareSheetTitle(onClose = onDismissRequest, compact = true)
                Column(Modifier.padding(top = 12.dp)) { body() }
            }
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { ShareSheetTitle(onClose = onDismissRequest, compact = false) },
            text = body,
            confirmButton = {},
            dismissButton = null,
            showButtons = false,
        )
    }

    if (posterShown) SharePosterDialog(
        onDismissRequest = { posterShown = false },
        title = title,
        author = author,
        coverUrl = coverUrl,
        jmid = jmid,
    )
}

@Composable
private fun ShareSheetTitle(onClose: () -> Unit, compact: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(Res.string.share_share_music_title),
            Modifier.weight(1f),
            fontSize = if (compact) 18.sp else 22.sp,
            fontWeight = FontWeight.Medium,
        )
        HachimiIconButton(onClick = onClose) {
            Icon(Icons.Default.Close, stringResource(Res.string.common_close))
        }
    }
}

@Composable
private fun ShareSheetBody(
    context: PlatformContext,
    title: String,
    author: String,
    coverUrl: String?,
    link: String,
    shareText: String,
    copied: Boolean,
    onCopied: () -> Unit,
    onShareToMoment: (() -> Unit)?,
    onSavePoster: () -> Unit,
) {
    val typography = MaterialTheme.typography
    val variant = HachimiTheme.colorScheme.onSurfaceVariant
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RemoteImage(coverUrl, Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)))
            Column(Modifier.padding(start = 12.dp)) {
                Text(title, style = typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    author,
                    Modifier.padding(top = 2.dp),
                    style = typography.bodySmall,
                    color = variant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        // The link is the plainest way to share, so it gets the room
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(HachimiTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                .padding(start = 12.dp, end = 4.dp)
                .heightIn(min = 44.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Link, null, Modifier.size(16.dp), tint = variant)
            Text(
                link,
                Modifier.weight(1f).padding(start = 8.dp),
                style = typography.bodyMedium,
                color = HachimiTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(
                onClick = {
                    copyToClipboard(context, link)
                    onCopied()
                },
            ) {
                Icon(
                    if (copied) Icons.Default.Check else Icons.Outlined.ContentCopy,
                    stringResource(if (copied) Res.string.player_share_copied_clipboard else Res.string.share_copy_link),
                    Modifier.size(18.dp),
                    tint = if (copied) MaterialTheme.colorScheme.primary else variant,
                )
            }
        }
        Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            if (onShareToMoment != null) ShareAction(
                icon = Icons.Outlined.DynamicFeed,
                label = stringResource(Res.string.moment_share),
                onClick = onShareToMoment,
            )
            // Only where `share` opens the system sheet; elsewhere it would just copy, as the link does
            if (systemShareSupported) ShareAction(
                icon = Icons.Default.Share,
                label = stringResource(Res.string.share),
                onClick = {
                    share(context, shareText)
                    onCopied()
                },
            )
            ShareAction(
                icon = Icons.Outlined.Download,
                label = stringResource(Res.string.share_save_poster),
                onClick = onSavePoster,
            )
        }
    }
}

/** One way to share: an icon in its own tile, the name beside it. Rows stay readable at any label length. */
@Composable
private fun ShareAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            label,
            Modifier.padding(start = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
