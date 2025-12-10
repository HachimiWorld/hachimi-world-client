package world.hachimi.app.ui.player.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.PlaylistViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    if (vm.showPlaylistDialog) BasicAlertDialog(
        modifier = Modifier.width(400.dp),
        onDismissRequest = {
            vm.cancelAddToPlaylist()
            onDismiss()
        },
        content = {
            ElevatedCard(elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)) {
                Column(Modifier.padding(24.dp)) {
                    Text("收藏到歌单", style = MaterialTheme.typography.titleLarge)

                    Spacer(Modifier.height(16.dp))

                    Text("请选择一个歌单")

                    if (vm.playlistIsLoading) {
                        LinearProgressIndicator()
                    }

                    Spacer(Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { vm.createPlaylist() }
                    ) {
                        Row(Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text(
                                modifier = Modifier.padding(start = 16.dp),
                                text = "新建歌单"
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    LazyColumn(Modifier.height(200.dp).fillMaxWidth()) {
                        itemsIndexed(vm.playlists, key = { _, item -> item.id }) { index, item ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                onClick = {
                                    vm.selectedPlaylistId = item.id
                                }
                            ) {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        Modifier.size(64.dp),
                                        MaterialTheme.shapes.medium,
                                        LocalContentColor.current.copy(0.12f)
                                    ) {
                                        AsyncImage(
                                            modifier = Modifier.fillMaxSize(),
                                            model = item.coverUrl,
                                            contentDescription = "Playlist Cover",
                                            contentScale = ContentScale.Crop,
                                        )
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = item.name
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

                    Row(Modifier.align(Alignment.End)) {
                        TextButton(onClick = {
                            vm.cancelAddToPlaylist()
                            onDismiss()
                        }) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = {
                                vm.confirmAddToPlaylist()
                                onDismiss()
                            },
                            enabled = vm.selectedPlaylistId != null && !vm.addingToPlaylistOperating
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    )

    CreatePlaylistDialog(vm)
}
