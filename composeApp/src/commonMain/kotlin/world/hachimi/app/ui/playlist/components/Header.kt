package world.hachimi.app.ui.playlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.play_all
import hachimiworld.composeapp.generated.resources.playlist_cover_cd
import hachimiworld.composeapp.generated.resources.playlist_description_placeholder
import hachimiworld.composeapp.generated.resources.song_cover_cd
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.ui.util.fillMaxWidthIn
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun Header(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    coverUrl: String?,
    username: String,
    avatarUrl: String?,
    updateTime: Instant,
    count: Int,
    onPlayAllClick: () -> Unit,
    onNavToUserClick: () -> Unit,
    coverOverlay: (@Composable BoxScope.(HazeState) -> Unit)? = null,
    extraActions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth().height(200.dp)) {
            Box(
                modifier = Modifier.size(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(LocalContentColor.current.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                val hazeState = rememberHazeState()
                AsyncImage(
                    modifier = Modifier.hazeSource(hazeState).fillMaxSize(),
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .httpHeaders(CoilHeaders)
                        .data(coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(Res.string.playlist_cover_cd),
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(LocalContentColor.current.copy(alpha = 0.12f))
                )
                coverOverlay?.invoke(this, hazeState)
            }

            Column(modifier = Modifier.weight(1f).padding(start = 24.dp)) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                InfoSection(avatarUrl, onNavToUserClick, username, count, updateTime)

                Text(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp),
                    text = description
                        ?: stringResource(Res.string.playlist_description_placeholder),
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis,
                    color = HachimiTheme.colorScheme.onSurfaceVariant
                )

                // Actions
                Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        modifier = Modifier,
                        onClick = onPlayAllClick
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = stringResource(Res.string.song_cover_cd)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(stringResource(Res.string.play_all))
                    }

                    extraActions()
                }
            }
        }
    }
}


@Composable
@Preview
private fun PreviewHeader() {
    PreviewTheme(background = true) {
        Header(
            modifier = Modifier.fillMaxWidthIn(),
            title = "Title",
            description = "Description",
            coverUrl = null,
            username = "username",
            updateTime = remember { Clock.System.now() },
            avatarUrl = null,
            count = 10,
            onPlayAllClick = {},
            onNavToUserClick = {},
        )
    }
}