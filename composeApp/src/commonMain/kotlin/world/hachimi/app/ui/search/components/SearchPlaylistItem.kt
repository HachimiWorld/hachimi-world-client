package world.hachimi.app.ui.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun SearchPlaylistItem(
    title: String,
    description: String?,
    username: String,
    coverUrl: String?,
    avatarUrl: String?,
    songCount: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.clickable(onClick = onClick)) {
            Cover(
                coverUrl = coverUrl,
                modifier = Modifier.fillMaxHeight().aspectRatio(1f).padding(8.dp)
            )

            Column(Modifier.padding(8.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(description ?: "", style = TextStyle(fontSize = 12.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)

                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//                    Avatar(avatarUrl)
//                    Spacer(Modifier.width(8.dp))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = username,
                        style = TextStyle(fontSize = 12.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = LocalContentColor.current.copy(0.72f)
                    )

                    Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null)
                    Text(
                        text = songCount.toString(),
                        style = TextStyle(fontSize = 12.sp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Cover(
    coverUrl: String?,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = LocalContentColor.current.copy(0.12f)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current).data(coverUrl)
                .httpHeaders(CoilHeaders)
                .crossfade(true).build(),
            contentDescription = "Playlist Cover",
            contentScale = ContentScale.Crop
        )
    }

}
@Composable
private fun Avatar(url: String?) {
    Surface(
        modifier = Modifier.size(16.dp),
        shape = CircleShape,
        color = LocalContentColor.current.copy(0.12f)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current).data(url)
                .httpHeaders(CoilHeaders)
                .crossfade(true).build(),
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop
        )
    }
}
@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        SearchPlaylistItem(
            title = "Title",
            username = "Creator",
            coverUrl = null,
            avatarUrl = null,
            songCount = 10,
            onClick = {},
            modifier = Modifier,
            description = ""
        )
    }
}