package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.formatSongDuration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun MusicQueue(
    queue: List<GlobalStore.MusicQueueItem>,
    playingSongId: Long?,
    onPlayClick: (Long) -> Unit,
    onRemoveClick: (Long) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "播放队列", style = headStyle)
            ClearButton(onClick = onClearClick)
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().defaultMinSize(minHeight = 400.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(queue, key = { _, item -> item.id }) { index, item ->
                Item(
                    modifier = Modifier.fillMaxWidth().animateItem(),
                    isPlaying = item.id == playingSongId,
                    onPlayClick = { onPlayClick(item.id) },
                    coverUrl = item.coverUrl,
                    name = item.name,
                    artist = item.artist,
                    duration = item.duration,
                    onRemoveClick = { onRemoveClick(item.id) },
                )
            }
        }
    }
}

private val headStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 24.sp,
    lineHeight = 32.sp
)
@Composable
private fun ClearButton(onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = HachimiTheme.colorScheme.surface, contentColor = HachimiTheme.colorScheme.onSurface) {
        Box(Modifier.clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("清空")
        }
    }
}

@Composable
private fun Item(
    modifier: Modifier,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    coverUrl: String,
    name: String,
    artist: String,
    duration: Duration,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable(
            onClick = onPlayClick,
            indication = null,
            interactionSource = null
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(coverUrl).crossfade(true)
                .build(),
            contentDescription = "Cover",
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(LocalContentColor.current.copy(0.12f)),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                style = titleStyle,
                color = HachimiTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = artist,
                style = authorStyle,
                color = HachimiTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(text = formatSongDuration(duration), style =durationStyle, color = HachimiTheme.colorScheme.onSurfaceVariant)
        HachimiIconButton(onClick = onRemoveClick) {
            Icon(Icons.Filled.Close, contentDescription = "Remove from playlist")
        }
    }
}

private val titleStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

private val authorStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

private val durationStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
)

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        MusicQueue(
            remember {
                listOf(
                    GlobalStore.MusicQueueItem(0, "0", "Test Song 1", "Artist", 101.seconds, "", true),
                    GlobalStore.MusicQueueItem(1, "1", "Test Song 2", "Artist", 207.seconds, "", false),
                    GlobalStore.MusicQueueItem(2, "2", "Test Song 3", "Artist", 128.seconds, "", null),
                    GlobalStore.MusicQueueItem(3, "3", "Test Song 4", "Artist", 162.seconds, "", true),
                    GlobalStore.MusicQueueItem(4, "4", "Test Song 5", "Artist", 116.seconds, "", false),
                )
            },
            null,
            {},
            {},
            {})
    }
}