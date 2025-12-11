package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.components.Text

private val lyricsStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 36.sp,
    lineHeight = 48.sp
)

@Composable
fun Lyrics2(
    loading: Boolean,
    supportTimedLyrics: Boolean,
    currentLine: Int,
    lines: List<String>,
    lazyListState: LazyListState = rememberLazyListState(),
    centralizeFirstLine: Boolean = true,
    contentPadding: PaddingValues = PaddingValues.Zero,
    modifier: Modifier
) {
    var firstJump by remember { mutableStateOf(true) }

    BoxWithConstraints(modifier) {
        LaunchedEffect(currentLine) {
            if (currentLine == -1) {
                lazyListState.scrollToItem(0)
            } else {
                if (firstJump) {
                    lazyListState.scrollToItem(currentLine)
                    firstJump = false
                } else {
                    lazyListState.animateScrollToItem(currentLine)
                }
            }
        }

        if (loading) {
            Icon(
                modifier = Modifier.size(36.dp).align(Alignment.CenterStart),
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Loading",
                tint = LocalContentColor.current.copy(0.48f)
            )
        } else if (lines.isEmpty()) {
            Icon(
                modifier = Modifier.size(36.dp).align(Alignment.CenterStart),
                imageVector = Icons.Default.MusicNote,
                contentDescription = "No Lyrics",
                tint = LocalContentColor.current.copy(0.48f)
            )
        } else if (lines.isNotEmpty()) {
            // Let the first line be centered
            val textMeasurer = rememberTextMeasurer()
            val measuredLineHeight =
                remember(lines) { textMeasurer.measure(lines.first(), style = lyricsStyle).size.height }

            val middleToTopPx = constraints.maxHeight / 2 - measuredLineHeight / 2
            val middleToTop = with(LocalDensity.current) { middleToTopPx.toDp() }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = if (centralizeFirstLine) PaddingValues(
                    top = middleToTop + contentPadding.calculateTopPadding(),
                    bottom = maxHeight / 2 + contentPadding.calculateBottomPadding(),
                    start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = contentPadding.calculateEndPadding(LocalLayoutDirection.current)
                ) else contentPadding,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(lines) { index, line ->
                    val offsetToCurrent = currentLine - index
                    LyricsLine(
                        line = line,
                        active =
                            if (supportTimedLyrics) offsetToCurrent == 0
                            else true
                    )
                }
            }
        }
    }
}

@Composable
private fun LyricsLine(
    line: String,
    active: Boolean,
    activeAlpha: Float = 0.87f,
    inactiveAlpha: Float = 0.32f
) {
    val transition = updateTransition(active)
    val alpha by transition.animateFloat {
        if (it) activeAlpha else inactiveAlpha
    }
    if (line.isBlank()) Icon(
        imageVector = Icons.Default.MusicNote,
        contentDescription = "Interlude",
        tint = LocalContentColor.current.copy(alpha = alpha),
        modifier = Modifier.size(36.dp)
    ) else Text(
        text = line,
        color = LocalContentColor.current.copy(alpha = alpha),
        style = lyricsStyle
    )
}

