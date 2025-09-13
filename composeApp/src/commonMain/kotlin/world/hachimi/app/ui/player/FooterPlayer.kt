package world.hachimi.app.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PlaylistViewModel
import world.hachimi.app.ui.player.components.SongControl
import world.hachimi.app.ui.player.components.SongProgress
import world.hachimi.app.ui.root.component.MusicQueue
import kotlin.random.Random

@Composable
fun FooterPlayer() {
    val global = koinInject<GlobalStore>()
    val playerState = global.playerState
    Surface(shadowElevation = 2.dp) {
        BoxWithConstraints {
            AnimatedVisibility(
                visible = playerState.hasSong || playerState.isFetching,
                modifier = Modifier.fillMaxWidth(),
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically()
            ) {
                if (maxWidth < 600.dp) {
                    CompactFooterPlayer(Modifier.fillMaxWidth())
                } else {
                    ExpandedFooterPlayer()
                }
            }
        }
    }
}

@Composable
fun CompactFooterPlayer(modifier: Modifier) {
    val global = koinInject<GlobalStore>()
    val playerState = global.playerState
    Column(modifier.clickable(onClick = { global.expandPlayer() }).padding(horizontal = 24.dp, vertical = 12.dp)) {
        Row(Modifier.height(60.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.aspectRatio(1f),
                shape = MaterialTheme.shapes.medium,
                color = LocalContentColor.current.copy(0.12f)
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = playerState.songCoverUrl,
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(playerState.songTitle, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text(playerState.songAuthor, style = MaterialTheme.typography.bodySmall)
            }

            var queueExpanded by remember { mutableStateOf(false) }

            // TODO[refactor](footer): I really should not write this garbage. Refactor later.
            var tobeAddedSong by remember { mutableStateOf<Pair<Long, Long>?>(null) }

            IconButton(onClick = {
                tobeAddedSong = playerState.songId?.let { it to Random.nextLong() }
            }) {
                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, "Add To Playlist")
            }

            IconButton(onClick = { queueExpanded = true }) {
                Icon(Icons.AutoMirrored.Filled.QueueMusic, "Queue")
            }

            if (queueExpanded) Popup(
                alignment = Alignment.CenterEnd,
                onDismissRequest = { queueExpanded = false }
            ) {
                MusicQueue(onClose = { queueExpanded = false })
            }

            AddToPlaylistDialog(tobeAddedSong?.first, tobeAddedSong?.second)
            CreatePlaylistDialog()
        }

        SongProgress(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            durationMillis = playerState.songDurationSecs * 1000L,
            currentMillis = playerState.currentMillis,
            onProgressChange = {
                global.setSongProgress(it)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedFooterPlayer() {
    val global = koinInject<GlobalStore>()
    val playerState = global.playerState


    Row(Modifier.height(120.dp).padding(horizontal = 24.dp, vertical = 12.dp)) {
        Surface(
            modifier = Modifier.aspectRatio(1f),
            shape = MaterialTheme.shapes.medium,
            onClick = { global.expandPlayer() },
            color = LocalContentColor.current.copy(0.12f)
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = playerState.songCoverUrl,
                contentDescription = "Cover",
                contentScale = ContentScale.Crop
            )
        }

        Column(Modifier.padding(start = 16.dp).width(200.dp)) {
            Text(playerState.songTitle, style = MaterialTheme.typography.bodyMedium)
            Text(playerState.songAuthor, style = MaterialTheme.typography.bodySmall)
        }

        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            SongControl(
                modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
                isPlaying = playerState.isPlaying,
                isLoading = playerState.isBuffering,
                loadingProgress = playerState.downloadProgress,
                onPlayPauseClick = { global.playOrPause() },
                onPreviousClick = { global.queuePrevious() },
                onNextClick = { global.queueNext() }
            )

            Spacer(Modifier.height(12.dp))

            SongProgress(
                modifier = Modifier.fillMaxWidth().widthIn(max = 800.dp),
                durationMillis = playerState.songDurationSecs * 1000L,
                currentMillis = playerState.currentMillis,
                onProgressChange = {
                    global.setSongProgress(it)
                }
            )
        }

        var queueExpanded by remember { mutableStateOf(false) }

        // TODO[refactor](footer): I really should not write this garbage. Refactor later.
        var tobeAddedSong by remember { mutableStateOf<Pair<Long, Long>?>(null) }

        IconButton(onClick = {
            tobeAddedSong = playerState.songId?.let { it to Random.nextLong() }
        }) {
            Icon(Icons.AutoMirrored.Filled.PlaylistAdd, "Add To Playlist")
        }

        IconButton(onClick = { queueExpanded = true }) {
            Icon(Icons.AutoMirrored.Filled.QueueMusic, "Queue")
        }

        if (queueExpanded) Popup(
            alignment = Alignment.CenterEnd,
            onDismissRequest = { queueExpanded = false }
        ) {
            MusicQueue(onClose = {
                queueExpanded = false
            })
        }

        AddToPlaylistDialog(tobeAddedSong?.first, tobeAddedSong?.second)
        CreatePlaylistDialog()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToPlaylistDialog(
    tobeAddedSongId: Long?,
    random: Long?,
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
                        TextButton(onClick = { vm.cancelAddToPlaylist() }) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = { vm.confirmAddToPlaylist() },
                            enabled = vm.selectedPlaylistId != null && !vm.addingToPlaylistOperating
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun CreatePlaylistDialog(vm: PlaylistViewModel = koinViewModel()) {
    if (vm.showCreatePlaylistDialog) AlertDialog(
        title = { Text("新建歌单") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = vm.createPlaylistName,
                    onValueChange = { vm.createPlaylistName = it },
                    label = { Text("名称") },
                    singleLine = true
                )
                TextField(
                    value = vm.createPlaylistDescription,
                    onValueChange = { vm.createPlaylistDescription = it },
                    label = { Text("描述") },
                    minLines = 3,
                    maxLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("私有歌单")
                    Switch(
                        modifier = Modifier.padding(start = 16.dp),
                        checked = vm.createPlaylistPrivate,
                        onCheckedChange = { vm.createPlaylistPrivate = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { vm.confirmCreatePlaylist() },
                enabled = vm.createPlaylistName.isNotBlank() && !vm.createPlaylistOperating
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.cancelCreatePlaylist() }) {
                Text("取消")
            }
        },
        onDismissRequest = {
            vm.cancelCreatePlaylist()
        }
    )
}