package world.hachimi.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.components.placeholder
import world.hachimi.app.ui.util.AdaptiveListSpacing
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.calculateGridColumns
import world.hachimi.app.ui.util.contentPaddingForMaxWidth
import world.hachimi.app.ui.util.listHeadInsetsSpacerItem

/**
 * Stands in for a page-wide grid of [SongCard]s: the same columns and margins, the header row with
 * its play-all button, then cards to the bottom of the screen. It doesn't scroll.
 *
 * [playAll] leaves room for the play-all button; [dateLabel] adds a short bar over the cards, for
 * the pages that group songs by day. Playlist grids use it too, without the button.
 *
 * @since 261112
 */
@Composable
fun SongCardGridSkeleton(modifier: Modifier = Modifier, playAll: Boolean = true, dateLabel: Boolean = false) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = calculateGridColumns(maxWidth),
            contentPadding = contentPaddingForMaxWidth(PaddingValues(AdaptiveScreenMargin), maxWidth),
            horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            userScrollEnabled = false,
        ) {
            listHeadInsetsSpacerItem()
            if (playAll) item(span = { GridItemSpan(maxLineSpan) }, contentType = "header") {
                // The play-all button sits at the end of the header row
                Row(Modifier.fillMaxWidth().height(HeaderHeight), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.size(PlayAllWidth, HeaderHeight).placeholder(true, CircleShape))
                }
            }
            if (dateLabel) item(span = { GridItemSpan(maxLineSpan) }, contentType = "date") {
                Box(Modifier.height(24.dp), contentAlignment = Alignment.CenterStart) {
                    Box(Modifier.width(56.dp).height(14.dp).placeholder(true))
                }
            }
            items(SkeletonCards, contentType = { "card" }) {
                SongCardSkeleton(Modifier.fillMaxWidth())
            }
        }
    }
}

private val HeaderHeight = 36.dp
private val PlayAllWidth = 104.dp
private const val SkeletonCards = 18
