package world.hachimi.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MultiContentMeasurePolicy
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.theme.PreviewTheme
import kotlin.time.Duration.Companion.seconds

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
        tags = item.tags.map { it.name },
        likeCount = item.likeCount,
        playCount = item.playCount,
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
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
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
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
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
                onClick = {}
            )
        }
    }
}