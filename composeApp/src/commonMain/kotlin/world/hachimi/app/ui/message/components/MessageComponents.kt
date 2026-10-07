package world.hachimi.app.ui.message.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.ui.design.HachimiTheme

private val CardRadius = 16.dp

/**
 * Puts [items] in one card, like the settings page: the first row rounds the top, the last the
 * bottom, and rows are separated by a line.
 */
fun <T> LazyListScope.cardItems(
    items: List<T>,
    key: (T) -> Any,
    content: @Composable (T) -> Unit,
) {
    itemsIndexed(items, key = { _, item -> key(item) }) { index, item ->
        val top = if (index == 0) CardRadius else 0.dp
        val bottom = if (index == items.lastIndex) CardRadius else 0.dp
        Column(
            Modifier
                .clip(RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom))
                .background(HachimiTheme.colorScheme.surface)
        ) {
            if (index > 0) {
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp)
                        .background(HachimiTheme.colorScheme.outline)
                )
            }
            content(item)
        }
    }
}

/** A small dot at the left edge, vertically centered, marking an unread row. */
@Composable
fun Modifier.unreadDot(show: Boolean): Modifier {
    if (!show) return this
    val color = HachimiTheme.colorScheme.primary
    return drawWithContent {
        drawContent()
        drawCircle(color, radius = 3.dp.toPx(), center = Offset(8.dp.toPx(), size.height / 2))
    }
}

@Composable
fun RemoteImage(url: String?, modifier: Modifier = Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .httpHeaders(CoilHeaders)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = modifier.background(HachimiTheme.colorScheme.onSurface.copy(0.08f)),
        contentScale = ContentScale.Crop,
    )
}
