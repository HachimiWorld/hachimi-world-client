package world.hachimi.app.ui.follow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.follow_cancel
import hachimiworld.composeapp.generated.resources.follow_empty_followers
import hachimiworld.composeapp.generated.resources.follow_empty_followers_subtitle
import hachimiworld.composeapp.generated.resources.follow_empty_following
import hachimiworld.composeapp.generated.resources.follow_empty_following_subtitle
import hachimiworld.composeapp.generated.resources.follow_followers_list_title
import hachimiworld.composeapp.generated.resources.follow_following_list_title
import hachimiworld.composeapp.generated.resources.follow_load_error
import hachimiworld.composeapp.generated.resources.follow_unfollow_confirm
import hachimiworld.composeapp.generated.resources.follow_unfollow_confirm_subtitle
import hachimiworld.composeapp.generated.resources.follow_unfollow_confirm_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import world.hachimi.app.model.FollowListType
import world.hachimi.app.model.FollowViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.follow.components.EmptyState
import world.hachimi.app.ui.follow.components.ErrorState
import world.hachimi.app.ui.follow.components.FollowerItemCard
import world.hachimi.app.ui.follow.components.FollowingItemCard
import world.hachimi.app.ui.follow.components.LoadingSkeleton
import world.hachimi.app.ui.follow.components.UnfollowDialog
import world.hachimi.app.ui.util.InitStatusScaffold
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.ui.util.listTailSpacerItem

@Composable
fun FollowListScreen(
    type: FollowListType,
    vm: FollowViewModel = koinViewModel(key = type.name) { parametersOf(type) },
) {
    val navigator = LocalNavigator.current

    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }

    val title = when (type) {
        FollowListType.FOLLOWING -> stringResource(Res.string.follow_following_list_title)
        FollowListType.FOLLOWERS -> stringResource(Res.string.follow_followers_list_title)
    }

    ScreenScaffold(
        title = { Text(title, maxLines = 1) },
        showBack = type == FollowListType.FOLLOWERS,
        onBack = navigator::back,
    ) {
        InitStatusScaffold(
            initializeStatus = vm.initializeStatus,
            isLoading = vm.loading,
            onRetryClick = { vm.retry() },
            initPage = { LoadingSkeleton() },
            errorPage = {
                ErrorState(
                    message = vm.errorData ?: stringResource(Res.string.follow_load_error),
                    onRetry = { vm.retry() },
                )
            },
        ) {
            val itemsEmpty = when (type) {
                FollowListType.FOLLOWING -> vm.followingItems.isEmpty()
                FollowListType.FOLLOWERS -> vm.followerItems.isEmpty()
            }

            if (itemsEmpty) {
                when (type) {
                    FollowListType.FOLLOWING -> EmptyState(
                        title = stringResource(Res.string.follow_empty_following),
                        subtitle = stringResource(Res.string.follow_empty_following_subtitle),
                        showDiscoverButton = true,
                        onDiscoverClick = { navigator.push(Route.Root.Home.Recommend) },
                    )
                    FollowListType.FOLLOWERS -> EmptyState(
                        title = stringResource(Res.string.follow_empty_followers),
                        subtitle = stringResource(Res.string.follow_empty_followers_subtitle),
                        showDiscoverButton = false,
                    )
                }
            } else {
                val listState = rememberLazyListState()
                val isCompact = LocalWindowSize.current.width < WindowSize.COMPACT

                LaunchedEffect(listState.canScrollForward, vm.hasMore, vm.loadingMore, vm.loading) {
                    if (!listState.canScrollForward && vm.hasMore && !vm.loadingMore && !vm.loading) {
                        vm.loadMore()
                    }
                }

                when (type) {
                    FollowListType.FOLLOWING -> FollowingList(vm, listState, isCompact)
                    FollowListType.FOLLOWERS -> FollowersList(vm, listState, isCompact)
                }
            }
        }
    }

    vm.unfollowDialogTarget?.let { target ->
        UnfollowDialog(
            username = target.username,
            subtitle = stringResource(Res.string.follow_unfollow_confirm_subtitle),
            confirmText = stringResource(Res.string.follow_unfollow_confirm),
            cancelText = stringResource(Res.string.follow_cancel),
            confirmTitle = stringResource(Res.string.follow_unfollow_confirm_title, target.username),
            loading = vm.actionLoading,
            onConfirm = { vm.confirmUnfollow() },
            onDismiss = { vm.dismissUnfollowDialog() },
        )
    }
}

@Composable
private fun FollowingList(
    vm: FollowViewModel,
    listState: LazyListState,
    isCompact: Boolean,
) {
    val items = vm.followingItems.toList()

    if (isCompact) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items, key = { "f_${it.user.uid}" }) { item ->
                FollowingItemCard(item, vm, isCompact)
            }
            item(key = "load_more") {
                LoadMoreItem(hasMore = vm.hasMore, isLoading = vm.loadingMore)
            }
            listTailSpacerItem()
        }
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            val chunked = items.chunked(2)
            items(chunked, key = { chunk -> "row_${chunk.firstOrNull()?.user?.uid}" }) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    row.forEach { item ->
                        Box(Modifier.weight(1f)) {
                            FollowingItemCard(item, vm, isCompact)
                        }
                    }
                    if (row.size < 2) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
            item(key = "load_more") {
                LoadMoreItem(hasMore = vm.hasMore, isLoading = vm.loadingMore)
            }
            listTailSpacerItem()
        }
    }
}

@Composable
private fun FollowersList(
    vm: FollowViewModel,
    listState: LazyListState,
    isCompact: Boolean,
) {
    val items = vm.followerItems.toList()

    if (isCompact) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items, key = { "fr_${it.user.uid}" }) { item ->
                FollowerItemCard(item, vm, isCompact)
            }
            item(key = "load_more") {
                LoadMoreItem(hasMore = vm.hasMore, isLoading = vm.loadingMore)
            }
            listTailSpacerItem()
        }
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            val chunked = items.chunked(2)
            items(chunked, key = { chunk -> "row_${chunk.firstOrNull()?.user?.uid}" }) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    row.forEach { item ->
                        Box(Modifier.weight(1f)) {
                            FollowerItemCard(item, vm, isCompact)
                        }
                    }
                    if (row.size < 2) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
            item(key = "load_more") {
                LoadMoreItem(hasMore = vm.hasMore, isLoading = vm.loadingMore)
            }
            listTailSpacerItem()
        }
    }
}
