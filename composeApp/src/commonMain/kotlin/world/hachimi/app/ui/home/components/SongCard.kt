package world.hachimi.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.TagBadge
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.formatCompactCount

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
    Card(modifier = modifier.defaultMinSize(minWidth = 160.dp)) {
        Column(Modifier.clickable(onClick = onClick).padding(8.dp)) {
            Box(Modifier.aspectRatio(1f).background(LocalContentColor.current.copy(0.12f), RoundedCornerShape(8.dp))) {
                val hazeState = rememberHazeState()
                AsyncImage(
                    modifier = Modifier.hazeSource(hazeState).fillMaxSize().clip(RoundedCornerShape(8.dp)),
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .httpHeaders(CoilHeaders)
                        .data(coverUrl)
                        .crossfade(true)
                        .placeholderMemoryCacheKey(coverUrl)
                        .memoryCacheKey(coverUrl)
                        .build(),
                    contentDescription = "Song Cover Image",
                    contentScale = ContentScale.Crop
                )
                FlowRow(
                    Modifier.align(Alignment.TopStart).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.fastForEach { tag ->
                        TagBadge(hazeState, tag)
                    }
                }
                Row(
                    modifier = Modifier.padding(0.dp).align(Alignment.BottomEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TagBadge(hazeState, modifier = Modifier.padding(all = 8.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = "Play Count",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                formatCompactCount(playCount),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(Modifier.width(8.dp))

                            Icon(
                                Icons.Default.Favorite,
                                "Play Count",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                formatCompactCount(likeCount),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Column(Modifier.padding(top = 8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1f, fill = false),
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Explicit mark
                    if (explicit == true) Icon(
                        imageVector = Icons.Default.Explicit,
                        contentDescription = "Explicit",
                        modifier = Modifier.padding(start = 4.dp).requiredSize(16.dp)
                    )
                }
                Text(
                    modifier = Modifier,
                    text = author,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
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
