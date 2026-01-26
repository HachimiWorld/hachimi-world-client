package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.player_cover_cd
import hachimiworld.composeapp.generated.resources.player_queue_clear
import hachimiworld.composeapp.generated.resources.player_queue_title
import hachimiworld.composeapp.generated.resources.player_remove_from_playlist_cd
import org.jetbrains.compose.resources.stringResource
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
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero
) {
    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = remember(queue) {
            val idx = queue.indexOfFirst { it.id == playingSongId }
            if (idx == -1) 0
            else idx
        }
    )
    LazyColumn(
        modifier = modifier.defaultMinSize(minHeight = 400.dp),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = scrollState
    ) {
        items(queue, key = { item -> item.id }) { item ->
            Item(
                modifier = Modifier.fillParentMaxWidth().animateItem(),
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

@Composable
fun MusicQueueHeader(
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = stringResource(Res.string.player_queue_title), style = headStyle)
        ClearButton(onClick = onClearClick)
    }
}

private val headStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 24.sp,
    lineHeight = 32.sp
)

@Composable
private fun ClearButton(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = HachimiTheme.colorScheme.surface,
        contentColor = HachimiTheme.colorScheme.onSurface
    ) {
        Box(Modifier.clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(stringResource(Res.string.player_queue_clear))
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
        modifier = modifier.clickable(
            onClick = onPlayClick,
            indication = null,
            interactionSource = null
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(coverUrl).crossfade(true)
                    .build(),
                contentDescription = stringResource(Res.string.player_cover_cd),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(LocalContentColor.current.copy(0.12f)),
            )
            androidx.compose.animation.AnimatedVisibility(visible = isPlaying, enter = fadeIn(), exit = fadeOut()) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(0.12f)), Alignment.Center) {
                    Icon(Icons.Default.Headphones, "Playing", tint = Color.White)
                }
            }
        }
        Column(Modifier.height(48.dp).weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
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
        Text(
            text = formatSongDuration(duration),
            style = durationStyle,
            color = HachimiTheme.colorScheme.onSurfaceVariant
        )
        HachimiIconButton(onClick = onRemoveClick) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(Res.string.player_remove_from_playlist_cd))
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
            queue = remember {
                listOf(
                    GlobalStore.MusicQueueItem(0, "0", "Test Song 1", "Artist", 101.seconds, "", true),
                    GlobalStore.MusicQueueItem(1, "1", "Test Song 2", "Artist", 207.seconds, "", false),
                    GlobalStore.MusicQueueItem(2, "2", "Test Song 3", "Artist", 128.seconds, "", null),
                    GlobalStore.MusicQueueItem(3, "3", "Test Song 4", "Artist", 162.seconds, "", true),
                    GlobalStore.MusicQueueItem(4, "4", "Test Song 5", "Artist", 116.seconds, "", false),
                )
            },
            playingSongId = null,
            onPlayClick = {},
            onRemoveClick = {}
        )
    }
}