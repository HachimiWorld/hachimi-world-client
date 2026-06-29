package world.hachimi.app.ui.userspace.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.follow_follow
import hachimiworld.composeapp.generated.resources.follow_followers
import hachimiworld.composeapp.generated.resources.follow_following
import hachimiworld.composeapp.generated.resources.follow_following_label
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.model.FollowViewModel
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.SubtleButton
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.formatCompactCount

@Composable
fun StatsRow(
    followerCount: Long,
    followingCount: Long,
    myself: Boolean,
    isFollowing: Boolean?,
    isFollowLoading: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                value = formatCompactCount(followerCount),
                label = stringResource(Res.string.follow_followers),
                clickable = myself,
                onClick = onFollowersClick
            )
            StatItem(
                value = formatCompactCount(followingCount),
                label = stringResource(Res.string.follow_following),
                clickable = myself,
                onClick = onFollowingClick
            )
        }

        // Follow button (only for non-own profiles, per design spec)
        if (!myself) {
            Spacer(Modifier.width(16.dp))
            if (isFollowLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
            } else if (isFollowing == true) {
                SubtleButton(
                    onClick = onUnfollow,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        stringResource(Res.string.follow_following_label),
                        fontSize = 14.sp
                    )
                }
            } else {
                AccentButton(
                    onClick = onFollow,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        stringResource(Res.string.follow_follow),
                        fontSize = 14.sp,
                        color = HachimiTheme.colorScheme.onSurfaceReverse
                    )
                }
            }
        }
    }
}

@Composable
fun StatsRow(
    profile: UserModule.PublicUserProfile,
    myself: Boolean,
    isCompact: Boolean,
    followVM: FollowViewModel,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StatsRow(
        followerCount = profile.followerCount,
        followingCount = profile.followingCount,
        myself = myself,
        isFollowing = profile.isFollowing,
        isFollowLoading = followVM.actionLoading && followVM.uid == profile.uid,
        onFollow = { followVM.followUser(profile.uid) },
        onUnfollow = { followVM.showUnfollowDialog(profile.uid, profile.username) },
        onFollowersClick = onFollowersClick,
        onFollowingClick = onFollowingClick,
        modifier = modifier
    )
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    clickable: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = if (clickable) Modifier.clickable(onClick = onClick) else Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = HachimiTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
@Preview
private fun Preview() {
    PreviewTheme(background = true) {
        StatsRow(
            followerCount = 1234,
            followingCount = 56,
            myself = false,
            isFollowing = false,
            isFollowLoading = false,
            onFollow = {},
            onUnfollow = {},
            onFollowersClick = {},
            onFollowingClick = {}
        )
    }
}
