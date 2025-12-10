package world.hachimi.app.ui.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.PlaylistDetailViewModel
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.util.formatSongDuration
import world.hachimi.app.util.singleLined
import kotlin.time.Duration
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
        InitializeStatus.LOADED -> Box(Modifier.fillMaxSize()) {
            vm.playlistInfo?.let { info ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row {
                            Card(
                                modifier = Modifier.size(128.dp),
                                onClick = { vm.editCover() },
                                enabled = !vm.coverUploading
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    AsyncImage(
                                        modifier = Modifier.fillMaxSize(),
                                        model = info.coverUrl,
                                        contentDescription = "Playlist Cover Image",
                                        contentScale = ContentScale.Crop
                                    )
                                    if (vm.coverUploading) {
                                        if (vm.coverUploadingProgress == 0f || vm.coverUploadingProgress == 1f) CircularProgressIndicator()
                                        else CircularProgressIndicator(progress = { vm.coverUploadingProgress })
                                    }
                                    if (!info.isPublic) {
                                        Card(
                                            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                                            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text(
                                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                                                text = "私有", style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f).padding(start = 24.dp).height(120.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable(onClick = { vm.edit() })
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = info.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    text = info.description ?: "暂无介绍",
                                    style = MaterialTheme.typography.bodySmall,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }


                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "歌曲列表", style = MaterialTheme.typography.titleLarge
                            )

                            Text(
                                modifier = Modifier.padding(start = 16.dp),
                                text = "${vm.songs.size} 首",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(Modifier.weight(1f))
                            Button(
                                modifier = Modifier,
                                onClick = { vm.playAll() }
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                                Spacer(Modifier.width(16.dp))
                                Text("播放全部")
                            }
                        }
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
                            onRemoveClick = {
                                vm.removeFromPlaylist(song.songId)
                            }
                        )
                    }

                    item {
                        Spacer(Modifier.navigationBarsPadding().windowInsetsBottomHeight(LocalContentInsets.current))
                    }
                }
            }

            if (vm.loading) CircularProgressIndicator(Modifier.align(Alignment.Center))

            EditDialog(vm)
        }
    }
}

@Composable
private fun SongItem(
    orderIndex: Int,
    coverUrl: String,
    title: String,
    artist: String,
    duration: Duration,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Card(modifier = modifier) {
        Row(
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = { dropdownExpanded = true }
            ).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "#" + (orderIndex + 1).toString(),
                style = MaterialTheme.typography.bodyMedium
            )

            Box(Modifier.size(48.dp).clip(MaterialTheme.shapes.small)) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "Song Cover Image",
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium)
                Text(artist, style = MaterialTheme.typography.bodySmall)
            }

            Text(formatSongDuration(duration), style = MaterialTheme.typography.bodySmall)

            Box {
                IconButton(onClick = { dropdownExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Dropdown")
                }
                DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                    DropdownMenuItem(onClick = {
                        onRemoveClick()
                        dropdownExpanded = false
                    }, text = {
                        Text("移出歌单")
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDialog(vm: PlaylistDetailViewModel) {
    if (vm.showEditDialog) BasicAlertDialog(
        onDismissRequest = { vm.cancelEdit() }
    ) {
        ElevatedCard(elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)) {

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "编辑歌单信息", style = MaterialTheme.typography.titleLarge)
                TextField(
                    value = vm.editName,
                    onValueChange = { vm.editName = it.singleLined() },
                    label = { Text("名称") },
                    singleLine = true
                )
                TextField(
                    value = vm.editDescription,
                    onValueChange = { vm.editDescription = it },
                    label = { Text("描述") },
                    minLines = 3,
                    maxLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("私有歌单")
                    Switch(
                        modifier = Modifier.padding(start = 16.dp),
                        checked = vm.editPrivate,
                        onCheckedChange = { vm.editPrivate = it }
                    )
                }

                Row(Modifier.align(Alignment.End)) {
                    TextButton(onClick = { vm.cancelEdit() }) {
                        Text("取消")
                    }
                    TextButton(
                        onClick = { vm.confirmEdit() },
                        enabled = vm.editName.isNotBlank() && !vm.editOperating
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}