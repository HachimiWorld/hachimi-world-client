package world.hachimi.app.ui.playlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.chrisbanes.haze.*
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
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
    Surface(
        modifier = modifier.defaultMinSize(minWidth = 160.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.clickable(onClick = onEnter).padding(8.dp)) {
            Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
                val hazeState = rememberHazeState()
                AsyncImage(
                    modifier = Modifier.hazeSource(hazeState).fillMaxSize().clip(RoundedCornerShape(8.dp))
                        .background(LocalContentColor.current.copy(alpha = 0.12f)),
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Playlist Cover Image",
                    contentScale = ContentScale.Crop
                )
                Row(
                    Modifier.align(Alignment.BottomStart).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.clip(CircleShape).hazeEffect(
                            hazeState, style = HazeStyle(
                                backgroundColor = HachimiTheme.colorScheme.surface,
                                blurRadius = 12.dp,
                                tint = HazeTint(color = HachimiTheme.colorScheme.surface)
                            )
                        ),
                        color = Color.Transparent,
                        shape = CircleShape,
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            text = "$songCount 首",
                            style = TextStyle(fontSize = 12.sp),
                            color = HachimiTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Column(Modifier.padding(top = 8.dp)) {
                Text(
                    text = title,
                    style = TextStyle(fontSize = 14.sp),
                    color = HachimiTheme.colorScheme.onSurface,
                    maxLines = 1
                )
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