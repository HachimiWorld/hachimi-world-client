package world.hachimi.app.ui.message

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.follow_empty_followers
import hachimiworld.composeapp.generated.resources.follow_empty_followers_subtitle
import hachimiworld.composeapp.generated.resources.follow_mutual
import hachimiworld.composeapp.generated.resources.message_channel_followers
import hachimiworld.composeapp.generated.resources.message_load_error
import hachimiworld.composeapp.generated.resources.notification_retry
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import world.hachimi.app.api.module.MessageModule
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.model.FollowListType
import world.hachimi.app.model.FollowViewModel
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.MessageCenterStore
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.message.components.RemoteImage
import world.hachimi.app.ui.message.components.cardItems
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Spacer
import world.hachimi.app.ui.message.components.unreadDot
import world.hachimi.app.ui.notification.components.NotificationListSkeleton
import world.hachimi.app.ui.notification.components.NotificationMessageState
import world.hachimi.app.ui.util.listTailPadding
import world.hachimi.app.ui.util.listTailSpacerItem
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime
import kotlin.time.Instant

@Composable
fun NewFollowersScreen() {
    val navigator = LocalNavigator.current
    ScreenScaffold(
        title = { Text(stringResource(Res.string.message_channel_followers), maxLines = 1) },
        showBack = true,
        onBack = navigator::back,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            NewFollowersContent(Modifier.widthIn(max = 720.dp).fillMaxWidth())
        }
    }
}

/**
 * The user's followers, split into those since the last visit and earlier ones. Opening it marks
 * new followers read.
 */
@Composable
fun NewFollowersContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = AdaptiveScreenMargin, top = AdaptiveScreenMargin, end = AdaptiveScreenMargin),
    center: MessageCenterStore = koinInject(),
    vm: FollowViewModel = koinViewModel(key = FollowListType.FOLLOWERS.name) { parametersOf(FollowListType.FOLLOWERS) },
) {
    var newSince by remember { mutableStateOf<Instant?>(null) }
    LaunchedEffect(Unit) { newSince = center.openChannel(MessageModule.CHANNEL_FOLLOW) }
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }

    when (vm.initializeStatus) {
        InitializeStatus.INIT -> NotificationListSkeleton(modifier)
        InitializeStatus.FAILED -> NotificationMessageState(
            title = stringResource(Res.string.message_load_error),
            modifier = modifier.listTailPadding(),
            icon = Icons.Default.PersonAdd,
            action = {
                AccentButton(onClick = { vm.retry() }) {
                    Text(stringResource(Res.string.notification_retry))
                }
            },
        )
        InitializeStatus.LOADED -> if (vm.followerItems.isEmpty()) {
            NotificationMessageState(
                title = stringResource(Res.string.follow_empty_followers),
                subtitle = stringResource(Res.string.follow_empty_followers_subtitle),
                modifier = modifier.listTailPadding(),
                icon = Icons.Default.PersonAdd,
            )
        } else {
            val listState = rememberLazyListState()
            LaunchedEffect(listState.canScrollForward, vm.hasMore, vm.loadingMore, vm.loading) {
                if (!listState.canScrollForward && vm.hasMore && !vm.loadingMore && !vm.loading) {
                    vm.loadMore()
                }
            }
            val since = newSince
            fun isNew(item: UserModule.FollowerItem): Boolean {
                if (since == null) return false
                val followedAt = runCatching { Instant.parse(item.followedAt) }.getOrNull() ?: return false
                return followedAt > since
            }

            LazyColumn(state = listState, modifier = modifier.fillMaxSize(), contentPadding = contentPadding) {
                cardItems(items = vm.followerItems.toList(), key = { it.user.uid }) { item ->
                    FollowerRow(item, isNew = isNew(item), onClick = { vm.navigateToSpace(item.user.uid) })
                }
                item(key = "tail") {
                    if (vm.hasMore) LoadMoreItem(hasMore = true, isLoading = vm.loadingMore)
                }
                listTailSpacerItem()
            }
        }
    }
}

@Composable
private fun FollowerRow(item: UserModule.FollowerItem, isNew: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .unreadDot(isNew)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RemoteImage(item.user.avatarUrl, Modifier.size(40.dp).clip(CircleShape))
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.user.username,
                    modifier = Modifier.weight(1f, fill = false),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.isMutual) {
                    Text(
                        text = stringResource(Res.string.follow_mutual),
                        modifier = Modifier.padding(start = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = HachimiTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.weight(1f))
                runCatching { Instant.parse(item.followedAt) }.getOrNull()?.let {
                    Text(
                        text = formatTime(it, distance = true, precise = false, fullFormat = LocalDateTime.Formats.YMD),
                        modifier = Modifier.padding(start = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = HachimiTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!item.user.bio.isNullOrEmpty() && !item.user.isBanned) {
                Text(
                    text = item.user.bio,
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HachimiTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
