package world.hachimi.app.ui.player.fullscreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PlayerUIState
import world.hachimi.app.model.SearchViewModel
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.insets.currentSafeAreaInsets
import world.hachimi.app.ui.player.components.AddToPlaylistDialog
import world.hachimi.app.ui.player.components.PlayerProgress
import world.hachimi.app.ui.player.components.ShareDialog
import world.hachimi.app.ui.player.fullscreen.components.AuthorAndPV
import world.hachimi.app.ui.player.fullscreen.components.Cover
import world.hachimi.app.ui.player.fullscreen.components.InfoTabContent
import world.hachimi.app.ui.player.fullscreen.components.JmidLabel
import world.hachimi.app.ui.player.fullscreen.components.Lyrics2
import world.hachimi.app.ui.player.fullscreen.components.MusicQueue
import world.hachimi.app.ui.player.fullscreen.components.Page
import world.hachimi.app.ui.player.fullscreen.components.PagerButtons
import world.hachimi.app.ui.player.fullscreen.components.Titles
import world.hachimi.app.ui.player.fullscreen.components.rememberTabTransitionSpec
import world.hachimi.app.ui.util.fadingEdges
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun ExpandedPlayerScreen2(
    global: GlobalStore = koinInject()
) {
    val uiState = global.player.playerState

    Box(Modifier.fillMaxSize()) {
        Content(global, uiState)
        ShrinkButton(
            modifier = Modifier.padding(32.dp).padding(top = currentSafeAreaInsets().top).align(Alignment.TopStart),
            onClick = global::shrinkPlayer
        )
    }
}

@Composable
private fun Content(
    global: GlobalStore,
    uiState: PlayerUIState
) {
    var currentPage by remember { mutableStateOf(Page.Lyrics) }
    val scrollState = rememberLazyListState() // Keep the scroll state between pages
    var coverTopLeft by remember { mutableStateOf(IntOffset.Zero) }
    var coverSize by remember { mutableStateOf(IntSize.Zero) }

    val leftPaneInteractionSource = remember { MutableInteractionSource() }
    val leftPaneHovered by leftPaneInteractionSource.collectIsHoveredAsState()

    var tobeAddedSong by remember { mutableStateOf<Pair<Long, Long>?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }

    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        LeftPane(
            modifier = Modifier.weight(1f).fillMaxHeight().hoverable(leftPaneInteractionSource),
            header = {
                JmidLabel(
                    Modifier
                        .wrapContentHeight(align = Alignment.Bottom)
                        .padding(bottom = 6.dp),
                    uiState.displayedJmid
                )
            },
            cover = {
                Cover(model = uiState.displayedCover)
            },
            footer = {
                Footer(
                    global = global,
                    uiState = uiState,
                    hideInfo = currentPage == Page.Info,
                    hovered = leftPaneHovered,
                    onNavToUser = { uid ->
                        global.nav.push(Route.Root.PublicUserSpace(uid))
                        global.shrinkPlayer()
                    },
                    modifier = Modifier.padding(top = 12.dp),
                    onAddToPlaylistClick = {
                        tobeAddedSong = uiState.readySongInfo?.id?.let { it to Random.nextLong() }
                    },
                    onShareClick = {
                        showShareDialog = true
                    }
                )
            },
            onCoverLayout = { topLeft, size ->
                coverTopLeft = topLeft
                coverSize = size
            }
        )
        Box(Modifier.weight(1f)) {
            TabContent(currentPage, uiState, coverTopLeft, global, scrollState)
            PagerButtons(
                modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp),
                currentPage = currentPage,
                onPageSelect = { currentPage = it }
            )
        }
    }

    AddToPlaylistDialog(
        tobeAddedSongId = tobeAddedSong?.first, random = tobeAddedSong?.second,
        onDismiss = { tobeAddedSong = null },
    )

    if (showShareDialog) {
        uiState.readySongInfo?.let { info ->
            ShareDialog(
                onDismissRequest = { showShareDialog = false },
                jmid = info.displayId,
                title = info.title,
                author = info.uploaderName,
            )
        }
    }
}

@Composable
private fun TabContent(
    currentPage: Page,
    uiState: PlayerUIState,
    coverTopLeft: IntOffset,
    global: GlobalStore,
    scrollState: LazyListState
) {
    AnimatedContent(
        targetState = currentPage,
        transitionSpec = rememberTabTransitionSpec()
    ) { page ->
        when (page) {
            Page.Info -> InfoTabContent(
                modifier = Modifier.fillMaxSize()
                    .wrapContentWidth()
                    .widthIn(max = 400.dp),
                uiState = uiState,
                contentPadding = with(LocalDensity.current) {
                    PaddingValues(
                        top = coverTopLeft.y.toDp(),
                        bottom = coverTopLeft.y.toDp()
                    )
                },
                onNavToUser = { uid ->
                    global.nav.push(Route.Root.PublicUserSpace(uid))
                    global.shrinkPlayer()
                },
                onSearchTag = { _, name ->
                    global.nav.push(Route.Root.Search(name, SearchViewModel.SearchType.SONG))
                    global.shrinkPlayer()
                }
            )

            Page.Queue -> QueueTab(
                modifier = Modifier.fillMaxSize()
                    .wrapContentWidth()
                    .widthIn(max = 400.dp)
                    .padding(vertical = 64.dp)
                    .fadingEdges(),
                global = global,
                contentPadding = with(LocalDensity.current) {
                    PaddingValues(
                        top = coverTopLeft.y.toDp() - 64.dp, // - paddings
                        bottom = coverTopLeft.y.toDp() - 64.dp // - paddings
                    )
                }
            )

            Page.Lyrics -> Lyrics2(
                modifier = Modifier.fillMaxSize().padding(end = 64.dp).padding(vertical = 64.dp).fadingEdges(),
                lazyListState = scrollState,
                supportTimedLyrics = uiState.timedLyricsEnabled,
                currentLine = uiState.currentLyricsLine,
                lines = uiState.lyricsLines,
                loading = uiState.fetchingMetadata,
            )

            else -> {}
        }
    }
}

@Composable
private fun ShrinkButton(modifier: Modifier, onClick: () -> Unit) {
    HachimiIconButton(modifier = modifier, onClick = onClick) {
        Icon(Icons.Default.CloseFullscreen, "Close")
    }
}

@Composable
private fun LeftPane(
    modifier: Modifier,
    header: @Composable () -> Unit,
    cover: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    onCoverLayout: (topLeft: IntOffset, size: IntSize) -> Unit
) {
    val minCoverSize = 256.dp
    val maxCoverSize = 600.dp
    val minHorizonPadding = 172.dp
    val minVerticalPadding = 300.dp

    Layout(modifier = modifier, content = {
        header()
        cover()
        footer()
    }) { measurables, constraints ->
        val header = measurables[0]
        val cover = measurables[1]
        val footer = measurables[2]

        val minEdgeSize = minOf(
            constraints.maxHeight - minVerticalPadding.toPx(),
            constraints.maxWidth - minHorizonPadding.toPx()
        )
        val coverSize = (minEdgeSize).coerceIn(minCoverSize.toPx(), maxCoverSize.toPx()).roundToInt()

        val coverPlaceable = cover.measure(Constraints.fixed(coverSize, coverSize))
        val coverY = (constraints.maxHeight - coverPlaceable.height) / 2
        val x = (constraints.maxWidth - coverSize) / 2

        val headerPlaceable = header.measure(Constraints.fixed(coverSize, coverY))
        val footerPlaceable = footer.measure(Constraints.fixed(coverSize, constraints.maxHeight - coverY))

        layout(constraints.maxWidth, constraints.maxHeight) {
            headerPlaceable.place(x, 0)
            coverPlaceable.place(x, coverY)
            footerPlaceable.place(x, coverY + coverPlaceable.height)
            onCoverLayout(
                IntOffset(x, coverY),
                IntSize(coverSize, coverSize)
            )
        }
    }
}

@Composable
private fun Footer(
    global: GlobalStore,
    uiState: PlayerUIState,
    hideInfo: Boolean,
    hovered: Boolean,
    onNavToUser: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onAddToPlaylistClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val showControl = hideInfo || hovered
    var showDropdownMenu by remember { mutableStateOf(false) }

    Column(modifier) {
        PlayerProgress(
            durationMillis = uiState.displayedDurationMillis,
            currentMillis = { uiState.displayedCurrentMillis },
            onProgressChange = { global.player.setSongProgress(it) },
            bufferingProgress = uiState.downloadProgress,
            trackColor = HachimiTheme.colorScheme.onSurface.copy(0.1f),
            barColor = HachimiTheme.colorScheme.onSurface.copy(1f)
        )

        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(visible = !hideInfo) {
            AuthorAndPV(
                authorName = uiState.displayedAuthor,
                hasMultipleArtists = uiState.songInfo?.productionCrew.orEmpty().size > 1,
                pvLink = uiState.readySongInfo?.externalLinks?.firstOrNull()?.url,
                pvAlignToEnd = true,
                avatar = uiState.userProfile?.avatarUrl,
                onUserClick = {
                    uiState.readySongInfo?.uploaderUid?.let {
                        onNavToUser(it)
                    }
                }
            )
        }

        Box(Modifier.align(Alignment.End)) {
            DropdownMenu(
                expanded = showDropdownMenu, onDismissRequest = { showDropdownMenu = false },
            ) {
                DropdownMenuItem(
                    onClick = {
                        onShareClick()
                        showDropdownMenu = false
                    },
                    text = { Text("分享") },
                    leadingIcon = { Icon(Icons.Default.Share, "Share") },
                )
            }
        }

        Box(Modifier.weight(1f)) {
            Crossfade(showControl) { showControl ->
                if (showControl) Controls(
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    playing = uiState.isPlaying,
                    onPreviousClick = { global.player.previous() },
                    onPlayOrPauseClick = { global.player.playOrPause() },
                    onNextClick = { global.player.next() },
                    onAddToPlaylistClick = onAddToPlaylistClick,
                    onMoreClick = { showDropdownMenu = true }
                )
                else BriefInfo(
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    title = uiState.displayedTitle,
                    subtitle = uiState.readySongInfo?.subtitle,
                    description = uiState.readySongInfo?.description,
                )
            }
        }
    }
}

@Composable
private fun Controls(
    modifier: Modifier,
    playing: Boolean,
    onPreviousClick: () -> Unit,
    onPlayOrPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        ControlButton(onClick = onAddToPlaylistClick) {
            Icon(Icons.Default.Add, "Add to playlist", tint = LocalContentColor.current.copy(0.6f))
        }
        ControlButton(onClick = onPreviousClick) {
            Icon(Icons.Default.SkipPrevious, "Skip Previous")
        }
        ControlButton(onClick = onPlayOrPauseClick) {
            if (playing) Icon(Icons.Default.Pause, "Pause")
            else Icon(Icons.Default.PlayArrow, "Play")
        }
        ControlButton(onClick = onNextClick) {
            Icon(Icons.Default.SkipNext, "Skip Next")
        }
        ControlButton(onClick = onMoreClick) {
            Icon(Icons.Default.MoreVert, "More", tint = LocalContentColor.current.copy(0.6f))
        }
    }
}

@Composable
private fun ControlButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.size(28.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(24.dp)) {
            content()
        }
    }
}

private val bodyStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
)

@Composable
private fun BriefInfo(
    modifier: Modifier,
    title: String,
    subtitle: String?,
    description: String?
) {
    Column(modifier) {
        Titles(
            modifier = Modifier.fillMaxWidth(),
            title = title, subtitle = subtitle
        )

        description?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                style = bodyStyle, color = LocalContentColor.current.copy(0.6f),
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QueueTab(
    global: GlobalStore,
    modifier: Modifier,
    contentPadding: PaddingValues
) {
    Column(modifier) {
        MusicQueue(
            queue = global.player.musicQueue,
            playingSongId = if (global.player.playerState.fetchingMetadata) global.player.playerState.fetchingSongId else global.player.playerState.songInfo?.id,
            onPlayClick = { global.player.playSongInQueue(it) },
            onRemoveClick = { global.player.removeFromQueue(it) },
            contentPadding = contentPadding
        )
    }
}