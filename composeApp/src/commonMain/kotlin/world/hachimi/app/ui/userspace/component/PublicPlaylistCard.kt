package world.hachimi.app.ui.userspace.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.playlist_cover_cd
import hachimiworld.composeapp.generated.resources.user_space_playlist_songs_count
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.api.module.PlaylistModule
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import kotlin.time.Clock

@Composable
fun PublicPlaylistCard(
    playlist: PlaylistModule.PlaylistMetadata,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(8.dp),
                color = HachimiTheme.colorScheme.surface
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .httpHeaders(CoilHeaders)
                        .data(playlist.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(Res.string.playlist_cover_cd),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(Res.string.user_space_playlist_songs_count, playlist.songsCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = HachimiTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
@Preview
private fun Preview() {
    val mockPlaylist = remember {
        PlaylistModule.PlaylistMetadata(
            id = 1,
            userId = 1000,
            userName = "Hachimi",
            userAvatarUrl = null,
            name = "我的歌单",
            description = null,
            coverUrl = null,
            songsCount = 42,
            createTime = Clock.System.now(),
            updateTime = Clock.System.now()
        )
    }
    PreviewTheme(background = true) {
        PublicPlaylistCard(
            playlist = mockPlaylist,
            onClick = {}
        )
    }
}
