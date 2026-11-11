package world.hachimi.app.ui.playlist

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.moderation_playlist_hidden_body
import hachimiworld.composeapp.generated.resources.moderation_playlist_hidden_title
import hachimiworld.composeapp.generated.resources.playlist_private_badge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import hachimiworld.composeapp.generated.resources.common_cancel
import hachimiworld.composeapp.generated.resources.common_delete
import hachimiworld.composeapp.generated.resources.playlist_delete_confirm_text
import hachimiworld.composeapp.generated.resources.playlist_delete_confirm_title
import sh.calvin.reorderable.ReorderableItem
import androidx.compose.foundation.background
import world.hachimi.app.ui.design.HachimiTheme
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import hachimiworld.composeapp.generated.resources.common_more
import hachimiworld.composeapp.generated.resources.playlist_delete
import hachimiworld.composeapp.generated.resources.playlist_edit_title
import world.hachimi.app.ui.design.components.DropdownMenu
import world.hachimi.app.ui.design.components.DropdownMenuItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.TextButton
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.PlaylistDetailViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.component.TitleStyle
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.TagBadge
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.playlist.components.CompactHeader
import world.hachimi.app.ui.playlist.components.EditDialog
import world.hachimi.app.ui.playlist.components.Header
import world.hachimi.app.ui.playlist.components.SongItem
import world.hachimi.app.ui.playlist.components.UnavailableSongItem
import world.hachimi.app.ui.playlist.components.PlaylistEntry
import world.hachimi.app.ui.playlist.components.playlistEntries
import world.hachimi.app.ui.report.ModerationNotice
import world.hachimi.app.api.module.PlaylistModule
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.ui.util.contentPaddingForMaxWidth
import world.hachimi.app.ui.util.fadeInFadeOut
import world.hachimi.app.ui.util.listHeadInsetsSpacerItem
import world.hachimi.app.ui.util.listTailSpacerItem
import kotlin.time.Duration.Companion.seconds

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    vm: PlaylistDetailViewModel = koinViewModel(),
) {
    val navigator = LocalNavigator.current

    DisposableEffect(vm, playlistId) {
        vm.mounted(playlistId)
        onDispose {
            vm.dispose()
        }
    }

    LaunchedEffect(vm.deleted) {
        if (vm.deleted) navigator.back()
    }

    val global = koinInject<GlobalStore>()
    ScreenScaffold(
        titleStyle = TitleStyle.Revealed,
        title = { Text(vm.playlistInfo?.name.orEmpty(), maxLines = 1) },
        showBack = true,
        onBack = navigator::back,
    ) {
    AnimatedContent(
        targetState = vm.initStatus,
        transitionSpec = { fadeInFadeOut() }
    ) { initStatus ->
        when (initStatus) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> BoxWithConstraints(Modifier.fillMaxSize()) {
                vm.playlistInfo?.let { info ->
                    val listState = rememberLazyListState()
                    // The order shown, moved by hand while dragging and saved when the song is let go
                    var entries by remember(vm.songs, vm.unavailableSongs) {
                        mutableStateOf(playlistEntries(vm.songs, vm.unavailableSongs))
                    }
                    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
                        val fromIndex = entries.indexOfFirst { it.key == from.key }
                        val toIndex = entries.indexOfFirst { it.key == to.key }
                        // Only songs trade places; the header and notices stay put
                        if (fromIndex < 0 || toIndex < 0) return@rememberReorderableLazyListState
                        entries = entries.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
                    }
                    val haptics = LocalHapticFeedback.current
                    val pageColor = HachimiTheme.colorScheme.background
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = contentPaddingForMaxWidth(
                            PaddingValues(
                                AdaptiveScreenMargin
                            ), maxWidth
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listHeadInsetsSpacerItem()
                        item {
                            Header(global, info, vm)
                        }

                        if (info.isHidden) {
                            item(key = "hidden_notice") {
                                ModerationNotice(
                                    title = stringResource(Res.string.moderation_playlist_hidden_title),
                                    body = stringResource(Res.string.moderation_playlist_hidden_body),
                                    reason = vm.hiddenReason,
                                )
                            }
                        }

                        itemsIndexed(entries, key = { _, entry -> entry.key }) { index, entry ->
                            // Holding a song picks it up; the list makes room as it moves and saves where it's let go
                            ReorderableItem(reorderState, key = entry.key) { dragging ->
                                val lift by animateDpAsState(if (dragging) 8.dp else 0.dp)
                                var startIndex by remember { mutableIntStateOf(-1) }
                                val drag = Modifier.longPressDraggableHandle(
                                    onDragStarted = {
                                        startIndex = entries.indexOfFirst { it.key == entry.key }
                                        haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                    },
                                    onDragStopped = {
                                        haptics.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                        val target = entries.indexOfFirst { it.key == entry.key }
                                        if (target >= 0 && startIndex >= 0 && target != startIndex) {
                                            vm.moveSong(entry.songId, target, entries.map { it.songId })
                                        }
                                    },
                                )
                                // The row's fill is see-through, so lay the page color under it; otherwise the
                                // shadow shows through the row as a second, offset block
                                val itemModifier = Modifier.fillMaxWidth()
                                    .shadow(lift, RowShape)
                                    .background(pageColor, RowShape)
                                    .then(drag)
                                if (entry is PlaylistEntry.Unavailable) {
                                    UnavailableSongItem(
                                        modifier = itemModifier,
                                        onRemoveClick = { vm.removeFromPlaylist(entry.song.songId) },
                                    )
                                    return@ReorderableItem
                                }
                                val song = (entry as PlaylistEntry.Song).song
                                SongItem(
                                    modifier = itemModifier,
                                    orderIndex = index,
                                    title = song.title,
                                    onClick = {
                                        global.player.insertToQueue(
                                            GlobalStore.MusicQueueItem(
                                                id = song.songId,
                                                displayId = song.songDisplayId,
                                                name = song.title,
                                                artist = song.uploaderName,
                                                duration = song.durationSeconds.seconds,
                                                coverUrl = song.coverUrl,
                                                explicit = null // TODO(playlist): Get explicit info
                                            ), true, false
                                        )
                                    },
                                    coverUrl = song.coverUrl,
                                    artist = song.uploaderName,
                                    duration = song.durationSeconds.seconds,
                                    editable = true,
                                    onRemoveClick = {
                                        vm.removeFromPlaylist(song.songId)
                                    },
                                    // Long press belongs to dragging here; the menu stays on its button
                                    menuOnLongPress = false,
                                )
                            }
                        }

                        listTailSpacerItem()
                    }
                }

                if (vm.loading) CircularProgressIndicator(Modifier.align(Alignment.Center))

                EditDialog(vm)
                DeleteConfirmDialog(vm)
            }
        }
    }
    }
}

@Composable
private fun Header(
    global: GlobalStore,
    info: PlaylistModule.PlaylistItem,
    vm: PlaylistDetailViewModel
) {
    @Composable
    fun BoxScope.Overlay(hazeState: HazeState) {
        if (vm.coverUploading) {
            if (vm.coverUploadingProgress == 0f || vm.coverUploadingProgress == 1f)
                CircularProgressIndicator()
            else
                LinearProgressIndicator(progress = { vm.coverUploadingProgress })
        }
        if (!info.isPublic) {
            TagBadge(
                hazeState,
                tag = stringResource(Res.string.playlist_private_badge),
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
            )
        }
        Box(Modifier.fillMaxSize().clickable { vm.editCover() })
    }

    Column {
        val navigator = LocalNavigator.current
        val userInfo = global.userInfo
        if (LocalWindowSize.current.width < WindowSize.COMPACT) {
            CompactHeader(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                username = userInfo?.name ?: "",
                avatarUrl = userInfo?.avatarUrl,
                description = info.description,
                title = info.name,
                coverUrl = info.coverUrl,
                updateTime = info.updateTime,
                count = vm.songs.size,
                onNavToUserClick = {
                    userInfo?.uid?.let { navigator.push(Route.Root.PublicUserSpace(it)) }
                },
                onPlayAllClick = { vm.playAll() },
                coverOverlay = { hazeState ->
                    Overlay(hazeState)
                },
                action = { MoreButton(vm) }
            )
        } else {
            Header(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                username = userInfo?.name ?: "",
                avatarUrl = userInfo?.avatarUrl,
                description = info.description,
                title = info.name,
                coverUrl = info.coverUrl,
                updateTime = info.updateTime,
                count = vm.songs.size,
                onNavToUserClick = {
                    userInfo?.uid?.let { navigator.push(Route.Root.PublicUserSpace(it)) }
                },
                onPlayAllClick = { vm.playAll() },
                coverOverlay = { hazeState ->
                    Overlay(hazeState)
                },
                extraActions = { MoreButton(vm) }
            )
        }
    }
}

/** Editing and deleting the playlist, behind one button so the header keeps to playing. */
@Composable
private fun MoreButton(vm: PlaylistDetailViewModel) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }, contentPadding = PaddingValues(8.dp)) {
            Icon(Icons.Default.MoreHoriz, contentDescription = stringResource(Res.string.common_more))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    vm.edit()
                },
                text = { Text(stringResource(Res.string.playlist_edit_title)) },
                leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
            )
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    vm.showDeleteConfirm = true
                },
                text = { Text(stringResource(Res.string.playlist_delete)) },
                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
            )
        }
    }
}

@Composable
private fun DeleteConfirmDialog(vm: PlaylistDetailViewModel) {
    if (!vm.showDeleteConfirm) return
    AlertDialog(
        onDismissRequest = { vm.showDeleteConfirm = false },
        title = { Text(stringResource(Res.string.playlist_delete_confirm_title)) },
        text = { Text(stringResource(Res.string.playlist_delete_confirm_text)) },
        confirmButton = {
            TextButton(onClick = vm::confirmDelete, enabled = !vm.deleteOperating) {
                Text(stringResource(Res.string.common_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.showDeleteConfirm = false }) { Text(stringResource(Res.string.common_cancel)) }
        },
    )
}

private val RowShape = RoundedCornerShape(16.dp)
