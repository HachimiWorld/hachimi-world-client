package world.hachimi.app.ui.player.footer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil3.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import org.koin.compose.koinInject
import soup.compose.material.motion.animation.materialSharedAxisYIn
import soup.compose.material.motion.animation.materialSharedAxisYOut
import soup.compose.material.motion.animation.rememberSlideDistance
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.LocalAnimatedVisibilityScope
import world.hachimi.app.ui.LocalSharedTransitionScope
import world.hachimi.app.ui.SharedTransitionKeys
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.*
import world.hachimi.app.ui.player.components.AddToPlaylistDialog
import world.hachimi.app.ui.player.components.CreatePlaylistDialog
import world.hachimi.app.ui.player.components.PlayerProgress
import world.hachimi.app.ui.player.footer.components.Author
import world.hachimi.app.ui.player.footer.components.Title
import world.hachimi.app.ui.player.fullscreen.components.MusicQueue
import kotlin.random.Random

@Composable
fun ExpandedFooterPlayer2(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    global: GlobalStore = koinInject(),
) {
    val uiState = global.player.playerState
    var tobeAddedSong by remember { mutableStateOf<Pair<Long, Long>?>(null) }
    var musicQueueExpanded by remember { mutableStateOf(false) }

    AnimatedVisibility(visible = !global.playerExpanded) {
        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@AnimatedVisibility) {
            Container(
                modifier = modifier.requiredWidthIn(min = 400.dp).height(104.dp),
                hazeState = hazeState,
                content = {
                    FooterPlayerLayout {
                        Cover(
                            modifier = Modifier.layoutId("cover").padding(8.dp),
                            model = uiState.displayedCover,
                            onClick = { global.expandPlayer() }
                        )
                        Column(
                            modifier = Modifier.layoutId("info").padding(top = 8.dp, start = 24.dp)
                        ) {
                            Title(uiState.displayedTitle)
                            Author(uiState.displayedAuthor)
                        }
                        ControlButton(
                            modifier = Modifier.layoutId("control").padding(top = 8.dp),
                            playing = uiState.isPlaying,
                            onPreviousClick = { global.player.previous() },
                            onNextClick = { global.player.next() },
                            onPlayClick = { global.player.playOrPause() },
                            onPauseClick = { global.player.playOrPause() }
                        )
                        PlayerProgress(
                            modifier = Modifier.layoutId("progress").padding(start = 24.dp, bottom = 6.dp),
                            durationMillis = uiState.displayedDurationMillis,
                            currentMillis = uiState.displayedCurrentMillis,
                            bufferingProgress = uiState.downloadProgress,
                            onProgressChange = { global.player.setSongProgress(it) }
                        )
                        VolumeControl(
                            modifier = Modifier.layoutId("volume-control")
                                .padding(start = 24.dp, end = 24.dp, bottom = 6.dp),
                            volume = uiState.volume,
                            onVolumeChange = { global.player.updateVolume(it) }
                        )
                        FunctionButtons(
                            modifier = Modifier.layoutId("function-buttons").padding(top = 8.dp, end = 8.dp),
                            shuffleOn = global.player.shuffleMode,
                            repeatOn = global.player.repeatMode,
                            onShuffleChange = { global.player.updateShuffleMode(it) },
                            onRepeatChange = { global.player.updateRepeatMode(it) },
                            onAddToPlaylistClick = {
                                tobeAddedSong = uiState.readySongInfo?.id?.let { it to Random.nextLong() }
                            },
                            onMusicQueueClick = { musicQueueExpanded = true },
                            onOpenInFullClick = { global.expandPlayer() },
                        )
                    }
                },
                shape = RoundedCornerShape(size = 24.dp)
            )
        }
    }

    // TODO: Extract this dialog to global scope
    if (!global.playerExpanded) {
        AddToPlaylistDialog(tobeAddedSong?.first, tobeAddedSong?.second)
        CreatePlaylistDialog()
    }

    MusicQueuePopup(
        expanded = musicQueueExpanded,
        onDismissRequest = { musicQueueExpanded = false }
    ) {
        Card(
            modifier = Modifier.size(400.dp, 400.dp),
            hazeState = hazeState
        ) {
            MusicQueue(
                modifier = Modifier.padding(16.dp),
                queue = global.player.musicQueue,
                playingSongId = if (global.player.playerState.fetchingMetadata) global.player.playerState.fetchingSongId else global.player.playerState.songInfo?.id,
                onPlayClick = { global.player.playSongInQueue(it) },
                onRemoveClick = { global.player.removeFromQueue(it) },
                onClearClick = { global.player.clearQueue() },
            )
        }
    }
}

@Composable
private fun FooterPlayerLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val map = measurables.associateBy { it.layoutId }
        val cover = map["cover"]!!
        val info = map["info"]!!
        val control = map["control"]!!
        val progress = map["progress"]!!
        val volumeControl = map["volume-control"]!!
        val functionButton = map["function-buttons"]!!

        val coverPlaceable = cover.measure(constraints)
        val volumeControlPlaceable = volumeControl.measure(constraints)
        val functionButtonPlaceable = functionButton.measure(constraints)

        val progressPlaceable = progress.measure(
            constraints.copy(
                maxWidth = (constraints.maxWidth - coverPlaceable.width - volumeControlPlaceable.width).coerceAtLeast(0)
            )
        )
        val controlPlaceable = control.measure(constraints)
        val controlX = constraints.maxWidth / 2 - controlPlaceable.width / 2

        val infoPlaceable = info.measure(
            constraints.copy(
                maxWidth = (controlX - coverPlaceable.width).coerceAtLeast(0)
            )
        )

        layout(constraints.maxWidth, constraints.maxHeight) {
            coverPlaceable.place(0, 0)
            controlPlaceable.place(controlX, 0)
            progressPlaceable.place(
                x = coverPlaceable.width,
                y = constraints.maxHeight - progressPlaceable.height,
            )
            infoPlaceable.place(
                x = coverPlaceable.width,
                y = 0
            )
            volumeControlPlaceable.place(
                x = constraints.maxWidth - volumeControlPlaceable.width,
                y = constraints.maxHeight - volumeControlPlaceable.height,
            )
            functionButtonPlaceable.place(
                x = constraints.maxWidth - functionButtonPlaceable.width,
                y = 0,
            )
        }
    }
}

@Composable
private fun Cover(
    model: Any?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    with(LocalSharedTransitionScope.current) {
        Box(
            modifier
                .sharedElement(
                    rememberSharedContentState(SharedTransitionKeys.Cover),
                    LocalAnimatedVisibilityScope.current
                )
                .size(88.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray)
                .clickable(interactionSource = interactionSource) { onClick() }
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = model,
                contentDescription = "Cover",
                contentScale = ContentScale.Crop
            )
            if (hovered) Box(Modifier.fillMaxSize().background(Color.Black.copy(0.25f))) {
                Icon(
                    modifier = Modifier.align(Alignment.Center),
                    imageVector = Icons.Default.OpenInFull, contentDescription = "Expand",
                    tint = Color.White.copy(0.87f)
                )
            }
        }
    }
}

@Composable
private fun ControlButton(
    modifier: Modifier,
    playing: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    Row(modifier) {
        PreviousButton(onClick = onPreviousClick)
        Spacer(Modifier.width(8.dp))
        PlayPauseButton(
            modifier = Modifier.size(78.dp),
            playing = playing,
            onClick = {
                if (playing) onPauseClick()
                else onPlayClick()
            }
        )
        Spacer(Modifier.width(8.dp))
        NextButton(onClick = onNextClick)
    }
}

@Composable
private fun PreviousButton(
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier.height(78.dp),
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.padding(16.dp),
            imageVector = Icons.Default.SkipPrevious,
            contentDescription = "Skip Previous",
            tint = HachimiTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun NextButton(
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier.height(78.dp),
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.padding(16.dp),
            imageVector = Icons.Default.SkipNext,
            contentDescription = "Skip Next",
            tint = HachimiTheme.colorScheme.onSurface
        )
    }
}


@Composable
private fun VolumeControl(
    modifier: Modifier,
    volume: Float,
    onVolumeChange: (Float) -> Unit
) {
    Column(modifier.width(160.dp)) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = when {
                volume <= 0f -> Icons.AutoMirrored.Filled.VolumeMute
                volume <= 0.5f -> Icons.AutoMirrored.Filled.VolumeDown
                volume > 0.5f -> Icons.AutoMirrored.Filled.VolumeUp
                else -> error("unreachable")
            },
            contentDescription = "Volume Control"
        )

        HachimiSlider(
            modifier = Modifier.fillMaxWidth().height(6.dp),
            progress = { volume },
            onProgressChange = onVolumeChange,
            applyMode = SliderChangeApplyMode.Immediate,
            trackColor = HachimiTheme.colorScheme.outline,
            barColor = HachimiTheme.colorScheme.primary
        )
    }
}

@Composable
private fun FunctionButtons(
    modifier: Modifier,
    shuffleOn: Boolean,
    repeatOn: Boolean,
    onShuffleChange: (Boolean) -> Unit,
    onRepeatChange: (Boolean) -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onMusicQueueClick: () -> Unit,
    onOpenInFullClick: () -> Unit,
) {

    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HachimiIconToggleButton(shuffleOn, onShuffleChange) {
            Icon(Icons.Default.Shuffle, if (shuffleOn) "Shuffle On" else "Shuffle Off")
        }
        HachimiIconToggleButton(repeatOn, onRepeatChange) {
            Icon(Icons.Default.Repeat, if (repeatOn) "Repeat On" else "Repeat Off")
        }
        HachimiIconButton(onClick = onAddToPlaylistClick) {
            Icon(Icons.Default.Add, "Add to playlist")
        }
        HachimiIconButton(onClick = onMusicQueueClick) {
            Icon(Icons.AutoMirrored.Filled.List, "Music Queue")
        }
        /*HachimiIconButton(onClick = onOpenInFullClick) {
            Icon(Icons.Default.OpenInFull, "Open In Full")
        }*/
    }
}

@Composable
private fun MusicQueuePopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val expandedState = remember { MutableTransitionState(expanded) }
    expandedState.targetState = expanded

    if (expandedState.targetState || expandedState.currentState) {
        Popup(
            onDismissRequest = onDismissRequest,
            alignment = Alignment.BottomEnd,
            offset = with(LocalDensity.current) {
                IntOffset(
                    -24.dp.roundToPx(),
                    -140.dp.roundToPx()
                )
            },
            properties = PopupProperties(focusable = true)
        ) {
            AnimatedVisibility(
                visibleState = expandedState,
                enter = materialSharedAxisYIn(true, rememberSlideDistance()),
                exit = materialSharedAxisYOut(false, rememberSlideDistance())
            ) {
                content()
            }
        }
    }
}