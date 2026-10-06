package world.hachimi.app.ui.notification

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.notification_load_error
import hachimiworld.composeapp.generated.resources.notification_load_more_failed
import hachimiworld.composeapp.generated.resources.notification_mark_all_read
import hachimiworld.composeapp.generated.resources.notification_refresh_failed
import hachimiworld.composeapp.generated.resources.notification_retry
import hachimiworld.composeapp.generated.resources.notification_select_hint
import hachimiworld.composeapp.generated.resources.notification_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.NotificationStore
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.notification.components.NotificationDivider
import world.hachimi.app.ui.notification.components.NotificationEmptyState
import world.hachimi.app.ui.notification.components.NotificationListSkeleton
import world.hachimi.app.ui.notification.components.NotificationMessageState
import world.hachimi.app.ui.notification.components.NotificationRow
import world.hachimi.app.ui.notification.components.NotificationSelectHint
import world.hachimi.app.ui.notification.components.RetentionHint
import world.hachimi.app.ui.notification.components.RetryLine
import world.hachimi.app.ui.notification.components.VerticalDivider
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.ui.util.listTailPadding
import world.hachimi.app.ui.util.listTailSpacerItem

@Composable
fun NotificationInboxScreen(store: NotificationStore = koinInject()) {
    val navigator = LocalNavigator.current
    val global = koinInject<GlobalStore>()
    val scope = rememberCoroutineScope()
    val twoPane = LocalWindowSize.current.width >= WindowSize.EXPANDED
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { store.refresh() }

    ScreenScaffold(
        title = { Text(stringResource(Res.string.notification_title), maxLines = 1) },
        showBack = true,
        onBack = navigator::back,
        actions = {
            val unread = store.unreadCount ?: 0
            if (unread > 0 || store.markingAllRead) {
                TextButton(onClick = { store.markAllRead() }, enabled = !store.markingAllRead) {
                    Text(stringResource(Res.string.notification_mark_all_read))
                }
            }
        },
    ) {
        val list: @Composable (Modifier) -> Unit = { modifier ->
            NotificationList(
                store = store,
                selectedId = if (twoPane) selectedId else null,
                onOpen = { id ->
                    if (twoPane) selectedId = id else navigator.push(Route.Root.NotificationDetail(id))
                },
                onMarkRead = { id ->
                    scope.launch {
                        store.markRead(id).onFailure { global.alert(it.message) }
                    }
                },
                modifier = modifier,
            )
        }

        if (twoPane) {
            Row(Modifier.fillMaxSize()) {
                list(Modifier.width(360.dp))
                VerticalDivider()
                Box(Modifier.weight(1f).fillMaxSize()) {
                    val id = selectedId
                    if (id == null) {
                        NotificationSelectHint(
                            stringResource(Res.string.notification_select_hint),
                            Modifier.listTailPadding(),
                        )
                    } else {
                        NotificationDetailContent(notificationId = id, onBackToInbox = { selectedId = null })
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                list(Modifier.widthIn(max = 720.dp).fillMaxWidth())
            }
        }
    }
}

@Composable
private fun NotificationList(
    store: NotificationStore,
    selectedId: String?,
    onOpen: (String) -> Unit,
    onMarkRead: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (store.initializeStatus) {
        InitializeStatus.INIT -> NotificationListSkeleton(modifier)
        InitializeStatus.FAILED -> NotificationMessageState(
            title = stringResource(Res.string.notification_load_error),
            modifier = modifier.listTailPadding(),
            action = {
                AccentButton(onClick = { store.refresh() }, enabled = !store.refreshing) {
                    Text(stringResource(Res.string.notification_retry))
                }
            },
        )
        InitializeStatus.LOADED -> if (store.items.isEmpty()) {
            NotificationEmptyState(modifier.listTailPadding())
        } else {
            val listState = rememberLazyListState()
            LaunchedEffect(listState.canScrollForward, store.hasMore, store.loadingMore, store.loadMoreFailed) {
                if (!listState.canScrollForward && store.hasMore && !store.loadingMore && !store.loadMoreFailed) {
                    store.loadMore()
                }
            }
            LazyColumn(state = listState, modifier = modifier.fillMaxSize()) {
                if (store.refreshFailed) {
                    item(key = "refresh_failed") {
                        RetryLine(stringResource(Res.string.notification_refresh_failed), onRetry = { store.refresh() })
                    }
                }
                itemsIndexed(store.items, key = { _, item -> item.notificationId }) { index, item ->
                    if (index > 0) NotificationDivider()
                    NotificationRow(
                        item = item,
                        selected = item.notificationId == selectedId,
                        onClick = { onOpen(item.notificationId) },
                        onMarkRead = { onMarkRead(item.notificationId) },
                    )
                }
                item(key = "tail") {
                    when {
                        store.loadMoreFailed -> RetryLine(
                            stringResource(Res.string.notification_load_more_failed),
                            onRetry = { store.loadMore() },
                        )
                        store.hasMore -> LoadMoreItem(hasMore = true, isLoading = store.loadingMore)
                        else -> RetentionHint()
                    }
                }
                listTailSpacerItem()
            }
        }
    }
}
