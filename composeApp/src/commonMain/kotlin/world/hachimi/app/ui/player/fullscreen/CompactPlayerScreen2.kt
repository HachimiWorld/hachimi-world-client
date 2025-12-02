package world.hachimi.app.ui.player.fullscreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PlayerUIState
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.insets.currentSafeAreaInsets
import world.hachimi.app.ui.player.components.PlayerProgress
import world.hachimi.app.ui.player.fullscreen.components.Page
import world.hachimi.app.ui.player.fullscreen.components.PagerButtons2

@Composable
fun CompactPlayerScreen2(
    global: GlobalStore = koinInject()
) {
    val uiState = global.player.playerState

    BackgroundContainer(
        rememberAsyncImagePainter(
            model = uiState.displayedCover,
            filterQuality = FilterQuality.None,
            placeholder = ColorPainter(HachimiTheme.colorScheme.onSurface)
        )
    ) {
        Column(
            Modifier.fillMaxSize()
                .padding(top = currentSafeAreaInsets().top, bottom = currentSafeAreaInsets().bottom)
                .padding(vertical = 24.dp, horizontal = 32.dp)
        ) {
            var currentPage by remember { mutableStateOf<Page?>(null) }

            HachimiIconButton(onClick = { global.shrinkPlayer() }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Shrink")
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = rememberTabTransitionSpec()
                ) { tab ->
                    when (tab) {
                        null -> PlayerTab(uiState, global)
                        Page.Info -> InfoTab()
                        Page.Queue -> QueueTab()
                        Page.Lyrics -> LyricsTab()
                    }
                }
            }

            PagerButtons2(
                modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                currentPage = currentPage,
                onPageSelect = {
                    currentPage =
                        if (currentPage == it) null // Toggle off
                        else it
                }
            )
        }
    }
}

@Composable
private fun PlayerTab(uiState: PlayerUIState, global: GlobalStore) {
    Column(Modifier.padding(top = 32.dp)) {
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
                    hasMultipleArtists = uiState.songInfo?.productionCrew.orEmpty().size > 1,
                    pvLink = uiState.readySongInfo?.externalLinks?.firstOrNull()?.url
                )
            }
            HachimiIconButton(onClick = {
                // TODO:
            }) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
            }
            HachimiIconButton(onClick = {
                // TODO:
            }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        }

        PlayerProgress(
            modifier = Modifier.padding(top = 24.dp),
            durationMillis = uiState.displayedDurationMillis,
            currentMillis = uiState.displayedCurrentMillis,
            bufferingProgress = uiState.downloadProgress,
            onProgressChange = { global.player.setSongProgress(it) },
            trackColor = HachimiTheme.colorScheme.onSurfaceReverse.copy(0.1f),
            barColor = HachimiTheme.colorScheme.onSurfaceReverse.copy(1f),
            timeOnTop = false,
            touchMode = true
        )

        ControlButtons(
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
            playing = uiState.isPlaying,
            onPreviousClick = { global.player.previous() },
            onNextClick = { global.player.next() },
            onPlayClick = { global.player.playOrPause() },
            onPauseClick = { global.player.playOrPause() }
        )
    }
}

@Composable
private fun InfoTab() {

}

@Composable
private fun LyricsTab() {
    Column(Modifier.fillMaxSize()) {

    }
}

@Composable
private fun QueueTab() {

}

@Composable
private fun Header(
    modifier: Modifier = Modifier,
) {

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
        contentColor = HachimiTheme.colorScheme.onSurfaceReverse
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
        color = HachimiTheme.colorScheme.onSurfaceReverse,
        contentColor = HachimiTheme.colorScheme.onSurface
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
        contentColor = HachimiTheme.colorScheme.onSurfaceReverse
    ) {
        Box(
            modifier = Modifier.clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next")
        }
    }
}