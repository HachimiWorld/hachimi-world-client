package world.hachimi.app.ui.playlist.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import world.hachimi.app.ui.theme.PreviewTheme
import kotlin.time.Instant

@Composable
fun PlaylistItem(
    coverUrl: String?,
    title: String,
    songCount: Int,
    createTime: Instant,
    onEnter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier, onClick = onEnter) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant) {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = coverUrl,
                        contentDescription = "Playlist Cover Image",
                        contentScale = ContentScale.Crop
                    )
                }
                Row(
                    Modifier.align(Alignment.BottomStart).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            text = "$songCount 首", style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Column(Modifier.padding(vertical = 8.dp, horizontal = 8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                /*Text(
                    text = author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )*/
            }
        }
    }
}

@Composable
@Preview
private fun Preview() {
    PreviewTheme(background = false) {
        PlaylistItem(
            modifier = Modifier.width(250.dp),
            coverUrl = "",
            title = "Top 100 songs",
            songCount = 100,
            createTime = remember { Instant.parse("2023-04-01T00:00:00Z") },
            onEnter = {}
        )
    }
}