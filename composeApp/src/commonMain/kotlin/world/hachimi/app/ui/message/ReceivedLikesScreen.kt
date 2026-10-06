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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.message_channel_likes
import hachimiworld.composeapp.generated.resources.message_likes_empty_subtitle
import hachimiworld.composeapp.generated.resources.message_likes_empty_title
import hachimiworld.composeapp.generated.resources.message_likes_suffix
import hachimiworld.composeapp.generated.resources.message_likes_suffix_with_count
import hachimiworld.composeapp.generated.resources.message_load_error
import hachimiworld.composeapp.generated.resources.message_load_more_failed
import hachimiworld.composeapp.generated.resources.message_name_separator
import hachimiworld.composeapp.generated.resources.notification_retry
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.MessageModule.ReceivedLikeItem
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.ReceivedLikesViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.message.components.RemoteImage
import world.hachimi.app.ui.message.components.cardItems
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import androidx.compose.material3.MaterialTheme
import world.hachimi.app.ui.message.components.unreadDot
import world.hachimi.app.ui.notification.components.NotificationListSkeleton
import world.hachimi.app.ui.notification.components.NotificationMessageState
import world.hachimi.app.ui.notification.components.RetryLine
import world.hachimi.app.ui.util.listTailPadding
import world.hachimi.app.ui.util.listTailSpacerItem
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime

@Composable
fun ReceivedLikesScreen() {
    val navigator = LocalNavigator.current
    ScreenScaffold(
        title = { Text(stringResource(Res.string.message_channel_likes), maxLines = 1) },
        showBack = true,
        onBack = navigator::back,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            ReceivedLikesContent(Modifier.widthIn(max = 720.dp).fillMaxWidth())
        }
    }
}

/**
 * The list without the toolbar, for embedding, split into likes since the last visit and earlier
 * ones. Opening it marks received likes read.
 */
@Composable
fun ReceivedLikesContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = AdaptiveScreenMargin, top = AdaptiveScreenMargin, end = AdaptiveScreenMargin),
    vm: ReceivedLikesViewModel = koinViewModel(),
) {
    val navigator = LocalNavigator.current
    LaunchedEffect(vm) { vm.mounted() }

    when (vm.initializeStatus) {
        InitializeStatus.INIT -> NotificationListSkeleton(modifier)
        InitializeStatus.FAILED -> NotificationMessageState(
            title = stringResource(Res.string.message_load_error),
            modifier = modifier.listTailPadding(),
            icon = Icons.Default.FavoriteBorder,
            action = {
                AccentButton(onClick = { vm.retry() }) {
                    Text(stringResource(Res.string.notification_retry))
                }
            },
        )
        InitializeStatus.LOADED -> if (vm.items.isEmpty()) {
            NotificationMessageState(
                title = stringResource(Res.string.message_likes_empty_title),
                subtitle = stringResource(Res.string.message_likes_empty_subtitle),
                modifier = modifier.listTailPadding(),
                icon = Icons.Default.FavoriteBorder,
            )
        } else {
            val listState = rememberLazyListState()
            LaunchedEffect(listState.canScrollForward, vm.hasMore, vm.loadingMore, vm.loadMoreFailed) {
                if (!listState.canScrollForward && vm.hasMore && !vm.loadingMore && !vm.loadMoreFailed) {
                    vm.loadMore()
                }
            }
            LazyColumn(state = listState, modifier = modifier.fillMaxSize(), contentPadding = contentPadding) {
                cardItems(items = vm.items.toList(), key = { it.songId }) { item ->
                    ReceivedLikeRow(
                        item = item,
                        isNew = vm.isNew(item),
                        onClick = { navigator.push(Route.Root.CreationCenter.ArtworkDetail(item.songId)) },
                    )
                }
                item(key = "tail") {
                    when {
                        vm.loadMoreFailed -> RetryLine(
                            stringResource(Res.string.message_load_more_failed),
                            onRetry = { vm.loadMore() },
                        )
                        vm.hasMore -> LoadMoreItem(hasMore = true, isLoading = vm.loadingMore)
                    }
                }
                listTailSpacerItem()
            }
        }
    }
}

@Composable
private fun ReceivedLikeRow(item: ReceivedLikeItem, isNew: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .unreadDot(isNew)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RemoteImage(item.coverUrl, Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)))
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = likesText(item),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatTime(item.latestLikeTime, distance = true, precise = false, fullFormat = LocalDateTime.Formats.YMD),
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = HachimiTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "《${item.songTitle}》",
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = HachimiTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** "A、B 等 12 人赞了你的作品", with the names emphasized. */
@Composable
fun likesText(item: ReceivedLikeItem): AnnotatedString {
    val shown = item.latestLikers.take(2)
    val names = shown.joinToString(stringResource(Res.string.message_name_separator)) { it.username }
    val suffix = if (item.likeCount <= shown.size) {
        stringResource(Res.string.message_likes_suffix)
    } else {
        stringResource(Res.string.message_likes_suffix_with_count, item.likeCount.toString(), (item.likeCount - shown.size).toString())
    }
    val variant = HachimiTheme.colorScheme.onSurfaceVariant
    return buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(names) }
        append(" ")
        withStyle(SpanStyle(color = variant)) { append(suffix) }
    }
}
