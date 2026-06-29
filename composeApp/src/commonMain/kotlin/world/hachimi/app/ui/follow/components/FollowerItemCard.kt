package world.hachimi.app.ui.follow.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.follow_mutual
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.model.FollowViewModel
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text

@Composable
fun FollowerItemCard(
    item: UserModule.FollowerItem,
    vm: FollowViewModel,
    isCompact: Boolean
) {
    val cardRadius = if (isCompact) 12.dp else 14.dp
    val avatarSize = if (isCompact) 48.dp else 56.dp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cardRadius),
        color = HachimiTheme.colorScheme.surface,
        contentColor = HachimiTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.clickable { vm.navigateToSpace(item.user.uid) }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val avatarModifier = Modifier.size(avatarSize).clip(CircleShape)
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .httpHeaders(CoilHeaders)
                    .data(item.user.avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = avatarModifier,
                filterQuality = FilterQuality.High,
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f).padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.user.username,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (item.isMutual) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HachimiTheme.colorScheme.secondaryContainer,
                            contentColor = HachimiTheme.colorScheme.secondary
                        ) {
                            Text(
                                text = stringResource(Res.string.follow_mutual),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color = HachimiTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                if (!item.user.bio.isNullOrEmpty() && !item.user.isBanned) {
                    Text(
                        text = item.user.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = HachimiTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.followedAt.isNotEmpty()) {
                    Text(
                        text = item.followedAt,
                        fontSize = 12.sp,
                        color = HachimiTheme.colorScheme.onSurface.copy(0.36f)
                    )
                }
            }
        }
    }
}
