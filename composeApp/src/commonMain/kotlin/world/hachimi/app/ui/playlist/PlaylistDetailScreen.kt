package world.hachimi.app.ui.playlist

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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.playlist_private_badge
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.PlaylistModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.PlaylistDetailViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.TagBadge
import world.hachimi.app.ui.playlist.components.CompactHeader
import world.hachimi.app.ui.playlist.components.EditDialog
import world.hachimi.app.ui.playlist.components.Header
import world.hachimi.app.ui.playlist.components.SongItem
import world.hachimi.app.ui.util.listTailSpacerItem
import world.hachimi.app.util.AdaptiveScreenMargin
import world.hachimi.app.util.WindowSize
import world.hachimi.app.util.contentPaddingForMaxWidth
import kotlin.time.Duration.Companion.seconds

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    vm: PlaylistDetailViewModel = koinViewModel(),
) {
    DisposableEffect(vm, playlistId) {
        vm.mounted(playlistId)
        onDispose {
            vm.dispose()
        }
    }

    val global = koinInject<GlobalStore>()
    when (vm.initStatus) {
        InitializeStatus.INIT -> LoadingPage()
        InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
        InitializeStatus.LOADED -> BoxWithConstraints(Modifier.fillMaxSize()) {
            vm.playlistInfo?.let { info ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPaddingForMaxWidth(PaddingValues(AdaptiveScreenMargin), maxWidth),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Header(global, info, vm)
                    }

                    itemsIndexed(vm.songs, key = { _, item -> item.songId }) { index, song ->
                        SongItem(
                            modifier = Modifier.fillMaxWidth(),
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
                            }
                        )
                    }

                    listTailSpacerItem()
                }
            }

            if (vm.loading) CircularProgressIndicator(Modifier.align(Alignment.Center))

            EditDialog(vm)
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
                action = {
                    Button(
                        onClick = { vm.edit() },
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Edit")
                    }
                }
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
                extraActions = {
                    Button(
                        onClick = { vm.edit() },
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Edit")
                    }
                }
            )
        }
    }
}
