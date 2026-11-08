package world.hachimi.app.ui.moment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_cancel
import hachimiworld.composeapp.generated.resources.common_delete
import hachimiworld.composeapp.generated.resources.common_more
import hachimiworld.composeapp.generated.resources.moment_attachment_unavailable
import hachimiworld.composeapp.generated.resources.moment_attachment_unknown
import hachimiworld.composeapp.generated.resources.moment_comment_cd
import hachimiworld.composeapp.generated.resources.moment_delete_confirm_text
import hachimiworld.composeapp.generated.resources.moment_delete_confirm_title
import hachimiworld.composeapp.generated.resources.moment_hidden_notice
import hachimiworld.composeapp.generated.resources.moment_like_cd
import hachimiworld.composeapp.generated.resources.moment_playlist_songs
import hachimiworld.composeapp.generated.resources.moment_show_more
import hachimiworld.composeapp.generated.resources.moment_unlike_cd
import hachimiworld.composeapp.generated.resources.report_action
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import world.hachimi.app.api.module.MomentAttachment
import world.hachimi.app.api.module.MomentModule.MomentImage
import world.hachimi.app.api.module.MomentModule.MomentItem
import world.hachimi.app.api.module.MomentModule.MomentUser
import world.hachimi.app.api.module.ReportModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.MomentStore
import world.hachimi.app.model.ReportStore
import world.hachimi.app.model.fromPublicDetail
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.AnimatedCount
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.DropdownMenu
import world.hachimi.app.ui.design.components.DropdownMenuItem
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.message.components.RemoteImage
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime
import kotlin.time.Clock

/** What a moment card can do. Made by [rememberMomentActions]. @since 261106 */
class MomentActions(
    val onOpen: (MomentItem, atComments: Boolean) -> Unit,
    val onLike: (MomentItem) -> Unit,
    val onDelete: (MomentItem) -> Unit,
    val onReport: (MomentItem) -> Unit,
    val onUserClick: (Long) -> Unit,
    val onImageClick: (MomentItem, index: Int) -> Unit,
    val onAttachmentClick: (MomentAttachment) -> Unit,
    val isMine: (MomentItem) -> Boolean,
)

/**
 * The usual [MomentActions], and where they show: the delete confirmation and the image viewer are
 * drawn here, so call it once per page.
 *
 * @param onDeleted After the user's moment is deleted, such as to leave its page.
 */
@Composable
fun rememberMomentActions(
    onDeleted: () -> Unit = {},
    store: MomentStore = koinInject(),
    reports: ReportStore = koinInject(),
    global: GlobalStore = koinInject(),
): MomentActions {
    val navigator = LocalNavigator.current
    var toDelete by remember { mutableStateOf<MomentItem?>(null) }
    var viewing by remember { mutableStateOf<Pair<List<MomentImage>, Int>?>(null) }

    toDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text(stringResource(Res.string.moment_delete_confirm_title)) },
            text = { Text(stringResource(Res.string.moment_delete_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    store.delete(item, onDeleted)
                    toDelete = null
                }) { Text(stringResource(Res.string.common_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text(stringResource(Res.string.common_cancel)) }
            },
        )
    }
    viewing?.let { (images, index) ->
        ImageViewer(images = images, initialIndex = index, onDismiss = { viewing = null })
    }

    return remember(navigator, store, reports, global) {
        MomentActions(
            onOpen = { item, atComments -> navigator.push(Route.Root.MomentDetail(item.id, atComments)) },
            onLike = store::toggleLike,
            onDelete = { toDelete = it },
            onReport = { reports.open(ReportModule.TARGET_MOMENT, it.id) },
            onUserClick = { navigator.push(Route.Root.PublicUserSpace(it)) },
            onImageClick = { item, index -> viewing = item.images to index },
            onAttachmentClick = { attachment ->
                when (attachment) {
                    is MomentAttachment.Song -> global.player.insertToQueue(
                        item = GlobalStore.MusicQueueItem.fromPublicDetail(attachment.song),
                        instantPlay = true,
                        append = false,
                    )
                    is MomentAttachment.Playlist -> navigator.push(Route.Root.PublicPlaylist(attachment.playlist.id))
                    is MomentAttachment.Unavailable, is MomentAttachment.Unknown -> {}
                }
            },
            isMine = store::isMine,
        )
    }
}

/**
 * A moment: who and when, its text, images and attachment, and its likes and comments.
 *
 * @param full Shows all of its text, selectable, for its own page, which gives the margins; a
 * list folds long text and opens the moment on a click.
 * @since 261106
 */
@Composable
fun MomentCard(
    item: MomentItem,
    actions: MomentActions,
    modifier: Modifier = Modifier,
    full: Boolean = false,
) {
    val variant = HachimiTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(if (full) Modifier else Modifier.clickable { actions.onOpen(item, false) })
            .padding(horizontal = if (full) 0.dp else 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AuthorRow(item, actions)
        if (item.isHidden) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Outlined.VisibilityOff, null, Modifier.size(14.dp), tint = variant)
                Text(stringResource(Res.string.moment_hidden_notice), fontSize = 12.sp, color = variant)
            }
        }
        if (item.content.isNotEmpty()) {
            if (full) {
                SelectionContainer {
                    Text(item.content, fontSize = 15.sp, lineHeight = 23.sp)
                }
            } else {
                FoldedText(item.content)
            }
        }
        if (item.images.isNotEmpty()) {
            ImageGrid(item.images, onClick = { actions.onImageClick(item, it) })
        }
        item.attachments.forEach { attachment ->
            AttachmentCard(attachment, onClick = { actions.onAttachmentClick(attachment) })
        }
        FooterRow(item, actions)
    }
}

@Composable
private fun AuthorRow(item: MomentItem, actions: MomentActions) {
    val variant = HachimiTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically) {
        RemoteImage(
            item.author.avatarUrl,
            Modifier.size(36.dp).clip(CircleShape).clickable { actions.onUserClick(item.author.uid) },
        )
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text(
                text = item.author.username,
                modifier = Modifier.clickable { actions.onUserClick(item.author.uid) },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatTime(item.createTime, distance = true, precise = false, fullFormat = LocalDateTime.Formats.YMD),
                fontSize = 12.sp,
                color = variant,
                maxLines = 1,
            )
        }
        MoreButton(item, actions)
    }
}

/** Long text folded to [FOLDED_LINES], with a hint that there's more. */
@Composable
private fun FoldedText(text: String) {
    var overflows by remember(text) { mutableStateOf(false) }
    Column {
        Text(
            text = text,
            fontSize = 15.sp,
            lineHeight = 23.sp,
            maxLines = FOLDED_LINES,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { overflows = it.hasVisualOverflow },
        )
        if (overflows) {
            Text(
                stringResource(Res.string.moment_show_more),
                modifier = Modifier.padding(top = 2.dp),
                fontSize = 14.sp,
                color = HachimiTheme.colorScheme.primary,
            )
        }
    }
}

/** One image at its own shape; more in rows of three (four as two by two), cut square. */
@Composable
internal fun ImageGrid(images: List<MomentImage>, onClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    if (images.size == 1) {
        val image = images.first()
        val ratio = if (image.width > 0 && image.height > 0) image.width.toFloat() / image.height else 1f
        RemoteImage(
            image.thumbUrl,
            modifier
                .widthIn(max = if (ratio >= 1f) 320.dp else 220.dp)
                .fillMaxWidth()
                .aspectRatio(ratio.coerceIn(0.6f, 1.8f))
                .clip(shape)
                .clickable { onClick(0) },
        )
        return
    }
    val columns = if (images.size == 4) 2 else 3
    Column(
        modifier.widthIn(max = if (columns == 2) 260.dp else 390.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        images.chunked(columns).forEachIndexed { row, chunk ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (column in 0 until columns) {
                    val index = row * columns + column
                    val image = chunk.getOrNull(column)
                    if (image == null) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        RemoteImage(
                            image.thumbUrl,
                            Modifier.weight(1f).aspectRatio(1f).clip(shape).clickable { onClick(index) },
                        )
                    }
                }
            }
        }
    }
}

/** A song to play, a playlist to open, or a note that it can't be shown. */
@Composable
internal fun AttachmentCard(attachment: MomentAttachment, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val variant = HachimiTheme.colorScheme.onSurfaceVariant
    val shape = RoundedCornerShape(12.dp)
    val clickable = attachment is MomentAttachment.Song || attachment is MomentAttachment.Playlist
    Row(
        modifier = modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(HachimiTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            .border(1.dp, HachimiTheme.colorScheme.outline, shape)
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when (attachment) {
            is MomentAttachment.Song -> {
                RemoteImage(attachment.song.coverUrl, Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)))
                Column(Modifier.weight(1f)) {
                    Text(attachment.song.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(attachment.song.uploaderName, fontSize = 12.sp, color = variant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.Default.PlayArrow, null, Modifier.size(24.dp), tint = HachimiTheme.colorScheme.primary)
            }
            is MomentAttachment.Playlist -> {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    RemoteImage(attachment.playlist.coverUrl, Modifier.size(48.dp))
                    if (attachment.playlist.coverUrl == null) {
                        Icon(Icons.AutoMirrored.Outlined.QueueMusic, null, Modifier.size(24.dp), tint = variant)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(attachment.playlist.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        stringResource(Res.string.moment_playlist_songs, attachment.playlist.songsCount.toString(), attachment.playlist.userName),
                        fontSize = 12.sp,
                        color = variant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            is MomentAttachment.Unavailable -> Text(
                stringResource(Res.string.moment_attachment_unavailable),
                modifier = Modifier.padding(4.dp),
                fontSize = 13.sp,
                color = variant,
            )
            is MomentAttachment.Unknown -> Text(
                stringResource(Res.string.moment_attachment_unknown),
                modifier = Modifier.padding(4.dp),
                fontSize = 13.sp,
                color = variant,
            )
        }
    }
}

@Composable
private fun FooterRow(item: MomentItem, actions: MomentActions) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CountButton(
            icon = if (item.liked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
            contentDescription = stringResource(if (item.liked) Res.string.moment_unlike_cd else Res.string.moment_like_cd),
            count = item.likeCount.toLong(),
            active = item.liked,
            onClick = { actions.onLike(item) },
        )
        CountButton(
            icon = Icons.AutoMirrored.Outlined.Comment,
            contentDescription = stringResource(Res.string.moment_comment_cd),
            count = item.commentCount,
            active = false,
            onClick = { actions.onOpen(item, true) },
        )
    }
}

@Composable
private fun CountButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    count: Long,
    active: Boolean,
    onClick: () -> Unit,
) {
    val color by animateColorAsState(if (active) HachimiTheme.colorScheme.primary else HachimiTheme.colorScheme.onSurfaceVariant)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription, Modifier.size(18.dp), tint = color)
        AnimatedVisibility(
            visible = count > 0,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            // Stays at 1 while it animates out at 0
            AnimatedCount(count.coerceAtLeast(1)) {
                Text(text = it.toString(), fontSize = 13.sp, color = color)
            }
        }
    }
}

@Composable
private fun MoreButton(item: MomentItem, actions: MomentActions) {
    val mine = actions.isMine(item)
    Box {
        var expanded by remember { mutableStateOf(false) }
        HachimiIconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = stringResource(Res.string.common_more),
                modifier = Modifier.size(18.dp),
                tint = HachimiTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            if (mine) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        actions.onDelete(item)
                    },
                    text = { Text(stringResource(Res.string.common_delete)) },
                    leadingIcon = { Icon(Icons.Outlined.Delete, null) },
                )
            } else {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        actions.onReport(item)
                    },
                    text = { Text(stringResource(Res.string.report_action)) },
                    leadingIcon = { Icon(Icons.Outlined.Flag, null) },
                )
            }
        }
    }
}

private const val FOLDED_LINES = 6

internal val previewActions = MomentActions(
    onOpen = { _, _ -> }, onLike = {}, onDelete = {}, onReport = {}, onUserClick = {},
    onImageClick = { _, _ -> }, onAttachmentClick = {}, isMine = { false },
)

internal fun previewMoment(images: Int = 3, content: String = "今天写了一首新歌，大家听听看！\n第二行文字。") = MomentItem(
    id = 1,
    author = MomentUser(1, "Hachimi", null),
    content = content,
    images = List(images) { MomentImage("", "", 800, 600) },
    attachments = listOf(MomentAttachment.Unavailable(MomentAttachment.TYPE_SONG)),
    likeCount = 12,
    liked = true,
    commentCount = 3,
    isHidden = false,
    createTime = Clock.System.now(),
)

@Preview
@Composable
private fun MomentCardPreview() {
    PreviewTheme(background = true) {
        MomentCard(previewMoment(), previewActions)
    }
}

@Preview
@Composable
private fun MomentCardOneImagePreview() {
    PreviewTheme(background = true) {
        MomentCard(previewMoment(images = 1), previewActions)
    }
}
