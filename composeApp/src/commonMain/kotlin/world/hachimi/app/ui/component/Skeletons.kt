package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.placeholder
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.contentPaddingForMaxWidth
import world.hachimi.app.ui.util.listHeadInsetsSpacerItem

// Stand-ins for the common rows and tiles while a page loads. Each keeps the size and the corners of
// the thing it stands for, so the content arrives in place instead of pushing the page around.

/**
 * A song row: the 48dp cover, the title and the artist, and a short value at the end (duration or
 * time), on the card the real rows sit on.
 *
 * @since 261112
 */
@Composable
fun SongRowSkeleton(modifier: Modifier = Modifier, index: Int = 0) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).placeholder(true, RoundedCornerShape(8.dp)))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Bar(TitleWidths[index % TitleWidths.size], 12.dp)
                Bar(0.3f, 10.dp)
            }
            Box(Modifier.padding(end = 8.dp).width(32.dp).height(10.dp).placeholder(true))
        }
    }
}

/**
 * A 100dp tall result row, cover square on the left and two lines beside it: songs and playlists
 * in search.
 *
 * @since 261112
 */
@Composable
fun MediaRowSkeleton(modifier: Modifier = Modifier, index: Int = 0) {
    Surface(modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(16.dp)) {
        Row {
            Box(Modifier.fillMaxHeight().aspectRatio(1f).padding(8.dp).placeholder(true, RoundedCornerShape(8.dp)))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Bar(TitleWidths[index % TitleWidths.size], 14.dp)
                Bar(0.35f, 10.dp)
            }
        }
    }
}

/**
 * A user tile: the round avatar over the name, as in the user results of search.
 *
 * @since 261112
 */
@Composable
fun UserTileSkeleton(modifier: Modifier = Modifier, avatarSize: Dp = 120.dp) {
    Surface(modifier, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(avatarSize).placeholder(true, CircleShape))
            Box(Modifier.padding(top = 12.dp).width(64.dp).height(12.dp).placeholder(true))
        }
    }
}

/**
 * A page-wide list of [SongRowSkeleton]s with the page's margins. [playAll] leaves the header row's
 * play-all button in place and [dateLabel] a day heading over the rows, for the pages that have them.
 *
 * @since 261112
 */
@Composable
fun SongListSkeleton(
    modifier: Modifier = Modifier,
    playAll: Boolean = false,
    dateLabel: Boolean = false,
    rowSpacing: Dp = 8.dp,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPaddingForMaxWidth(PaddingValues(AdaptiveScreenMargin), maxWidth),
            verticalArrangement = Arrangement.spacedBy(rowSpacing),
            userScrollEnabled = false,
        ) {
            listHeadInsetsSpacerItem()
            if (playAll) item(contentType = "header") {
                Row(Modifier.fillMaxWidth().height(36.dp), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.size(104.dp, 36.dp).placeholder(true, CircleShape))
                }
            }
            if (dateLabel) item(contentType = "date") {
                Box(Modifier.height(20.dp), contentAlignment = Alignment.CenterStart) {
                    Box(Modifier.width(56.dp).height(12.dp).placeholder(true))
                }
            }
            items(SkeletonRows, contentType = { "row" }) { SongRowSkeleton(index = it) }
        }
    }
}

/**
 * A plain list for the tool pages (creation, review, committee, devices): rows without cards, each
 * an optional [leading] square or circle, two lines and a short value at the end. [header] leaves a
 * title over the rows, with a button at its end when [headerAction] is set.
 *
 * @since 261112
 */
@Composable
fun PlainListSkeleton(
    modifier: Modifier = Modifier,
    leading: Dp? = 42.dp,
    leadingShape: Shape = RoundedCornerShape(8.dp),
    header: Boolean = false,
    headerAction: Boolean = false,
) {
    Column(modifier.fillMaxSize().clipToBounds().wrapContentHeight(Alignment.Top, unbounded = true)) {
        if (header) Row(
            Modifier.fillMaxWidth().padding(AdaptiveScreenMargin).height(36.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.width(120.dp).height(18.dp).placeholder(true))
            Spacer(Modifier.weight(1f))
            if (headerAction) Box(Modifier.size(88.dp, 36.dp).placeholder(true, CircleShape))
        }
        repeat(SkeletonRows) { index ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (leading != null) {
                    Box(Modifier.size(leading).placeholder(true, leadingShape))
                    Spacer(Modifier.width(16.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Bar(TitleWidths[index % TitleWidths.size], 12.dp)
                    Bar(0.3f, 10.dp)
                }
                Box(Modifier.padding(start = 16.dp).width(40.dp).height(10.dp).placeholder(true))
            }
        }
    }
}

/**
 * A page of reading: a title, a line of details, then paragraphs and a block. For detail pages
 * whose layout varies with what they show (a report, a review, a changelog).
 *
 * @since 261112
 */
@Composable
fun DetailSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().clipToBounds().wrapContentHeight(Alignment.Top, unbounded = true).padding(horizontal = AdaptiveScreenMargin, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Bar(0.55f, 20.dp)
        Bar(0.35f, 10.dp)
        Spacer(Modifier.height(8.dp))
        repeat(2) { paragraph ->
            repeat(3) { Bar(1f, 12.dp) }
            Bar(if (paragraph == 0) 0.6f else 0.4f, 12.dp)
            Spacer(Modifier.height(8.dp))
        }
        Box(Modifier.fillMaxWidth().height(120.dp).placeholder(true, RoundedCornerShape(16.dp)))
    }
}

@Composable
private fun Bar(fraction: Float, height: Dp) {
    Box(Modifier.fillMaxWidth(fraction).height(height).placeholder(true))
}

/** Title lengths that vary from row to row, so a column of stand-ins doesn't read as a table. */
private val TitleWidths = listOf(0.62f, 0.48f, 0.7f, 0.55f, 0.4f)
private const val SkeletonRows = 14
