package world.hachimi.app.ui.moment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_cancel
import hachimiworld.composeapp.generated.resources.moment_add_attachment
import hachimiworld.composeapp.generated.resources.moment_add_image_cd
import hachimiworld.composeapp.generated.resources.moment_create_title
import hachimiworld.composeapp.generated.resources.moment_discard
import hachimiworld.composeapp.generated.resources.moment_discard_text
import hachimiworld.composeapp.generated.resources.moment_discard_title
import hachimiworld.composeapp.generated.resources.moment_image_failed
import hachimiworld.composeapp.generated.resources.moment_image_rejected
import hachimiworld.composeapp.generated.resources.moment_input_placeholder
import hachimiworld.composeapp.generated.resources.moment_keep_editing
import hachimiworld.composeapp.generated.resources.moment_picker_empty
import hachimiworld.composeapp.generated.resources.moment_picker_failed
import hachimiworld.composeapp.generated.resources.moment_picker_playlist_private
import hachimiworld.composeapp.generated.resources.moment_picker_tab_playlists
import hachimiworld.composeapp.generated.resources.moment_picker_tab_recent
import hachimiworld.composeapp.generated.resources.moment_picker_title
import hachimiworld.composeapp.generated.resources.moment_publish
import hachimiworld.composeapp.generated.resources.moment_remove_cd
import hachimiworld.composeapp.generated.resources.notification_retry
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.MomentModule
import world.hachimi.app.api.module.PlaylistModule
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.model.CreateMomentViewModel
import world.hachimi.app.model.DraftAttachment
import world.hachimi.app.model.DraftImage
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.TabBar
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.message.components.RemoteImage
import world.hachimi.app.ui.square.MomentListMaxWidth
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.fillMaxWidthIn
import world.hachimi.app.ui.util.listTailPadding

/**
 * Writing a moment. Images go up as soon as they're picked; it can be posted once they're all up.
 *
 * @since 261106
 */
@Composable
fun CreateMomentScreen(vm: CreateMomentViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    var confirmDiscard by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf(false) }
    LaunchedEffect(vm) { vm.done.collect { navigator.back() } }
    val leave = { if (vm.hasDraft && !vm.publishing) confirmDiscard = true else navigator.back() }

    ScreenScaffold(
        title = { Text(stringResource(Res.string.moment_create_title), maxLines = 1) },
        showBack = true,
        onBack = leave,
        actions = {
            AccentButton(onClick = vm::publish, enabled = vm.canPublish) {
                if (vm.publishing) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(Res.string.moment_publish))
                }
            }
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .fillMaxWidthIn(maxWidth = MomentListMaxWidth)
                .padding(AdaptiveScreenMargin)
                .listTailPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextField(
                value = vm.content,
                onValueChange = { vm.content = it.take(MomentModule.CONTENT_MAX_CHARS) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.moment_input_placeholder)) },
                minLines = 5,
                enabled = !vm.publishing,
            )
            Text(
                "${vm.content.length} / ${MomentModule.CONTENT_MAX_CHARS}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                fontSize = 12.sp,
                color = HachimiTheme.colorScheme.onSurfaceVariant,
            )
            ImagesEditor(vm)
            AttachmentEditor(vm, onPick = {
                vm.loadPickerSources()
                picking = true
            })
        }
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text(stringResource(Res.string.moment_discard_title)) },
            text = { Text(stringResource(Res.string.moment_discard_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    navigator.back()
                }) { Text(stringResource(Res.string.moment_discard)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) { Text(stringResource(Res.string.moment_keep_editing)) }
            },
        )
    }
    if (picking) {
        AttachmentPicker(
            vm = vm,
            onPick = {
                vm.attachment = it
                picking = false
            },
            onDismiss = { picking = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ImagesEditor(vm: CreateMomentViewModel) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        vm.images.forEach { image ->
            DraftImageTile(image, onRemove = { vm.removeImage(image) }, onRetry = { vm.retryUpload(image) })
        }
        if (vm.canAddImages && !vm.publishing) {
            Box(
                Modifier
                    .size(TileSize)
                    .clip(TileShape)
                    .border(1.dp, HachimiTheme.colorScheme.outline, TileShape)
                    .clickable(onClick = vm::pickImages),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.AddPhotoAlternate,
                        stringResource(Res.string.moment_add_image_cd),
                        Modifier.size(28.dp),
                        tint = HachimiTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "${vm.images.size} / ${MomentModule.MAX_IMAGES}",
                        fontSize = 12.sp,
                        color = HachimiTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DraftImageTile(image: DraftImage, onRemove: () -> Unit, onRetry: () -> Unit) {
    val state = image.state
    val context = LocalPlatformContext.current
    Box(Modifier.size(TileSize).clip(TileShape)) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(image.bytes).crossfade(true).build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().background(HachimiTheme.colorScheme.onSurface.copy(0.08f)),
            contentScale = ContentScale.Crop,
        )
        when (state) {
            is DraftImage.State.Uploading -> Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(Modifier.size(28.dp), color = Color.White, strokeWidth = 3.dp, progress = { state.progress })
            }
            is DraftImage.State.Rejected -> Box(
                Modifier.fillMaxSize().background(Color(0xCCD32F2F)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(Res.string.moment_image_rejected),
                    modifier = Modifier.padding(6.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
            is DraftImage.State.Failed -> Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)).clickable(onClick = onRetry),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Refresh, null, Modifier.size(20.dp), tint = Color.White)
                    Text(
                        stringResource(Res.string.moment_image_failed),
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            is DraftImage.State.Uploaded -> {}
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Close, stringResource(Res.string.moment_remove_cd), Modifier.size(14.dp), tint = Color.White)
        }
    }
}

@Composable
private fun AttachmentEditor(vm: CreateMomentViewModel, onPick: () -> Unit) {
    val attachment = vm.attachment
    if (attachment == null) {
        TextButton(onClick = onPick, enabled = !vm.publishing) {
            Icon(Icons.Outlined.LibraryMusic, null, Modifier.size(18.dp))
            Text(stringResource(Res.string.moment_add_attachment), modifier = Modifier.padding(start = 8.dp))
        }
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) {
            when (attachment) {
                is DraftAttachment.Song -> SongRow(attachment.song, onClick = null)
                is DraftAttachment.Playlist -> PlaylistRow(attachment.playlist, onClick = null)
            }
        }
        HachimiIconButton(onClick = { vm.attachment = null }, enabled = !vm.publishing) {
            Icon(Icons.Default.Close, stringResource(Res.string.moment_remove_cd))
        }
    }
}

/** Picks a song from what the user played lately, or one of their public playlists. */
@Composable
private fun AttachmentPicker(vm: CreateMomentViewModel, onPick: (DraftAttachment) -> Unit, onDismiss: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.moment_picker_title)) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                TabBar(
                    tabs = listOf(
                        stringResource(Res.string.moment_picker_tab_recent),
                        stringResource(Res.string.moment_picker_tab_playlists),
                    ),
                    selectedIndex = tab,
                    onTabSelected = { tab = it },
                )
                Box(Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 420.dp)) {
                    val songs = vm.recentSongs
                    val playlists = vm.myPlaylists
                    val loaded = if (tab == 0) songs != null else playlists != null
                    when {
                        !loaded && vm.pickerFailed -> Column(
                            Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(stringResource(Res.string.moment_picker_failed), color = HachimiTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = vm::loadPickerSources) { Text(stringResource(Res.string.notification_retry)) }
                        }
                        !loaded -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                        tab == 0 && songs.isNullOrEmpty() || tab == 1 && playlists.isNullOrEmpty() -> Text(
                            stringResource(Res.string.moment_picker_empty),
                            modifier = Modifier.align(Alignment.Center),
                            color = HachimiTheme.colorScheme.onSurfaceVariant,
                        )
                        tab == 0 -> LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(songs.orEmpty(), key = { it.id }) { song ->
                                SongRow(song, onClick = { onPick(DraftAttachment.Song(song)) })
                            }
                        }
                        else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(playlists.orEmpty(), key = { it.id }) { playlist ->
                                val usable = playlist.isPublic && !playlist.isHidden
                                PlaylistRow(
                                    playlist,
                                    onClick = if (usable) ({ onPick(DraftAttachment.Playlist(playlist)) }) else null,
                                    note = if (usable) null else stringResource(Res.string.moment_picker_playlist_private),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) }
        },
    )
}

@Composable
private fun SongRow(song: SongModule.PublicSongDetail, onClick: (() -> Unit)?) {
    PickRow(
        image = { RemoteImage(song.coverUrl, Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))) },
        title = song.title,
        subtitle = song.uploaderName,
        onClick = onClick,
    )
}

@Composable
private fun PlaylistRow(playlist: PlaylistModule.PlaylistItem, onClick: (() -> Unit)?, note: String? = null) {
    PickRow(
        image = {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                RemoteImage(playlist.coverUrl, Modifier.size(44.dp))
                if (playlist.coverUrl == null) {
                    Icon(Icons.AutoMirrored.Outlined.QueueMusic, null, Modifier.size(22.dp), tint = HachimiTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        title = playlist.name,
        subtitle = note ?: playlist.songsCount.toString(),
        onClick = onClick,
        enabled = note == null,
    )
}

@Composable
private fun PickRow(
    image: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
    enabled: Boolean = true,
) {
    val alpha = if (enabled) 1f else 0.5f
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        image()
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = HachimiTheme.colorScheme.onSurface.copy(alpha = alpha),
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = HachimiTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
            )
        }
    }
}

private val TileSize = 96.dp
private val TileShape = RoundedCornerShape(10.dp)
