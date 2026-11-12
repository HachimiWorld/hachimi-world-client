package world.hachimi.app.ui.playlist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.component.SongRowSkeleton
import world.hachimi.app.ui.design.components.placeholder
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.ui.util.contentPaddingForMaxWidth
import world.hachimi.app.ui.util.listHeadInsetsSpacerItem

/**
 * Stands in for a playlist page while it loads: the header in the shape [CompactHeader] or [Header]
 * takes at this width, then song rows.
 *
 * @since 261112
 */
@Composable
fun PlaylistDetailSkeleton(modifier: Modifier = Modifier) {
    val compact = LocalWindowSize.current.width < WindowSize.COMPACT
    BoxWithConstraints(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPaddingForMaxWidth(PaddingValues(AdaptiveScreenMargin), maxWidth),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false,
        ) {
            listHeadInsetsSpacerItem()
            item(contentType = "header") {
                if (compact) CompactHeaderSkeleton() else WideHeaderSkeleton()
            }
            items(SkeletonRows, contentType = { "row" }) { SongRowSkeleton(index = it) }
        }
    }
}

@Composable
private fun CompactHeaderSkeleton() {
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).placeholder(true, RoundedCornerShape(24.dp)))
        HeaderLines(Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun WideHeaderSkeleton() {
    Row(Modifier.fillMaxWidth().height(200.dp).padding(bottom = 8.dp)) {
        Box(Modifier.size(200.dp).placeholder(true, RoundedCornerShape(24.dp)))
        HeaderLines(Modifier.weight(1f).padding(start = 24.dp))
    }
}

/** Title, the owner and count line, the description, and the play-all button. */
@Composable
private fun HeaderLines(modifier: Modifier) {
    Column(modifier) {
        Box(Modifier.width(180.dp).height(16.dp).placeholder(true))
        Box(Modifier.padding(top = 14.dp).width(220.dp).height(10.dp).placeholder(true))
        Box(Modifier.padding(top = 14.dp).fillMaxWidth(0.8f).height(10.dp).placeholder(true))
        Box(Modifier.padding(top = 20.dp).size(104.dp, 36.dp).placeholder(true, CircleShape))
    }
}

private const val SkeletonRows = 8
