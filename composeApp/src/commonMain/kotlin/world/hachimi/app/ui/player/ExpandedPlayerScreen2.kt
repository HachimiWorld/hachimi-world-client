package world.hachimi.app.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PlayerUIState
import world.hachimi.app.ui.LocalAnimatedVisibilityScope
import world.hachimi.app.ui.LocalSharedTransitionScope
import world.hachimi.app.ui.SharedTransitionKeys
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.DiffusionBackground
import world.hachimi.app.ui.design.components.HollowIconToggleButton
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.insets.currentSafeAreaInsets
import world.hachimi.app.ui.player.components.Lyrics2
import world.hachimi.app.ui.player.components.PlayerProgress
import kotlin.math.roundToInt

@Composable
fun ExpandedPlayerScreen2(
    global: GlobalStore = koinInject()
) {
    val uiState = global.player.playerState

    with(LocalSharedTransitionScope.current) {
        Box(
            Modifier
                .sharedBounds(
                    rememberSharedContentState(SharedTransitionKeys.Bounds),
                    LocalAnimatedVisibilityScope.current
                )
                .fillMaxSize()
                .background(HachimiTheme.colorScheme.background)
        ) {
            DiffusionBackground(
                modifier = Modifier.fillMaxSize(),
                painter = rememberAsyncImagePainter(
                    model = uiState.displayedCover,
                    filterQuality = FilterQuality.None,
                    placeholder = ColorPainter(HachimiTheme.colorScheme.onSurface)
                )
            )
            CompositionLocalProvider(LocalContentColor provides HachimiTheme.colorScheme.onSurfaceReverse) {
                IconButton(
                    modifier = Modifier.padding(32.dp).padding(top = currentSafeAreaInsets().top),
                    onClick = { global.shrinkPlayer() }
                ) {
                    Icon(Icons.Default.Close, "Close")
                }

                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    LeftPane(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        header = {
                            JmidLabel(
                                Modifier/*.border(1.dp, Color.Yellow)*/
                                    .wrapContentHeight(align = Alignment.Bottom)
                                    .padding(bottom = 6.dp),
                                uiState.displayedJmid
                            )
                        },
                        cover = { Cover(uiState.displayedCover) },
                        footer = {
                            Footer(
                                modifier = Modifier/*.border(1.dp, Color.Yellow)*/
                                    .padding(top = 12.dp),
                                global = global,
                                uiState = uiState
                            )
                        }
                    )
                    Box(Modifier.weight(1f)) {
                        var currentPage by remember { mutableStateOf(Page.Lyrics) }
                        AnimatedContent(currentPage) { page ->
                            when (page) {
                                Page.Lyrics -> Lyrics2(
                                    modifier = Modifier.fillMaxSize(),
                                    supportTimedLyrics = uiState.timedLyricsEnabled,
                                    currentLine = uiState.currentLyricsLine,
                                    lines = uiState.lyricsLines,
                                    loading = uiState.fetchingMetadata,
                                )
                                Page.Info -> Box(Modifier.fillMaxSize())
                                Page.Queue -> Box(Modifier.fillMaxSize())
                            }
                        }
                        PagerButtons(
                            modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp),
                            currentPage = currentPage,
                            onPageSelect = { currentPage = it }
                        )
                    }
                }
            }
        }
    }

    // TODO: Remove
    BoxWithConstraints {
        println("maxWidth: $maxWidth, maxHeight: $maxHeight")
    }
}

private val jmidStyle = TextStyle(
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    lineHeight = 16.sp
)

@Composable
private fun JmidLabel(
    modifier: Modifier = Modifier,
    jmid: String
) {
    Text(
        modifier = modifier,
        text = jmid,
        style = jmidStyle,
        color = LocalContentColor.current.copy(0.6f)
    )
}

@Composable
private fun LeftPane(
    modifier: Modifier,
    header: @Composable () -> Unit,
    cover: @Composable () -> Unit,
    footer: @Composable () -> Unit,
) {
    val minCoverSize = 256.dp
    val maxCoverSize = 600.dp
    val padding = 172.dp

    Layout(modifier = modifier, content = {
        header()
        cover()
        footer()
    }) { measurables, constraints ->
        val header = measurables[0]
        val cover = measurables[1]
        val footer = measurables[2]

        val minEdge = minOf(constraints.maxHeight, constraints.maxWidth)
        val coverSize = (minEdge - padding.toPx()).coerceIn(minCoverSize.toPx(), maxCoverSize.toPx()).roundToInt()

        val coverPlaceable = cover.measure(Constraints.fixed(coverSize, coverSize))
        val coverY = (constraints.maxHeight - coverPlaceable.height) / 2
        val x = (constraints.maxWidth - coverSize) / 2

        val headerPlaceable = header.measure(Constraints.fixed(coverSize, coverY))
        val footerPlaceable = footer.measure(Constraints.fixed(coverSize, constraints.maxHeight - coverY))

        layout(constraints.maxWidth, constraints.maxHeight) {
            headerPlaceable.place(x, 0)
            coverPlaceable.place(x, coverY)
            footerPlaceable.place(x, coverY + coverPlaceable.height)
        }
    }
}

@Composable
private fun Cover(
    model: Any?,
    modifier: Modifier = Modifier
) {
    with(LocalSharedTransitionScope.current) {
        Box(
            modifier
                .sharedElement(
                    rememberSharedContentState(SharedTransitionKeys.Cover),
                    LocalAnimatedVisibilityScope.current
                )
                .size(256.dp)
                .dropShadow(
                    RoundedCornerShape(8.dp), Shadow(
                        radius = 24.dp, color = Color.Black.copy(0.17f),
                        offset = DpOffset(0.dp, 2.dp)
                    )
                )
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray)
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = model,
                contentDescription = "Cover",
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun Footer(
    global: GlobalStore,
    uiState: PlayerUIState,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        PlayerProgress(
            durationMillis = uiState.displayedDurationMillis,
            currentMillis = uiState.displayedCurrentMillis,
            onProgressChange = { global.player.setSongProgress(it) },
            trackColor = HachimiTheme.colorScheme.onSurfaceReverse.copy(0.1f),
            barColor = HachimiTheme.colorScheme.onSurfaceReverse.copy(1f)
        )
        Controls(
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
            playing = uiState.isPlaying,
            onPreviousClick = { global.player.previous() },
            onPlayOrPauseClick = { global.player.playOrPause() },
            onNextClick = { global.player.next() },
            onAddToPlaylistClick = {
                // TODO:
            }
        )
        Information(
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
            title = uiState.displayedTitle,
            subtitle = uiState.songInfo?.subtitle,
            description = uiState.songInfo?.description
        )
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
) {
    Row(modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        ControlButton({}) {
            if (false) Icon(Icons.Default.Favorite, "Favorite On")
            else Icon(Icons.Default.FavoriteBorder, "Favorite Off")
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
        ControlButton(onClick = onAddToPlaylistClick) {
            Icon(Icons.Default.Add, "Add to playlist")
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

private val titleStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

private val subtitleStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
)

@Composable
private fun Information(
    modifier: Modifier,
    title: String,
    subtitle: String?,
    description: String?
) {
    Column(modifier) {
        Text(
            text = title,
            style = titleStyle, color = HachimiTheme.colorScheme.onSurfaceReverse,
            overflow = TextOverflow.Ellipsis, maxLines = 1
        )
        subtitle?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it, style = subtitleStyle, color = HachimiTheme.colorScheme.onSurfaceReverse.copy(0.6f),
                overflow = TextOverflow.Ellipsis, maxLines = 1
            )
        }
        description?.takeIf { it.isNotBlank() }?.let {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = it, style = subtitleStyle,
                maxLines = 2,
                color = HachimiTheme.colorScheme.onSurfaceReverse.copy(0.6f),
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

enum class Page {
    Lyrics, Info, Queue
}

@Composable
private fun PagerButtons(
    modifier: Modifier = Modifier,
    currentPage: Page,
    onPageSelect: (Page) -> Unit
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        HollowIconToggleButton(currentPage == Page.Info, { onPageSelect(Page.Info) }, icon = Icons.Outlined.Info, "Info")
        HollowIconToggleButton(currentPage == Page.Queue, { onPageSelect(Page.Queue) }, icon = Icons.AutoMirrored.Outlined.List, "Music Queue")
        HollowIconToggleButton(currentPage == Page.Lyrics, { onPageSelect(Page.Lyrics) }, icon = Icons.AutoMirrored.Outlined.Chat, "Lyrics")
    }
}



