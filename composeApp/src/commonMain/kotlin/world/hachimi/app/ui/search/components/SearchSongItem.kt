package world.hachimi.app.ui.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.formatSongDuration
import kotlin.time.Duration.Companion.seconds

@Composable
fun SearchSongItem(
    data: SongModule.SearchSongItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.clickable(onClick = onClick)) {
            Surface(
                Modifier.aspectRatio(1f).fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                color = LocalContentColor.current.copy(0.12f)
            ) {
                AsyncImage(
                    model = data.coverArtUrl,
                    contentDescription = "Song Cover Image",
                    contentScale = ContentScale.Crop
                )
            }
            Row(Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)) {
                Column(Modifier.weight(1f)) {

                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = data.title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1
                        )

                        if (data.explicit == true) Icon(
                            modifier = Modifier.padding(start = 8.dp).requiredSize(16.dp),
                            imageVector = Icons.Default.Explicit,
                            contentDescription = "Explicit",
                            tint = LocalContentColor.current.copy(0.72f),
                        )
                    }

                    Text(
                        text = data.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current.copy(0.72f),
                        maxLines = 1
                    )
                    Spacer(Modifier.weight(1f))
                    Text(data.uploaderName, style = MaterialTheme.typography.labelSmall, color = LocalContentColor.current.copy(0.6f))
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = "Duration", modifier = Modifier.size(12.dp))
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = formatSongDuration(data.durationSeconds.seconds),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Headphones, contentDescription = "Play Count", modifier = Modifier.size(12.dp))
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = data.playCount.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        SearchSongItem(data = SongModule.SearchSongItem(
            id = 0,
            displayId = "1",
            title = "Test Song Title",
            subtitle = "This is a test subtitle",
            description = "test",
            artist = "test",
            durationSeconds = 100,
            playCount = 100,
            likeCount = 100,
            coverArtUrl = "",
            audioUrl = "",
            uploaderUid = 0,
            uploaderName = "Author",
            explicit = true
        ))
    }
}