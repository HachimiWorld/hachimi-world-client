package world.hachimi.app.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.fastForEach
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PlayerUIState
import world.hachimi.app.model.SongDetailInfo
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.player.components.*
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.WindowSize
import kotlin.time.Clock

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayerScreen() {
    val global: GlobalStore = koinInject()
    BackHandler {
        global.shrinkPlayer()
    }
    BoxWithConstraints {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant) {
            if (maxWidth < WindowSize.MEDIUM) {
                CompactPlayerScreen(
                    playerState = global.player.playerState,
                    onShrinkClick = { global.shrinkPlayer() },
                    onPlayOrPauseClick = { global.player.playOrPause() },
                    onPreviousClick = { global.player.previous() },
                    onNextClick = { global.player.next() },
                    onProgressChange = { global.player.setSongProgress(it) },
                    onNavToAuthor = { uid ->
                        global.shrinkPlayer()
                        global.nav.push(Route.Root.PublicUserSpace(uid))
                    },
                    shuffle = global.player.shuffleMode,
                    repeat = global.player.repeatMode,
                    onShuffleChange = { global.player.updateShuffleMode(it) },
                    onRepeatChange = { global.player.updateRepeatMode(it) }
                )
            } else {
                ExpandedPlayerScreen(
                    playerState = global.player.playerState,
                    onShrinkClick = { global.shrinkPlayer() },
                    onPlayOrPauseClick = { global.player.playOrPause() },
                    onPreviousClick = { global.player.previous() },
                    onNextClick = { global.player.next() },
                    onProgressChange = { global.player.setSongProgress(it) },
                    onNavToAuthor = { uid ->
                        global.shrinkPlayer()
                        global.nav.push(Route.Root.PublicUserSpace(uid))
                    },
                    shuffle = global.player.shuffleMode,
                    repeat = global.player.repeatMode,
                    onShuffleChange = { global.player.updateShuffleMode(it) },
                    onRepeatChange = { global.player.updateRepeatMode(it) }
                )
            }
        }
    }
}

@Composable
fun CompactPlayerScreen(
    playerState: PlayerUIState,
    shuffle: Boolean,
    onShuffleChange: (Boolean) -> Unit,
    repeat: Boolean,
    onRepeatChange: (Boolean) -> Unit,
    onShrinkClick: () -> Unit,
    onPlayOrPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onProgressChange: (Float) -> Unit,
    onNavToAuthor: (Long) -> Unit
) {
    var displayingLyrics by remember { mutableStateOf(false) }
    // TODO: Remove these, use new UI/UX to indicate loading status
    val previewMetadata = playerState.previewMetadata
    val info = playerState.songInfo

    val displayedId = if (playerState.fetchingMetadata) {
        previewMetadata?.displayId
    } else {
        info?.displayId
    } ?: ""

    Column(Modifier.systemBarsPadding()) {
        Box(Modifier.fillMaxWidth().weight(1f)) {
            val lyricsAlpha by animateFloatAsState(if (displayingLyrics) 1f else 0f)
            Column(
                Modifier
                    .graphicsLayer { alpha = 1f - lyricsAlpha }
                    .padding(horizontal = 48.dp, vertical = 24.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    JmidLabel(displayedId, Modifier.align(Alignment.Start))
                    Spacer(Modifier.height(8.dp))
                    Album(
                        modifier = Modifier.fillMaxWidth(),
                        coverUrl = if (playerState.fetchingMetadata) previewMetadata?.coverUrl else info?.coverUrl,
                        onClick = { displayingLyrics = true },
                    )
                    Spacer(Modifier.height(16.dp))
                    MetadataInfo(playerState, onNavToAuthor)
                }

                // Current lyric line
                val lyricsLine =
                    remember { derivedStateOf { playerState.lyricsLines.getOrNull(playerState.currentLyricsLine) } }

                AnimatedContent(lyricsLine.value, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = it ?: "",
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Lyrics(
                modifier = Modifier.graphicsLayer {
                    // Hide and do not consume pointer input
                    this.scaleX = if (lyricsAlpha == 0f) 0f else 1f
                    this.scaleY = if (lyricsAlpha == 0f) 0f else 1f
                    this.alpha = lyricsAlpha
                }.fillMaxSize().clickable(
                    indication = null,
                    interactionSource = null,
                    onClick = { displayingLyrics = false },
                    enabled = displayingLyrics
                ).padding(horizontal = 24.dp),
                currentLine = playerState.currentLyricsLine,
                lines = playerState.lyricsLines,
                fadeColor = MaterialTheme.colorScheme.surfaceVariant,
                loading = playerState.fetchingMetadata,
            )

            IconButton(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                onClick = onShrinkClick
            ) {
                Icon(Icons.Default.CloseFullscreen, "Shrink")
            }
        }

        Column(
            Modifier.fillMaxWidth().padding(
                top = 12.dp,
                start = 24.dp,
                end = 24.dp,
                bottom = 24.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SongControl(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                isPlaying = playerState.isPlaying,
                isLoading = playerState.buffering,
                loadingProgress = { playerState.downloadProgress },
                onPlayPauseClick = onPlayOrPauseClick,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick,
                shuffle = shuffle,
                onShuffleModeChange = onShuffleChange,
                repeat = repeat,
                onRepeatModeChange = onRepeatChange
            )

            Spacer(Modifier.height(12.dp))

            SongProgress(
                durationMillis = if (playerState.fetchingMetadata) {
                    previewMetadata?.duration?.inWholeMilliseconds
                } else {
                    info?.durationSeconds?.let {
                        it * 1000L
                    }
                } ?: -1L,
                currentMillis = if (playerState.fetchingMetadata) {
                    0L
                } else {
                    playerState.currentMillis
                },
                onProgressChange = onProgressChange
            )
        }
    }
}

@Composable
fun ExpandedPlayerScreen(
    playerState: PlayerUIState,
    shuffle: Boolean,
    onShuffleChange: (Boolean) -> Unit,
    repeat: Boolean,
    onRepeatChange: (Boolean) -> Unit,
    onShrinkClick: () -> Unit,
    onPlayOrPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onProgressChange: (Float) -> Unit,
    onNavToAuthor: (Long) -> Unit
) {
    val previewMetadata = playerState.previewMetadata
    val info = playerState.songInfo

    val displayedId = if (playerState.fetchingMetadata) {
        previewMetadata?.displayId
    } else {
        info?.displayId
    } ?: ""
    val displayedCover = if (playerState.fetchingMetadata) previewMetadata?.coverUrl else info?.coverUrl

    Box {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().weight(1f).padding(32.dp)) {
                Column(Modifier.fillMaxHeight().weight(1f), verticalArrangement = Arrangement.Center) {
                    Column(Modifier.align(Alignment.End).padding(48.dp)) {
                        JmidLabel(displayedId, Modifier.align(Alignment.Start))

                        Spacer(Modifier.height(8.dp))

                        BoxWithConstraints(Modifier.wrapContentSize()) {
                            val size = min(maxHeight * 0.7f, maxWidth)

                            Album(
                                coverUrl = displayedCover,
                                onClick = {},
                                modifier = Modifier.size(size)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        MetadataInfo(playerState, onNavToAuthor)
                    }
                }

                Spacer(Modifier.width(64.dp))

                Lyrics(
                    loading = playerState.fetchingMetadata,
                    currentLine = playerState.currentLyricsLine,
                    lines = playerState.lyricsLines,
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    fadeColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Column(
                Modifier.fillMaxWidth().padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 24.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SongControl(
                    modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
                    isPlaying = playerState.isPlaying,
                    isLoading = playerState.buffering,
                    loadingProgress = { playerState.downloadProgress },
                    onPlayPauseClick = onPlayOrPauseClick,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick,
                    shuffle = shuffle,
                    onShuffleModeChange = onShuffleChange,
                    repeat = repeat,
                    onRepeatModeChange = onRepeatChange
                )

                Spacer(Modifier.height(12.dp))

                SongProgress(
                    durationMillis = if (playerState.fetchingMetadata) {
                        previewMetadata?.duration?.inWholeMilliseconds
                    } else {
                        info?.durationSeconds?.let {
                            it * 1000L
                        }
                    } ?: -1L,
                    currentMillis = if (playerState.fetchingMetadata) {
                        0L
                    } else {
                        playerState.currentMillis
                    },
                    onProgressChange = onProgressChange
                )
            }
        }

        IconButton(
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            onClick = onShrinkClick
        ) {
            Icon(Icons.Default.CloseFullscreen, "Shrink")
        }
    }
}

@Composable
private fun JmidLabel(
    displayedId: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = displayedId,
        style = MaterialTheme.typography.labelSmall,
        color = LocalContentColor.current.copy(0.7f)
    )
}
@Composable
private fun MetadataInfo(
    playerState: PlayerUIState,
    onNavToAuthor: (Long) -> Unit
) {
    var showOriginInfo by remember { mutableStateOf(false) }
    var showExternalLinks by remember { mutableStateOf(false) }
    var showStaffInfo by remember { mutableStateOf(false) }

    val previewMetadata = playerState.previewMetadata
    val info = playerState.songInfo

    val displayedTitle = if (playerState.fetchingMetadata) {
        previewMetadata?.title
    } else {
        info?.title
    } ?: ""
    val displayedAuthor = if (playerState.fetchingMetadata) {
        previewMetadata?.author
    } else {
        info?.uploaderName
    } ?: ""
    val explicit = if (playerState.fetchingMetadata) {
        previewMetadata?.explicit
    } else {
        info?.explicit
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = displayedTitle,
            style = MaterialTheme.typography.titleMedium
        )

        if (explicit == true) Icon(
            modifier = Modifier.padding(start = 8.dp).requiredSize(16.dp),
            imageVector = Icons.Default.Explicit,
            contentDescription = "Explicit",
            tint = LocalContentColor.current.copy(0.72f),
        )
    }

    if (!playerState.fetchingMetadata) {
        info?.subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall,
                color = LocalContentColor.current.copy(0.7f)
            )
        }
    }

    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            modifier = Modifier.clickable(indication = null, interactionSource = null, onClick = {
                info?.let {
                    if (it.productionCrew.isNotEmpty()) {
                        showStaffInfo = true
                    } else {
                        onNavToAuthor(info.uploaderUid)
                    }
                }
            }),
            text = info.let { info ->
                if (info?.productionCrew?.isNotEmpty() == true) {
                    "作者: $displayedAuthor 等人"
                } else {
                    "作者: $displayedAuthor"
                }
            },
            style = MaterialTheme.typography.labelSmall,
            color = LocalContentColor.current.copy(0.7f)
        )

        if (!playerState.fetchingMetadata) {
            info?.originInfos?.fastForEach { item ->
                Text(
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = null,
                        onClick = {
                            showOriginInfo = true
                        }),
                    text = "原作: ${item.title}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalContentColor.current.copy(0.7f)
                )
            }
        }

        if (info?.externalLinks?.isNotEmpty() == true) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "MV: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalContentColor.current.copy(0.7f)
                )
                Icon(
                    modifier = Modifier.size(14.dp).clickable(
                        indication = null,
                        interactionSource = null,
                        onClick = { showExternalLinks = true },
                    ),
                    imageVector = Icons.Default.MusicVideo,
                    contentDescription = "Music Video",
                    tint = LocalContentColor.current.copy(0.7f)
                )
            }
        }
    }

    if (showOriginInfo) {
        val originInfo = playerState.songInfo?.originInfos?.firstOrNull()

        if (originInfo != null) OriginInfoDialog(
            onDismissRequest = { showOriginInfo = false },
            title = originInfo.title,
            artist = originInfo.artist,
            url = originInfo.url
        )
    }

    if (showExternalLinks) {
        info?.externalLinks?.let {
            ExternalLinkDialog(
                onDismissRequest = { showExternalLinks = false },
                links = it
            )
        }
    }

    if (showStaffInfo) {
        info?.let { info ->
            StaffDialog(
                onDismissRequest = { showStaffInfo = false },
                uploaderUid = info.uploaderUid,
                uploaderName = info.uploaderName,
                crew = info.productionCrew,
                onNavToAuthorClick = onNavToAuthor
            )
        }
    }
}

@Composable
private fun rememberTestPlayerState(): PlayerUIState {
    val state = remember {
        PlayerUIState().apply {
            updateSongInfo(
                SongDetailInfo(
                    id = 0,
                    displayId = "JM-AWSL-123",
                    title = "Test Title",
                    subtitle = "Test Subtitle",
                    description = "",
                    durationSeconds = 365,
                    tags = emptyList(),
                    lyrics = "",
                    audioUrl = "",
                    coverUrl = "",
                    productionCrew = emptyList(),
                    creationType = 0,
                    originInfos = emptyList(),
                    uploaderUid = 100000,
                    uploaderName = "",
                    playCount = 0,
                    likeCount = 0,
                    externalLinks = listOf(
                        SongModule.ExternalLink("bilibili", "https://xxxxxxx")
                    ),
                    createTime = Clock.System.now(),
                    releaseTime = Clock.System.now(),
                    explicit = true,
                    gain = null
                )
            )
            hasSong = true
            setLyrics("[00:00.00] Test lyrics line\n[01:00.00] Test lyrics line 2")
            updateCurrentMillis(100L)
        }
    }
    return state
}

@Preview
@Composable
private fun PreviewExpanded() {
    val playerUIState = rememberTestPlayerState()
    Box(Modifier.requiredWidth(1200.dp)) {
        PreviewTheme(background = true) {
            ExpandedPlayerScreen(
                playerState = playerUIState,
                onShrinkClick = {},
                onPlayOrPauseClick = {},
                onPreviousClick = {},
                onNextClick = {},
                onProgressChange = {},
                onNavToAuthor = {},
                shuffle = false,
                repeat = false,
                onShuffleChange = { },
                onRepeatChange = { }
            )
        }
    }
}

@Preview
@Composable
private fun PreviewCompact() {
    val playerUIState = rememberTestPlayerState()
    PreviewTheme(background = true) {
        CompactPlayerScreen(
            playerState = playerUIState,
            onShrinkClick = {},
            onPlayOrPauseClick = {},
            onPreviousClick = {},
            onNextClick = {},
            onProgressChange = {},
            onNavToAuthor = {},
            shuffle = false,
            repeat = false,
            onShuffleChange = { },
            onRepeatChange = { }
        )
    }
}
