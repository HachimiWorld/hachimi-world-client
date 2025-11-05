package world.hachimi.app.ui.root.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.formatSongDuration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun MusicQueue(
    onClose: () -> Unit,
    queue: List<GlobalStore.MusicQueueItem>,
    playingSongId: Long?,
    onPlayClick: (Long) -> Unit,
    onRemoveClick: (Long) -> Unit,
    onClearClick: () -> Unit,
) {
    ElevatedCard {
        Column(Modifier.padding(24.dp).width(400.dp).height(600.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "播放队列", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(8.dp))
                Text(text = queue.size.toString(), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onClearClick) {
                    Text("清空")
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(vertical = 8.dp)) {
                itemsIndexed(queue, key = { _, item -> item.id }) { index, item ->
                    Item(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        isPlaying = item.id == playingSongId,
                        onPlayClick = { onPlayClick(item.id) },
                        name = item.name,
                        artist = item.artist,
                        duration = item.duration,
                        onRemoveClick = { onRemoveClick(item.id) },
                    )
                    if (index != queue.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Item(
    modifier: Modifier,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    name: String,
    artist: String,
    duration: Duration,
    onRemoveClick: () -> Unit
) {
    CompositionLocalProvider(
        LocalContentColor provides if (isPlaying) {
            MaterialTheme.colorScheme.primary
        } else {
            LocalContentColor.current
        }
    ) {
        Surface(
            modifier = modifier,
            onClick = onPlayClick,
            shape = MaterialTheme.shapes.medium,
            color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(formatSongDuration(duration), style = MaterialTheme.typography.labelSmall)
                IconButton(onClick = onRemoveClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove from playlist")
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        MusicQueue(
            {},
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