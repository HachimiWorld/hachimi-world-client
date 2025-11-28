package world.hachimi.app.ui.player

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PlaylistViewModel
import world.hachimi.app.ui.LocalAnimatedVisibilityScope
import world.hachimi.app.ui.LocalSharedTransitionScope
import world.hachimi.app.ui.SharedTransitionKeys
import world.hachimi.app.ui.insets.currentSafeAreaInsets
import world.hachimi.app.ui.player.components.SongControl
import world.hachimi.app.ui.player.components.SongProgress
import world.hachimi.app.ui.player.components.VolumeControl
import world.hachimi.app.ui.root.component.MusicQueue
import world.hachimi.app.util.WindowSize
import world.hachimi.app.util.singleLined
import kotlin.random.Random

@Composable
fun FooterPlayer() {
    val global = koinInject<GlobalStore>()
    val playerState = global.player.playerState
    Surface(shadowElevation = 2.dp) {
        BoxWithConstraints {
            AnimatedVisibility(
                visible = playerState.hasSong || playerState.fetchingMetadata,
                modifier = Modifier.fillMaxWidth(),
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically()
            ) {
                if (maxWidth < WindowSize.MEDIUM) {
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
    val playerState = global.player.playerState
    // TODO: Remove these, use new UI/UX to indicate loading status
    val displayedCover by remember { derivedStateOf { if (playerState.fetchingMetadata) playerState.previewMetadata?.coverUrl else playerState.songInfo?.coverUrl } }
    val displayedTitle by remember { derivedStateOf { if (playerState.fetchingMetadata) { playerState.previewMetadata?.title } else { playerState.songInfo?.title } ?: "" } }
    val displayedAuthor by remember { derivedStateOf { if (playerState.fetchingMetadata) { playerState.previewMetadata?.author } else { playerState.songInfo?.uploaderName } ?: "" } }
    val explicit by remember { derivedStateOf { if (playerState.fetchingMetadata) playerState.previewMetadata?.explicit else playerState.songInfo?.explicit } }
    with(LocalSharedTransitionScope.current) {
        AnimatedVisibility(
            !global.playerExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier
                    .sharedBounds(
                        rememberSharedContentState(SharedTransitionKeys.Bounds),
                        this,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                    .clickable(onClick = { global.expandPlayer() })
            ) {
                Surface(shadowElevation = 4.dp) {
                    Column(
                        Modifier.padding(bottom = currentSafeAreaInsets().bottom)
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Row(Modifier.height(60.dp), verticalAlignment = Alignment.CenterVertically) {
                            CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@AnimatedVisibility) {
                                Cover(global, displayedCover)
                            }

                            Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        displayedTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (explicit == true) Icon(
                                        imageVector = Icons.Default.Explicit,
                                        contentDescription = "Explicit",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(start = 4.dp).requiredSize(16.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(displayedAuthor, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }

                            var queueExpanded by remember { mutableStateOf(false) }

                            // TODO[refactor](footer): I really should not write this garbage. Refactor later.
                            var tobeAddedSong by remember { mutableStateOf<Pair<Long, Long>?>(null) }

                            IconButton(onClick = {
                                tobeAddedSong = playerState.songInfo?.id?.let { it to Random.nextLong() }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, "Add To Playlist")
                            }

                            IconButton(onClick = { queueExpanded = true }) {
                                Icon(Icons.AutoMirrored.Filled.QueueMusic, "Queue")
                            }

                            if (queueExpanded) Popup(
                                alignment = Alignment.CenterEnd,
                                onDismissRequest = { queueExpanded = false },
                                properties = PopupProperties(focusable = true)
                            ) {
                                MusicQueue(
                                    onClose = { queueExpanded = false },
                                    queue = global.player.musicQueue,
                                    playingSongId = if (playerState.fetchingMetadata) playerState.fetchingSongId else playerState.songInfo?.id,
                                    onPlayClick = { global.player.playSongInQueue(it) },
                                    onRemoveClick = { global.player.removeFromQueue(it) },
                                    onClearClick = { global.player.clearQueue() }
                                )
                            }

                            AddToPlaylistDialog(tobeAddedSong?.first, tobeAddedSong?.second)
                            CreatePlaylistDialog()
                        }

                        /*SongProgress(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        durationMillis = if (playerState.fetchingMetadata) {
                            previewMetadata?.duration?.inWholeMilliseconds
                        } else {
                            info?.durationSeconds?.let {
                                it * 1000L
                            }
                        } ?: -1L,
                        currentMillis = playerState.currentMillis,
                        onProgressChange = {
                            global.player.setSongProgress(it)
                        }
                    )*/
                    }
                }
                val animatedProgress by animateFloatAsState(targetValue = playerState.downloadProgress)
                val showProgress = animatedProgress > 0f && animatedProgress < 1f

                if (playerState.fetchingMetadata || playerState.buffering) Crossfade(showProgress) {
                    if (it) LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        progress = { animatedProgress },
                        strokeCap = StrokeCap.Square,
                        color = MaterialTheme.colorScheme.primary.copy(0.6f)
                    ) else LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        strokeCap = StrokeCap.Square,
                        color = MaterialTheme.colorScheme.primary.copy(0.6f)
                    )
                } else {
                    val durationMillis = if (playerState.fetchingMetadata) {
                        playerState.previewMetadata?.duration?.inWholeMilliseconds
                    } else {
                        playerState.songInfo?.durationSeconds?.let {
                            it * 1000L
                        }
                    } ?: -1L

                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        progress = {
                            (playerState.currentMillis.toFloat() / durationMillis).coerceIn(0f, 1f)
                        },
                        strokeCap = StrokeCap.Square,
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedFooterPlayer() {
    val global = koinInject<GlobalStore>()
    val player = global.player
    val playerState = global.player.playerState
    val displayedCover by remember { derivedStateOf { if (playerState.fetchingMetadata) playerState.previewMetadata?.coverUrl else playerState.songInfo?.coverUrl } }
    val displayedTitle by remember { derivedStateOf { if (playerState.fetchingMetadata) { playerState.previewMetadata?.title } else { playerState.songInfo?.title } ?: "" } }
    val displayedAuthor by remember { derivedStateOf { if (playerState.fetchingMetadata) { playerState.previewMetadata?.author } else { playerState.songInfo?.uploaderName } ?: "" } }
    val explicit by remember { derivedStateOf { if (playerState.fetchingMetadata) playerState.previewMetadata?.explicit else playerState.songInfo?.explicit } }
    var queueExpanded by remember { mutableStateOf(false) }

    // TODO[refactor](footer): I really should not write this garbage. Refactor later.
    var tobeAddedSong by remember { mutableStateOf<Pair<Long, Long>?>(null) }

    with(LocalSharedTransitionScope.current) {
        AnimatedVisibility(
            !global.playerExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                Modifier.sharedBounds(
                    rememberSharedContentState(SharedTransitionKeys.Bounds),
                    this,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                ).padding(bottom = currentSafeAreaInsets().bottom).height(120.dp)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                // Cover
                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@AnimatedVisibility) {
                    Cover(global, displayedCover)
                }

                Column(Modifier.padding(start = 16.dp).width(200.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            displayedTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (explicit == true) Icon(
                            imageVector = Icons.Default.Explicit,
                            contentDescription = "Explicit",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 4.dp).requiredSize(16.dp)
                        )
                    }
                    Text(displayedAuthor, style = MaterialTheme.typography.bodySmall)
                }

                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally)) {
                        SongControl(
                            modifier = Modifier.weight(1f).wrapContentWidth(),
                            isPlaying = playerState.isPlaying,
                            isLoading = playerState.buffering,
                            loadingProgress = { playerState.downloadProgress },
                            onPlayPauseClick = { global.player.playOrPause() },
                            onPreviousClick = { global.player.previous() },
                            onNextClick = { global.player.next() },
                            shuffle = player.shuffleMode,
                            onShuffleModeChange = {
                                player.updateShuffleMode(it)
                            },
                            repeat = player.repeatMode,
                            onRepeatModeChange = {
                                player.updateRepeatMode(it)
                            }
                        )

                        IconButton(onClick = {
                            tobeAddedSong = playerState.songInfo?.id?.let { it to Random.nextLong() }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.PlaylistAdd, "Add To Playlist")
                        }

                        IconButton(onClick = { queueExpanded = true }) {
                            Icon(Icons.AutoMirrored.Filled.QueueMusic, "Queue")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth()) {
                        SongProgress(
                            modifier = Modifier.widthIn(max = 800.dp).weight(1f),
                            durationMillis = if (playerState.fetchingMetadata) {
                                playerState.previewMetadata?.duration?.inWholeMilliseconds
                            } else {
                                playerState.songInfo?.durationSeconds?.let {
                                    it * 1000L
                                }
                            } ?: -1L,
                            currentMillis = if (playerState.fetchingMetadata) {
                                0L
                            } else {
                                playerState.currentMillis
                            },
                            onProgressChange = {
                                global.player.setSongProgress(it)
                            }
                        )

                        Spacer(Modifier.width(16.dp))

                        VolumeControl(
                            volume = playerState.volume,
                            onVolumeChange = { global.player.updateVolume(it) },
                        )
                    }
                }

                if (queueExpanded) Popup(
                    alignment = Alignment.CenterEnd,
                    onDismissRequest = { queueExpanded = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    MusicQueue(
                        onClose = { queueExpanded = false },
                        queue = global.player.musicQueue,
                        playingSongId = if (playerState.fetchingMetadata) playerState.fetchingSongId else playerState.songInfo?.id,
                        onPlayClick = { global.player.playSongInQueue(it) },
                        onRemoveClick = { global.player.removeFromQueue(it) },
                        onClearClick = { global.player.clearQueue() },
                    )
                }

                AddToPlaylistDialog(tobeAddedSong?.first, tobeAddedSong?.second)
                CreatePlaylistDialog()
            }
        }
    }
}

@Composable
private fun Cover(global: GlobalStore, displayedCover: String?) {
    with(LocalSharedTransitionScope.current) {
        Surface(
            modifier = Modifier
                .sharedElement(
                    rememberSharedContentState(SharedTransitionKeys.Cover),
                    LocalAnimatedVisibilityScope.current
                )
                .aspectRatio(1f),
            shape = MaterialTheme.shapes.medium,
            onClick = { global.expandPlayer() },
            color = LocalContentColor.current.copy(0.12f)
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = displayedCover,
                contentDescription = "Cover",
                contentScale = ContentScale.Crop
            )
        }
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
                    onValueChange = { vm.createPlaylistName = it.singleLined() },
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