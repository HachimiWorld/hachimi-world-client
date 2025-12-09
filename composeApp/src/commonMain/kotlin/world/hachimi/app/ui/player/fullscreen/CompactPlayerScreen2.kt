package world.hachimi.app.ui.player.fullscreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PlayerUIState
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.insets.currentSafeAreaInsets
import world.hachimi.app.ui.player.components.AddToPlaylistDialog
import world.hachimi.app.ui.player.components.CreatePlaylistDialog
import world.hachimi.app.ui.player.components.PlayerProgress
import world.hachimi.app.ui.player.fullscreen.components.*
import kotlin.random.Random

@Composable
fun CompactPlayerScreen2(
    global: GlobalStore = koinInject()
) {
    val uiState = global.player.playerState
    var showTab by rememberSaveable { mutableStateOf(false) }
    var currentPage by rememberSaveable { mutableStateOf<Page?>(null) }
    val scrollState = rememberLazyListState()
    val (painter, dominantColor) = rememberAsyncPainterAndColor(uiState.displayedCover)
    var tobeAddedSong by remember { mutableStateOf<Pair<Long, Long>?>(null) }

    BackgroundContainer(painter, dominantColor) {
        Column(
            Modifier.fillMaxSize()
                .padding(top = currentSafeAreaInsets().top, bottom = currentSafeAreaInsets().bottom)
                .padding(vertical = 24.dp)
        ) {

            HachimiIconButton(
                modifier = Modifier.padding(start = 32.dp),
                onClick = { global.shrinkPlayer() },
                touchMode = true
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Shrink")
            }

            AnimatedContent(
                targetState = showTab,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { showTab ->
                if (!showTab) {
                    PlayerTab(
                        uiState = uiState,
                        onNavToUser = {
                            global.nav.push(Route.Root.PublicUserSpace(it))
                            global.shrinkPlayer()
                        },
                        onAddToPlaylistClick = {
                            tobeAddedSong = uiState.readySongInfo?.id?.let { it to Random.nextLong() }
                        }
                    )
                } else {
                    Column {
                        Header(
                            modifier = Modifier.padding(top = 16.dp, start = 32.dp, end = 32.dp),
                            cover = uiState.displayedCover, title = uiState.displayedTitle,
                            author = uiState.displayedAuthor,
                            hasMultipleArtists = uiState.songInfo?.productionCrew?.isNotEmpty() == true,
                            pvLink = uiState.readySongInfo?.externalLinks?.firstOrNull()?.url,
                            avatar = uiState.userProfile?.avatarUrl,
                            onUserClick = {
                                uiState.readySongInfo?.uploaderUid?.let {
                                    global.nav.push(Route.Root.PublicUserSpace(it))
                                    global.shrinkPlayer()
                                }
                            }
                        )
                        AnimatedContent(
                            targetState = currentPage,
                            transitionSpec = rememberTabTransitionSpec()
                        ) { tab ->
                            when (tab) {
                                Page.Info -> InfoTab(
                                    uiState,
                                    onNavToUser = {
                                        global.nav.push(Route.Root.PublicUserSpace(it))
                                        global.shrinkPlayer()
                                    }
                                )

                                Page.Queue -> QueueTab()
                                Page.Lyrics -> LyricsTab(global, uiState, scrollState)
                                else -> {}
                            }
                        }
                    }
                }
            }

            PlayerProgress(
                modifier = Modifier.padding(top = 24.dp).padding(horizontal = 32.dp),
                durationMillis = uiState.displayedDurationMillis,
                currentMillis = uiState.displayedCurrentMillis,
                bufferingProgress = uiState.downloadProgress,
                onProgressChange = { global.player.setSongProgress(it) },
                trackColor = HachimiTheme.colorScheme.onSurface.copy(0.1f),
                barColor = HachimiTheme.colorScheme.onSurface,
                timeOnTop = false,
                touchMode = true
            )
            ControlButtons(
                modifier = Modifier.padding(top = 16.dp).padding(horizontal = 32.dp).fillMaxWidth(),
                playing = uiState.isPlaying,
                onPreviousClick = { global.player.previous() },
                onNextClick = { global.player.next() },
                onPlayClick = { global.player.playOrPause() },
                onPauseClick = { global.player.playOrPause() }
            )
            PagerButtons2(
                modifier = Modifier.padding(top = 16.dp).padding(horizontal = 32.dp).fillMaxWidth(),
                currentPage = if (showTab) currentPage else null,
                onPageSelect = {
                    if (!showTab) {
                        currentPage = it
                        showTab = true
                    } else {
                        if (currentPage == it) {
                            showTab = false
                        } else {
                            currentPage = it
                        }
                    }
                }
            )
        }
    }

    AddToPlaylistDialog(tobeAddedSong?.first, tobeAddedSong?.second)
    CreatePlaylistDialog()
}

@Composable
private fun PlayerTab(uiState: PlayerUIState, onNavToUser: (Long) -> Unit, onAddToPlaylistClick: () -> Unit) {
    Column(Modifier.padding(top = 32.dp).padding(horizontal = 32.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            JmidLabel(
                modifier = Modifier,
                jmid = uiState.displayedJmid
            )

            Cover(
                modifier = Modifier.padding(top = 12.dp)
                    .fillMaxWidth()
                    .layout { m, c ->
                        val size = minOf(c.maxWidth, c.maxHeight)
                        val p = m.measure(Constraints.fixed(size, size))
                        layout(p.width, p.height) {
                            p.place(0, 0)
                        }
                    },
                model = uiState.displayedCover,
            )
        }

        Row(
            modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Titles(
                    title = uiState.displayedTitle,
                    subtitle = uiState.readySongInfo?.subtitle,
                )

                AuthorAndPV(
                    modifier = Modifier.padding(top = 8.dp),
                    authorName = uiState.displayedAuthor,
                    hasMultipleArtists = uiState.songInfo?.productionCrew?.isNotEmpty() == true,
                    pvLink = uiState.readySongInfo?.externalLinks?.firstOrNull()?.url,
                    pvAlignToEnd = false,
                    avatar = uiState.userProfile?.avatarUrl,
                    onUserClick = {
                        uiState.readySongInfo?.uploaderUid?.let {
                            onNavToUser(it)
                        }
                    }
                )
            }
            HachimiIconButton(onClick = onAddToPlaylistClick) {
                Icon(Icons.Default.Add, contentDescription = "Add to playlist")
            }
            HachimiIconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        }
    }
}

@Composable
private fun InfoTab(uiState: PlayerUIState, onNavToUser: (Long) -> Unit) {
    Column(Modifier.fillMaxSize().padding(top = 32.dp).padding(horizontal = 32.dp)) {
        InfoTabContent(Modifier.weight(1f), uiState, onNavToUser = onNavToUser)
    }
}

@Composable
private fun LyricsTab(
    global: GlobalStore,
    uiState: PlayerUIState,
    scrollState: LazyListState
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
        Lyrics2(
            modifier = Modifier.weight(1f).fadingEdges(42.dp, 42.dp),
            lazyListState = scrollState,
            supportTimedLyrics = uiState.timedLyricsEnabled,
            currentLine = uiState.currentLyricsLine,
            lines = uiState.lyricsLines,
            loading = uiState.fetchingMetadata,
            centralizeFirstLine = false,
            contentPadding = PaddingValues(vertical = 42.dp)
        )
    }
}

@Composable
private fun QueueTab() {
    Box(Modifier.fillMaxSize().padding(top = 32.dp)) {}
}

@Composable
private fun Header(
    modifier: Modifier = Modifier,
    cover: Any?,
    title: String,
    author: String,
    avatar: String?,
    hasMultipleArtists: Boolean,
    pvLink: String?,
    onUserClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        AsyncImage(
            model = cover,
            contentDescription = "Cover",
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Column(Modifier.weight(1f).padding(start = 16.dp, end = 16.dp)) {
            Text(
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    velocity = 10.dp
                ),
                text = title,
                style = titleStyle,
                maxLines = 1
            )
            AuthorAndPV(
                modifier = Modifier.padding(top = 8.dp),
                authorName = author,
                hasMultipleArtists = hasMultipleArtists,
                pvLink = pvLink,
                pvAlignToEnd = false,
                avatar = avatar,
                onUserClick = onUserClick
            )
        }
        HachimiIconButton(onClick = {
            // TODO:
        }, touchMode = true) {
            Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
        }
        HachimiIconButton(onClick = {
            // TODO:
        }, touchMode = true) {
            Icon(Icons.Default.MoreVert, contentDescription = "More")
        }
    }
}

@Composable
private fun ControlButtons(
    modifier: Modifier,
    playing: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        PreviousButton(onClick = onPreviousClick)

        PlayPauseButton(
            modifier = Modifier.weight(1f),
            playing = playing,
            onClick = {
                if (playing) onPauseClick()
                else onPlayClick()
            }
        )

        NextButton(onClick = onNextClick)
    }
}

@Composable
private fun PreviousButton(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.defaultMinSize(minHeight = 78.dp, minWidth = 78.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEBE9E7).copy(0.2f),
        contentColor = LocalContentColor.current
    ) {
        Box(
            modifier = Modifier.clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
        }
    }
}

@Composable
private fun PlayPauseButton(
    playing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.defaultMinSize(minHeight = 78.dp, minWidth = 78.dp),
        shape = RoundedCornerShape(12.dp),
        color = HachimiTheme.colorScheme.onSurface,
        contentColor = HachimiTheme.colorScheme.onSurfaceReverse
    ) {
        Box(
            modifier = Modifier.clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (playing) Icon(Icons.Default.Pause, contentDescription = "Pause")
            else Icon(Icons.Default.PlayArrow, contentDescription = "Play")
        }
    }
}

@Composable
private fun NextButton(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.defaultMinSize(minHeight = 78.dp, minWidth = 78.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEBE9E7).copy(0.2f),
        contentColor = LocalContentColor.current
    ) {
        Box(
            modifier = Modifier.clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next")
        }
    }
}

private val titleStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp
)