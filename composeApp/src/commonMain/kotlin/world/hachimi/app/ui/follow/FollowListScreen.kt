package world.hachimi.app.ui.follow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import world.hachimi.app.model.FollowListType
import world.hachimi.app.model.FollowViewModel
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.follow.components.BottomLoader
import world.hachimi.app.ui.follow.components.EmptyState
import world.hachimi.app.ui.follow.components.ErrorState
import world.hachimi.app.ui.follow.components.FollowerItemCard
import world.hachimi.app.ui.follow.components.FollowingItemCard
import world.hachimi.app.ui.follow.components.LoadingSkeleton
import world.hachimi.app.ui.follow.components.UnfollowDialog
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.ui.util.listTailSpacerItem

@Composable
fun FollowListScreen(
    type: FollowListType,
    vm: FollowViewModel = koinViewModel()
) {
    val navigator = LocalNavigator.current

    DisposableEffect(type, vm) {
        vm.mounted(type)
        onDispose { vm.dispose() }
    }

    Column(Modifier.fillMaxSize()) {
        // Top bar
        Text(
            modifier = Modifier.padding(AdaptiveScreenMargin),
            text = when (type) {
                FollowListType.FOLLOWING -> stringResource(Res.string.follow_following_list_title)
                FollowListType.FOLLOWERS -> stringResource(Res.string.follow_followers_list_title)
            },
            style = MaterialTheme.typography.titleLarge
        )

        // Content
        val itemsEmpty = when (type) {
            FollowListType.FOLLOWING -> vm.followingItems.isEmpty()
            FollowListType.FOLLOWERS -> vm.followerItems.isEmpty()
        }

        when {
            vm.initializeStatus == InitializeStatus.INIT && vm.loading -> LoadingSkeleton()
            vm.initializeStatus == InitializeStatus.FAILED && itemsEmpty -> {
                ErrorState(
                    message = vm.error ?: stringResource(Res.string.follow_load_error),
                    onRetry = { vm.loadFirstPage() }
                )
            }
            itemsEmpty && vm.initializeStatus != InitializeStatus.INIT -> {
                when (type) {
                    FollowListType.FOLLOWING -> EmptyState(
                        title = stringResource(Res.string.follow_empty_following),
                        subtitle = stringResource(Res.string.follow_empty_following_subtitle),
                        showDiscoverButton = true,
                        onDiscoverClick = { navigator.push(world.hachimi.app.nav.Route.Root.Home.Recommend) }
                    )
                    FollowListType.FOLLOWERS -> EmptyState(
                        title = stringResource(Res.string.follow_empty_followers),
                        subtitle = stringResource(Res.string.follow_empty_followers_subtitle),
                        showDiscoverButton = false,
                    )
                }
            }
            else -> {
                val listState = rememberLazyListState()
                val isCompact = LocalWindowSize.current.width < WindowSize.COMPACT

                if (type == FollowListType.FOLLOWING) {
                    FollowingList(vm, listState, isCompact)
                } else {
                    FollowersList(vm, listState, isCompact)
                }
            }
        }
    }

    // Unfollow dialog
    vm.unfollowDialogTarget?.let { target ->
        UnfollowDialog(
            username = target.username,
            subtitle = stringResource(Res.string.follow_unfollow_confirm_subtitle),
            confirmText = stringResource(Res.string.follow_unfollow_confirm),
            cancelText = stringResource(Res.string.follow_cancel),
            confirmTitle = stringResource(Res.string.follow_unfollow_confirm_title, target.username),
            loading = vm.actionLoading,
            onConfirm = { vm.confirmUnfollow() },
            onDismiss = { vm.dismissUnfollowDialog() }
        )
    }
}

@Composable
private fun FollowingList(
    vm: FollowViewModel,
    listState: LazyListState,
    isCompact: Boolean
) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 3 && vm.hasMore && !vm.loadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) vm.loadNextPage()
    }

    val items = vm.followingItems.toList()

    if (isCompact) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items, key = { "f_${it.user.uid}" }) { item ->
                FollowingItemCard(item, vm, isCompact)
            }
            if (vm.loadingMore) {
                item(key = "loading_more") { BottomLoader() }
            }
            item(key = "bottom_padding") {
                Spacer(Modifier.height(96.dp).navigationBarsPadding().padding(LocalContentInsets.current.asPaddingValues()))
            }
        }
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            val chunked = items.chunked(2)
            items(chunked, key = { chunk -> "row_${chunk.firstOrNull()?.user?.uid}" }) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
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
            if (vm.loadingMore) {
                item(key = "loading_more") { BottomLoader() }
            }
            item(key = "bottom_padding") {
                Spacer(Modifier.height(96.dp).navigationBarsPadding().padding(LocalContentInsets.current.asPaddingValues()))
            }
        }
    }
}

@Composable
private fun FollowersList(
    vm: FollowViewModel,
    listState: LazyListState,
    isCompact: Boolean
) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 3 && vm.hasMore && !vm.loadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) vm.loadNextPage()
    }

    val items = vm.followerItems.toList()

    if (isCompact) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items, key = { "fr_${it.user.uid}" }) { item ->
                FollowerItemCard(item, vm, isCompact)
            }
            if (vm.loadingMore) {
                item(key = "loading_more") { BottomLoader() }
            }
            listTailSpacerItem()
        }
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            val chunked = items.chunked(2)
            items(chunked, key = { chunk -> "row_${chunk.firstOrNull()?.user?.uid}" }) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
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
            if (vm.loadingMore) {
                item(key = "loading_more") { BottomLoader() }
            }
            listTailSpacerItem()
        }
    }
}

