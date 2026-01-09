package world.hachimi.app.ui.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.PlaylistViewModel
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LinearProgressIndicator
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton

@Composable
fun AddToPlaylistDialog(
    tobeAddedSongId: Long?,
    random: Long?,
    onDismiss: () -> Unit,
    vm: PlaylistViewModel = koinViewModel(),
) {
    LaunchedEffect(vm, tobeAddedSongId, random) {
        if (tobeAddedSongId != null) {
            vm.toBeAddedSongId = tobeAddedSongId
            vm.addToPlaylist()
        }
    }

    if (vm.showPlaylistDialog) AlertDialog(
        modifier = Modifier.width(400.dp),
        onDismissRequest = {
            vm.cancelAddToPlaylist()
            onDismiss()
        },
        title = {
            Text("收藏到歌单")
        },
        text = {
            if (vm.playlistIsLoading) {
                LinearProgressIndicator()
            }
            LazyColumn(Modifier.padding(top = 8.dp).height(400.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { vm.createPlaylist() },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text(
                                modifier = Modifier.padding(start = 16.dp),
                                text = "新建歌单"
                            )
                        }
                    }
                }
                items(vm.playlists, key = { item -> item.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { vm.selectedPlaylistId = item.id },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                Modifier.size(64.dp).padding(8.dp),
                                RoundedCornerShape(8.dp),
                                LocalContentColor.current.copy(0.12f)
                            ) {
                                AsyncImage(
                                    modifier = Modifier.fillMaxSize(),
                                    model = ImageRequest.Builder(LocalPlatformContext.current)
                                        .data(item.coverUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Playlist Cover",
                                    contentScale = ContentScale.Crop,
                                )
                            }
                            Text(
                                modifier = Modifier.weight(1f).padding(start = 8.dp),
                                text = item.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (vm.selectedPlaylistId == item.id) {
                                Icon(
                                    modifier = Modifier.padding(end = 12.dp),
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected"
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    vm.confirmAddToPlaylist()
                    onDismiss()
                },
                enabled = vm.selectedPlaylistId != null && !vm.addingToPlaylistOperating
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                vm.cancelAddToPlaylist()
                onDismiss()
            }) {
                Text("取消")
            }
        }
    )

    CreatePlaylistDialog(vm)
}
