package world.hachimi.app.ui.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil3.request.ImageRequest
import coil3.request.crossfade
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
        modifier = modifier.wrapContentHeight(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.clickable(onClick = onClick)) {
            Cover(coverUrl)

            Column(Modifier.padding(12.dp)) {
                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(description ?: "", style = TextStyle(fontSize = 12.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(songCount.toString(), style = TextStyle(fontSize = 12.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(avatarUrl)
                    Spacer(Modifier.width(8.dp))
                    Text(text = username, style = TextStyle(fontSize = 12.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun Cover(coverUrl: String?) {
    Surface(
        modifier = Modifier.size(88.dp),
        shape = RoundedCornerShape(8.dp),
        color = LocalContentColor.current.copy(0.12f)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current).data(coverUrl)
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