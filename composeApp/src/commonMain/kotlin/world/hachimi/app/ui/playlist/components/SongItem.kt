package world.hachimi.app.ui.playlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_more
import hachimiworld.composeapp.generated.resources.playlist_remove_item
import hachimiworld.composeapp.generated.resources.song_cover_cd
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.formatSongDuration
import kotlin.time.Duration

@Composable
fun SongItem(
    orderIndex: Int,
    coverUrl: String,
    title: String,
    artist: String,
    duration: Duration,
    editable: Boolean,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = if (editable) { { dropdownExpanded = true } } else null
            ).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(LocalContentColor.current.copy(0.12f))) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .httpHeaders(CoilHeaders)
                        .data(coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(Res.string.song_cover_cd),
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    artist,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(formatSongDuration(duration), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(4.dp))

            if (editable) Box {
                HachimiIconButton(onClick = { dropdownExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(Res.string.common_more))
                }
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }) {
                    DropdownMenuItem(onClick = {
                        onRemoveClick()
                        dropdownExpanded = false
                    }, text = {
                        Text(stringResource(Res.string.playlist_remove_item))
                    })
                }
            }
        }
    }
}