package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import world.hachimi.app.getPlatform
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.isValidHttpsUrl

@Composable
fun AuthorAndPV(
    authorName: String,
    avatar: String?,
    hasMultipleArtists: Boolean,
    pvLink: String?,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier,
    pvAlignToEnd: Boolean
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        AmbientUserChip(
            onClick = onUserClick,
            avatar = rememberAsyncImagePainter(avatar, contentScale = ContentScale.Crop),
            name = authorName
        )
        if (hasMultipleArtists) {
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = "等人",
                style = subtitleStyle,
                color = LocalContentColor.current.copy(0.6f)
            )
        }

        pvLink?.takeIf { isValidHttpsUrl(it) }?.let {
            if (pvAlignToEnd) Spacer(Modifier.weight(1f))
            AmbientPVChip(
                platform = it,
                onClick = { getPlatform().openUrl(it) }
            )
        }
    }
}

private val subtitleStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
)
