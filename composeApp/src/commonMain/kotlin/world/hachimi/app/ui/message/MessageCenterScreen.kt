package world.hachimi.app.ui.message

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.message_channel_followers
import hachimiworld.composeapp.generated.resources.message_channel_likes
import hachimiworld.composeapp.generated.resources.message_channel_system
import hachimiworld.composeapp.generated.resources.message_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import world.hachimi.app.model.MessageCenterStore
import world.hachimi.app.model.NotificationStore
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.notification.MarkAllReadAction
import world.hachimi.app.ui.notification.NotificationInboxContent
import world.hachimi.app.ui.notification.components.UnreadBadge
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.WindowSize

private enum class MessageChannel(val label: StringResource, val icon: ImageVector, val route: Route) {
    System(Res.string.message_channel_system, Icons.Outlined.Notifications, Route.Root.Notifications),
    Likes(Res.string.message_channel_likes, Icons.Default.FavoriteBorder, Route.Root.ReceivedLikes),
    Followers(Res.string.message_channel_followers, Icons.Default.PersonAdd, Route.Root.NewFollowers),
}

/**
 * Lists the kinds of messages with their unread counts, like the settings page. Narrow windows
 * open each kind as its own page; wide ones show the selected kind beside the list.
 */
@Composable
fun MessageCenterScreen(
    center: MessageCenterStore = koinInject(),
    notifications: NotificationStore = koinInject(),
) {
    val navigator = LocalNavigator.current
    val width = LocalWindowSize.current.width
    val twoPane = width >= WindowSize.EXPANDED
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val selected = MessageChannel.entries[selectedIndex]

    LaunchedEffect(Unit) { center.refreshSummary() }

    fun unreadOf(channel: MessageChannel): Long? = when (channel) {
        MessageChannel.System -> notifications.unreadCount
        MessageChannel.Likes -> center.likeUnread
        MessageChannel.Followers -> center.followUnread
    }

    ScreenScaffold(
        title = { Text(stringResource(Res.string.message_title), maxLines = 1) },
        showBack = true,
        onBack = navigator::back,
        actions = {
            if (twoPane && selected == MessageChannel.System) MarkAllReadAction(notifications)
        },
    ) {
        val margin = AdaptiveScreenMargin
        if (twoPane) {
            Row(Modifier.fillMaxSize().padding(horizontal = margin)) {
                ChannelCard(
                    selected = selected,
                    unreadOf = ::unreadOf,
                    onSelect = { selectedIndex = it.ordinal },
                    modifier = Modifier.padding(top = margin).width(260.dp),
                )
                Spacer(Modifier.width(margin))
                Box(Modifier.weight(1f).fillMaxSize()) {
                    when (selected) {
                        MessageChannel.System -> NotificationInboxContent(
                            detailPane = width >= WindowSize.LARGE,
                            contentPadding = PaddingValues(top = margin),
                        )
                        MessageChannel.Likes -> ReceivedLikesContent(contentPadding = PaddingValues(top = margin))
                        MessageChannel.Followers -> NewFollowersContent(contentPadding = PaddingValues(top = margin))
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize().padding(horizontal = margin), contentAlignment = Alignment.TopCenter) {
                ChannelCard(
                    selected = null,
                    unreadOf = ::unreadOf,
                    onSelect = { navigator.push(it.route) },
                    modifier = Modifier.padding(top = margin).widthIn(max = 720.dp).fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ChannelCard(
    selected: MessageChannel?,
    unreadOf: (MessageChannel) -> Long?,
    onSelect: (MessageChannel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column {
            MessageChannel.entries.forEach { channel ->
                val isSelected = channel == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp)
                        .background(if (isSelected) HachimiTheme.colorScheme.primaryContainer else Color.Transparent)
                        .clickable { onSelect(channel) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        channel.icon,
                        contentDescription = null,
                        tint = if (isSelected) HachimiTheme.colorScheme.primary else HachimiTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(channel.label),
                        modifier = Modifier.weight(1f).padding(start = 14.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) HachimiTheme.colorScheme.primary else Color.Unspecified,
                    )
                    val unread = unreadOf(channel)
                    if (unread != null && unread > 0) UnreadBadge(unread)
                    if (selected == null) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = HachimiTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp).size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
