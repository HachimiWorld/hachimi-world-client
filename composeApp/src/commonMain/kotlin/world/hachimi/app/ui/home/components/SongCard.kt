package world.hachimi.app.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun SongCard(
    modifier: Modifier,
    item: SongModule.PublicSongDetail,
    onClick: () -> Unit
) {
    SongCard(
        modifier = modifier,
        coverUrl = item.coverUrl,
        title = item.title,
        subtitle = item.subtitle,
        author = item.uploaderName,
        tags = remember(item) { item.tags.map { it.name } },
        likeCount = item.likeCount,
        playCount = item.playCount,
        explicit = item.explicit,
        onClick = onClick,
    )
}

@Composable
fun SongCard(
    coverUrl: String,
    title: String,
    subtitle: String,
    author: String,
    tags: List<String>,
    playCount: Long,
    likeCount: Long,
    explicit: Boolean?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.defaultMinSize(minWidth = 160.dp),
        onClick = onClick,
        shape = CardDefaults.shape,
        color = CardDefaults.cardColors().containerColor
    ) {
        Column {
            Box(Modifier.aspectRatio(1f)) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                FlowRow(
                    Modifier.align(Alignment.TopStart).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        Surface(
                            color = Color(0x99FFFFFF),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                text = tag,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0x99000000),
                            )
                        }
                    }
                }
            }

            Column(Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier,
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    // Explicit mark
                    if (explicit == true) Icon(
                        imageVector = Icons.Default.Explicit,
                        contentDescription = "Explicit",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 4.dp).requiredSize(16.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Icon(
                        Icons.Default.Headphones,
                        "Play Count",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp).size(12.dp)
                    )
                    Text(
                        playCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SongCardInHorizontal(
    coverUrl: String,
    title: String,
    subtitle: String,
    author: String,
    tags: List<String>,
    playCount: Long,
    likeCount: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.defaultMinSize(minHeight = 200.dp),
        onClick = onClick,
        shape = CardDefaults.shape,
        color = CardDefaults.cardColors().containerColor
    ) {
        Layout(
            content = {
                Box(Modifier.aspectRatio(1f)) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    FlowRow(
                        Modifier.align(Alignment.TopStart).padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tags.forEach { tag ->
                            Surface(
                                color = Color(0x99FFFFFF),
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    text = tag,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0x99000000),
                                )
                            }
                        }
                    }
                }
                Column(Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Icon(Icons.Default.Explicit, "Explicit", tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.requiredSize(16.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Icon(
                            Icons.Default.Headphones,
                            "Play Count",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp).size(12.dp)
                        )
                        Text(
                            playCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            measurePolicy = { measurables, constraints ->
                val cover = measurables[0].measure(constraints)
                val content = measurables[1].measure(constraints.copy(maxWidth = cover.width))

                layout(cover.width, cover.height + content.height) {
                    cover.place(0, 0)
                    content.place(0, cover.height)
                }
            }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        SongCard(
            coverUrl = "",
            title = "Test Song",
            subtitle = "Artist",
            author = "Album",
            tags = listOf("Tag 1", "Tag 2"),
            playCount = 1000,
            likeCount = 100,
            explicit = true,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    PreviewTheme(background = true) {
        Box(Modifier.height(180.dp)) {
            SongCard(
                modifier = Modifier.fillMaxHeight(),
                coverUrl = "",
                title = "Test Song",
                subtitle = "Artist",
                author = "Album",
                tags = listOf("Tag 1", "Tag 2"),
                playCount = 1000,
                likeCount = 100,
                explicit = false,
                onClick = {}
            )
        }
    }
}