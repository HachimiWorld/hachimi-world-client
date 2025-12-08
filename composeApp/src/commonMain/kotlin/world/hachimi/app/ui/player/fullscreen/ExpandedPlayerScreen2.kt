package world.hachimi.app.ui.player.fullscreen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.asPainter
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance
import world.hachimi.app.getPlatform
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PlayerUIState
import world.hachimi.app.ui.calculateAvgColor
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.insets.currentSafeAreaInsets
import world.hachimi.app.ui.player.components.*
import world.hachimi.app.ui.player.fullscreen.components.Page
import world.hachimi.app.ui.player.fullscreen.components.PagerButtons
import world.hachimi.app.util.isValidHttpsUrl
import kotlin.math.roundToInt

@Composable
fun ExpandedPlayerScreen2(
    global: GlobalStore = koinInject()
) {
    val uiState = global.player.playerState
    val context = LocalPlatformContext.current
    var painter by remember { mutableStateOf<Painter?>(null) }
    var dominantColor by remember { mutableStateOf(Color.Gray) }

    LaunchedEffect(uiState.displayedCover) {
        val request = ImageRequest.Builder(context)
            .data(uiState.displayedCover)
            .size(Size.ORIGINAL)
            // .size(widthPx, heightPx)
            .build()

        val result = SingletonImageLoader.get(context).execute(request)
        when (result) {
            is ErrorResult -> {
                painter = ColorPainter(Color.Gray)
                dominantColor = Color.Gray
            }
            is SuccessResult -> {
                val image = result.image
                val avgColor = withContext(Dispatchers.Default) { calculateAvgColor(image) }
                println("AVG Color: $avgColor, Luminance: ${avgColor.luminance()}")
                painter = image.asPainter(context, FilterQuality.None)
                dominantColor = avgColor
            }
        }
    }

    BackgroundContainer(
        painter = painter,
        dominantColor = dominantColor
    ) {
        ShrinkButton(
            modifier = Modifier.padding(32.dp).padding(top = currentSafeAreaInsets().top).align(Alignment.TopEnd),
            onClick = global::shrinkPlayer
        )
        Content(global, uiState)
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

    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        LeftPane(
            modifier = Modifier.weight(1f).fillMaxHeight().hoverable(leftPaneInteractionSource),
            header = {
                JmidLabel(
                    Modifier/*.border(1.dp, Color.Yellow)*/
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
                    modifier = Modifier/*.border(1.dp, Color.Yellow)*/
                        .padding(top = 12.dp),
                    global = global,
                    hideInfo = currentPage == Page.Info,
                    uiState = uiState,
                    hovered = leftPaneHovered
                )
            },
            onCoverLayout = { topLeft, size ->
                coverTopLeft = topLeft
                coverSize = size
            }
        )
        Box(Modifier.weight(1f)) {
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
                        }
                    )

                    Page.Queue -> Box(Modifier.fillMaxSize())
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
            PagerButtons(
                modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp),
                currentPage = currentPage,
                onPageSelect = { currentPage = it }
            )
        }
    }
}

@Composable
private fun ShrinkButton(modifier: Modifier, onClick: () -> Unit) {
    HachimiIconButton(modifier = modifier, onClick = onClick) {
        Icon(Icons.Default.CloseFullscreen, "Close")
    }
}

typealias TabTransitionSpec = AnimatedContentTransitionScope<Page?>.() -> ContentTransform

@Composable
fun rememberTabTransitionSpec(): TabTransitionSpec {
    val sliderDistance = rememberSlideDistance()
    val spec: TabTransitionSpec = {
        val initialState = initialState
        val targetState = targetState

        if (initialState == null || targetState == null) {
            fadeIn() togetherWith fadeOut()
        } else if (targetState < initialState) {
            // Slide to the left page
            materialSharedAxisX(forward = false, slideDistance = sliderDistance)
        } else {
            // Slide to the right page
            materialSharedAxisX(forward = true, slideDistance = sliderDistance)
        }
    }
    return spec
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
    modifier: Modifier = Modifier
) {
    val showControl = hideInfo || hovered

    Column(modifier) {
        PlayerProgress(
            durationMillis = uiState.displayedDurationMillis,
            currentMillis = uiState.displayedCurrentMillis,
            onProgressChange = { global.player.setSongProgress(it) },
            bufferingProgress = uiState.downloadProgress,
            trackColor = HachimiTheme.colorScheme.onSurfaceReverse.copy(0.1f),
            barColor = HachimiTheme.colorScheme.onSurfaceReverse.copy(1f)
        )

        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(visible = !hideInfo) {
            AuthorAndPV(
                authorName = uiState.displayedAuthor,
                hasMultipleArtists = uiState.songInfo?.productionCrew.orEmpty().size > 1,
                pvLink = uiState.readySongInfo?.externalLinks?.firstOrNull()?.url,
                pvAlignToEnd = true
            )
        }

        Box(Modifier.weight(1f)) {
            Crossfade(showControl) { showControl ->
                if (showControl) Controls(
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    playing = uiState.isPlaying,
                    onPreviousClick = { global.player.previous() },
                    onPlayOrPauseClick = { global.player.playOrPause() },
                    onNextClick = { global.player.next() },
                    onAddToPlaylistClick = {
                        // TODO:
                    }
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
) {
    Row(modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        ControlButton({}) {
            if (false) Icon(Icons.Default.Favorite, "Favorite On", tint = LocalContentColor.current.copy(0.6f))
            else Icon(Icons.Default.FavoriteBorder, "Favorite Off", tint = LocalContentColor.current.copy(0.6f))
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
            Icon(Icons.Default.Add, "Add to playlist", tint = LocalContentColor.current.copy(0.6f))
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
private fun BriefInfo(
    modifier: Modifier,
    title: String,
    subtitle: String?,
    description: String?
) {
    Column(modifier) {
        Titles(title = title, subtitle = subtitle)

        description?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                style = subtitleStyle, color = LocalContentColor.current.copy(0.6f),
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AuthorAndPV(authorName: String, hasMultipleArtists: Boolean, pvLink: String?, modifier: Modifier = Modifier, pvAlignToEnd: Boolean) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        AmbientUserChip(
            onClick = {
                // TODO: Nav to user space
            },
            avatar = null, // TODO
            name = authorName
        )
        if (hasMultipleArtists) {
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = "等人",
                style = subtitleStyle,
                color = LocalContentColor.current.copy(0.6f)
            )
        }

        pvLink?.takeIf { isValidHttpsUrl(it) }?.let {
            if (pvAlignToEnd) Spacer(Modifier.weight(1f))
            AmbientPVChip(
                platform = it,
                onClick = { getPlatform().openUrl(it) }
            )
        }
    }
}

