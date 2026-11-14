package world.hachimi.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_empty
import hachimiworld.composeapp.generated.resources.common_refresh_cd
import hachimiworld.composeapp.generated.resources.home_recent_title
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.RecentPublishViewModel
import world.hachimi.app.model.fromPublicDetail
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.component.PageHeading
import world.hachimi.app.ui.component.PageTitleAction
import world.hachimi.app.ui.component.PlayAllCovers
import world.hachimi.app.ui.component.headingListPadding
import world.hachimi.app.ui.component.titleActionPlaced
import world.hachimi.app.ui.util.loadStateTransition
import world.hachimi.app.ui.home.components.SongCardGridSkeleton
import world.hachimi.app.ui.component.ErrorPage
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.home.components.AdaptivePullToRefreshBox
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.ui.util.AdaptiveListSpacing
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.ListTailSpacer
import world.hachimi.app.ui.util.calculateGridColumns
import world.hachimi.app.ui.util.contentPaddingForMaxWidth
import world.hachimi.app.ui.util.listHeadInsetsSpacerItem
import world.hachimi.app.util.formatDaysDistance
import kotlin.time.Clock

@Composable
fun RecentPublishScreen(
    vm: RecentPublishViewModel = koinViewModel(),
    global: GlobalStore = koinInject()
) {
    val navigator = LocalNavigator.current

    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.unmount() }
    }
    ScreenScaffold(
        title = { Text(stringResource(Res.string.home_recent_title), maxLines = 1) },
        showBack = true,
        onBack = navigator::back,
        headingInPageOnWide = true,
    ) {
        AnimatedContent(
            vm.initializeStatus,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = { loadStateTransition() },
        ) {
            when (it) {
                InitializeStatus.INIT -> SongCardGridSkeleton(playAll = !titleActionPlaced, dateLabel = true)
                InitializeStatus.FAILED -> ErrorPage(onRetry = { vm.retry() })
                InitializeStatus.LOADED -> Content(vm, global)
            }
        }
    }
}


@Composable
private fun Content(vm: RecentPublishViewModel, global: GlobalStore) {
    // The fan of covers plays them all: by the title on phones, in the header row on wide screens
    val covers = remember(vm.songs) { vm.songs.asSequence().flatMap { it.songs }.take(3).map { it.coverUrl }.toList() }
    val playAll: () -> Unit = { vm.playAllRecent() }
    val headingInScaffold = titleActionPlaced
    val playAllInTitle = vm.songs.isNotEmpty() && PageTitleAction { folded ->
        PlayAllCovers(covers = covers, folded = folded, onClick = playAll)
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val constraintMaxWidth = maxWidth
        AdaptivePullToRefreshBox(
            isRefreshing = vm.initializeStatus != InitializeStatus.INIT && vm.refreshing,
            onRefresh = { vm.fakeRefresh() },
            screenWidth = constraintMaxWidth
        ) {
            val state = rememberLazyGridState()
            LaunchedEffect(state.canScrollForward) {
                if (!vm.loading && !state.canScrollForward) {
                    vm.loadMore()
                }
            }

            if (vm.songs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.common_empty))
            } else LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                state = state,
                columns = calculateGridColumns(constraintMaxWidth),
                contentPadding = contentPaddingForMaxWidth(headingListPadding(headingInPage = !headingInScaffold), constraintMaxWidth),
                horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
                verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            ) {
                listHeadInsetsSpacerItem()
                // On phones the scaffold shows the title large, and the fan by it
                if (!headingInScaffold) item(span = { GridItemSpan(maxLineSpan) }) {
                    PageHeading(
                        title = stringResource(Res.string.home_recent_title),
                        action = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                HachimiIconButton(enabled = !vm.loading, onClick = { vm.fakeRefresh() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.common_refresh_cd))
                                }
                                PlayAllCovers(covers = covers, folded = false, onClick = playAll)
                            }
                        },
                    )
                }
                vm.songs.fastForEach {
                    item(
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = "separator"
                    ) {
                        val daysOffset = remember(it.date) {
                            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                            (today.toEpochDays() - it.date.toEpochDays()).toInt()
                        }
                        Text(
                            text = formatDaysDistance(daysOffset) ?: it.date.toString(),
                            style = MaterialTheme.typography.titleMedium)
                    }
                    items(items = it.songs, key = { it.id }) { item ->
                        SongCard(
                            modifier = Modifier.fillMaxWidth(),
                            coverUrl = item.coverUrl,
                            title = item.title,
                            subtitle = item.subtitle,
                            author = item.uploaderName,
                            tags = item.tags.map { it.name },
                            likeCount = item.likeCount,
                            playCount = item.playCount,
                            explicit = item.explicit,
                            onClick = {
                                global.player.insertToQueue(
                                    GlobalStore.MusicQueueItem.fromPublicDetail(item),
                                    true,
                                    false
                                )
                            },
                        )
                    }
                }
                item(
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "load_more"
                ) {
                    LoadMoreItem(hasMore = vm.hasMore, isLoading = vm.loading)
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ListTailSpacer()
                }
            }
        }
    }
}