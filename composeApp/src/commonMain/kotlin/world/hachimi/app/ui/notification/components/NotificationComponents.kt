package world.hachimi.app.ui.notification.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.notification_empty_subtitle
import hachimiworld.composeapp.generated.resources.notification_empty_title
import hachimiworld.composeapp.generated.resources.notification_retention_hint
import hachimiworld.composeapp.generated.resources.notification_retry
import hachimiworld.composeapp.generated.resources.notification_unread_cd
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.module.NotificationModule.NotificationItem
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.message.components.unreadDot
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime

/** `1`..`99`, then `99+`. */
fun formatBadgeCount(count: Long): String = if (count > 99) "99+" else count.toString()

/** Bell with the unread count. No badge when there is nothing unread or the count is unknown. */
@Composable
fun NotificationBell(unreadCount: Long?, modifier: Modifier = Modifier) {
    Box(modifier) {
        Icon(Icons.Outlined.Notifications, contentDescription = null)
        if (unreadCount != null && unreadCount > 0) {
            UnreadBadge(
                count = unreadCount,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-8).dp),
            )
        }
    }
}

@Composable
fun UnreadBadge(count: Long, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .clip(CircleShape)
            .background(HachimiTheme.colorScheme.primary)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = formatBadgeCount(count),
            color = HachimiTheme.colorScheme.onSurfaceReverse,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun NotificationRow(
    item: NotificationItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val unread = item.readTime == null
    val unreadDescription = stringResource(Res.string.notification_unread_cd)
    val background by animateColorAsState(
        if (selected) HachimiTheme.colorScheme.primaryContainer else Color.Transparent
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(background)
            .unreadDot(unread)
            .clickable(onClick = onClick)
            .semantics { if (unread) stateDescription = unreadDescription }
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatTime(item.createTime, distance = true, precise = false, fullFormat = LocalDateTime.Formats.YMD),
                modifier = Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = HachimiTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = item.body,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = HachimiTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun RetentionHint(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.notification_retention_hint),
        modifier = modifier.fillMaxWidth().padding(vertical = 20.dp),
        textAlign = TextAlign.Center,
        fontSize = 12.sp,
        color = HachimiTheme.colorScheme.onSurfaceVariant,
    )
}

/** Inline message with a retry action, for failures that keep the content on screen. */
@Composable
fun RetryLine(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(message, fontSize = 13.sp, color = HachimiTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = onRetry) {
            Text(stringResource(Res.string.notification_retry))
        }
    }
}

@Composable
fun NotificationMessageState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector = Icons.Outlined.Notifications,
    action: (@Composable () -> Unit)? = null,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(HachimiTheme.colorScheme.onSurface.copy(0.05f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = HachimiTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            if (subtitle != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    subtitle,
                    fontSize = 13.sp,
                    color = HachimiTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (action != null) {
                Spacer(Modifier.height(16.dp))
                action()
            }
        }
    }
}

@Composable
fun NotificationEmptyState(modifier: Modifier = Modifier) {
    NotificationMessageState(
        title = stringResource(Res.string.notification_empty_title),
        subtitle = stringResource(Res.string.notification_empty_subtitle) + "\n" +
                stringResource(Res.string.notification_retention_hint),
        modifier = modifier,
    )
}

@Composable
fun NotificationListSkeleton(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        repeat(6) {
            Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.fillMaxWidth(0.5f).height(14.dp).clip(RoundedCornerShape(5.dp))
                        .background(HachimiTheme.colorScheme.onSurface.copy(0.06f))
                )
                Box(
                    Modifier.fillMaxWidth(0.8f).height(12.dp).clip(RoundedCornerShape(5.dp))
                        .background(HachimiTheme.colorScheme.onSurface.copy(0.06f))
                )
            }
        }
    }
}

/** Placeholder for the detail pane before anything is selected. */
@Composable
fun NotificationSelectHint(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = HachimiTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(text, fontSize = 14.sp, color = HachimiTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxHeight().width(1.dp).background(HachimiTheme.colorScheme.outline))
}

