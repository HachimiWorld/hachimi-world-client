package world.hachimi.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onFirstVisible
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_empty
import hachimiworld.composeapp.generated.resources.common_more
import hachimiworld.composeapp.generated.resources.common_play_cd
import hachimiworld.composeapp.generated.resources.home_recent_title
import hachimiworld.composeapp.generated.resources.home_recommend_title
import hachimiworld.composeapp.generated.resources.home_weekly_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.HomeViewModel
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.fromPublicDetail
import world.hachimi.app.model.fromSearchSongItem
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.home.components.AdaptivePullToRefreshBox
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.ui.util.horizontalFadingEdges
import world.hachimi.app.util.AdaptiveListSpacing
import world.hachimi.app.util.WindowSize
import world.hachimi.app.util.fillMaxWidthIn

@Composable
fun HomeMainScreen(
    vm: HomeViewModel = koinViewModel(),
    global: GlobalStore = koinInject()
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.unmount() }
    }
    AdaptivePullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = vm.recentStatus != InitializeStatus.INIT && vm.refreshing,
        onRefresh = { vm.fakeRefresh() },
        screenWidth = LocalWindowSize.current.width
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item(contentType = "recent_section") {
                Segment(
                    modifier = Modifier.fillMaxWidthIn(),
                    label = stringResource(Res.string.home_recent_title),
                    status = vm.recentStatus,
                    loading = vm.recentLoading,
                    items = vm.recentSongs,
                    onMoreClick = { global.nav.push(Route.Root.Home.Recent) },
                    onLoad = vm::mountRecent,
                    onRefresh = {},
                    onRetryClick = vm::retryRecent
                )
            }

            item(contentType = "recommend_section") {
                Segment(
                    modifier = Modifier.fillMaxWidthIn(),
                    label = stringResource(Res.string.home_recommend_title),
                    status = vm.recommendStatus,
                    loading = vm.recommendLoading,
                    items = vm.recommendSongs,
                    onMoreClick = { global.nav.push(Route.Root.Home.Recommend) },
                    onLoad = vm::mountRecommend,
                    onRefresh = {},
                    onRetryClick = vm::retryRecommend
                )
            }

            item(contentType = "hot_section") {
                Segment(
                    modifier = Modifier.fillMaxWidthIn(),
                    label = stringResource(Res.string.home_weekly_title),
                    status = vm.hotStatus,
                    loading = vm.hotLoading,
                    items = vm.hotSongs,
                    onMoreClick = { global.nav.push(Route.Root.Home.WeeklyHot) },
                    onLoad = vm::mountHot,
                    onRefresh = {},
                    onRetryClick = vm::retryHot
                )
            }

            items(vm.recommendTags, key = { it }, contentType = { "tag_section" }) { tag ->
                CategorySegment(
                    modifier = Modifier.fillMaxWidthIn(),
                    category = tag
                )
            }

            item {
                Spacer(Modifier.navigationBarsPadding().padding(LocalContentInsets.current.asPaddingValues()))
            }
        }
    }
}

@Composable
private fun Segment(
    label: String,
    status: InitializeStatus,
    loading: Boolean,
    items: List<SongModule.PublicSongDetail>,
    onMoreClick: () -> Unit,
    onLoad: () -> Unit,
    onRefresh: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    global: GlobalStore = koinInject()
) {
    Column(modifier) {
        SegmentHeader(text = label, onMoreClick = onMoreClick, onLoad = onLoad)
        Spacer(Modifier.height(AdaptiveListSpacing))
        BoxWithConstraints {
            val cardSize: DpSize

            if (maxWidth < WindowSize.COMPACT) {
                val widthTwoColumn = (maxWidth - 48.dp - AdaptiveListSpacing * 2) / 2
                val width = minOf(widthTwoColumn, 180.dp)
                cardSize = DpSize(width, width + 48.dp)
            } else {
                cardSize = DpSize(180.dp, 228.dp)
            }
            LoadableContent(
                modifier = Modifier.fillMaxWidth().height(cardSize.height * 2 + AdaptiveListSpacing),
                initializeStatus = status,
                loading = loading,
                onRefresh = onRefresh,
                onRetryClick = onRetryClick,
                onLoad = {}
            ) {
                if (items.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.common_empty))
                } else LazyHorizontalGrid(
                    modifier = Modifier.fillMaxSize()
                        .horizontalFadingEdges(startEdgeWidth = 24.dp, endEdgeWidth = 24.dp),
                    rows = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
                    verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
                ) {
                    items(items = items, key = { it.id }) { item ->
                        SongCard(
                            modifier = Modifier.size(cardSize),
                            item = item,
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
            }
        }
    }
}

@Composable
private fun CategorySegment(
    category: String,
    modifier: Modifier = Modifier,
    global: GlobalStore = koinInject(),
    vm: HomeViewModel = koinViewModel()
) {
    Column(modifier) {
        SegmentHeader(category, onLoad = {
            vm.mountCategory(category)
        }, onMoreClick = {
            global.nav.push(Route.Root.Home.Category(category))
        })
        Spacer(Modifier.height(AdaptiveListSpacing))
        BoxWithConstraints {
            val cardSize: DpSize

            if (maxWidth < WindowSize.COMPACT) {
                val widthTwoColumn = (maxWidth - 48.dp - AdaptiveListSpacing * 2) / 2
                val width = minOf(widthTwoColumn, 180.dp)
                cardSize = DpSize(width, width + 48.dp)
            } else {
                cardSize = DpSize(180.dp, 228.dp)
            }
            val state = vm.categoryState[category]
            val status = state?.status?.value ?: InitializeStatus.INIT
            val loading = state?.loading?.value ?: false
            LoadableContent(
                modifier = Modifier.fillMaxWidth().height(cardSize.height * 2 + AdaptiveListSpacing),
                initializeStatus = status,
                loading = loading,
                onRefresh = { },
                onRetryClick = { vm.retryCategory(category) },
                onLoad = { }
            ) {
                val songs = state?.songs?.value ?: emptyList()
                if (songs.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(Res.string.common_empty))
                    }
                } else LazyHorizontalGrid(
                    modifier = Modifier.fillMaxSize()
                        .horizontalFadingEdges(startEdgeWidth = 24.dp, endEdgeWidth = 24.dp),
                    rows = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
                    verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
                ) {
                    items(songs, key = { item -> item.id }) { item ->
                        SongCard(
                            modifier = Modifier.size(cardSize),
                            coverUrl = item.coverArtUrl,
                            title = item.title,
                            subtitle = item.subtitle,
                            author = item.uploaderName,
                            tags = remember { emptyList<String>() },
                            playCount = item.playCount,
                            likeCount = item.likeCount,
                            explicit = item.explicit,
                            onClick = {
                                global.player.insertToQueue(
                                    GlobalStore.MusicQueueItem.fromSearchSongItem(item),
                                    true,
                                    false
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentHeader(
    text: String,
    onMoreClick: () -> Unit,
    onLoad: () -> Unit = {}
) {
    Row(
        modifier = Modifier.padding(start = 24.dp, end = 24.dp).onFirstVisible { onLoad() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MaterialTheme.typography.titleLarge)
        /*if (maxWidth >= WindowSize.COMPACT) {
            IconButton(
                modifier = Modifier.padding(start = 8.dp),
                enabled = !vm.isLoading,
                onClick = { vm.fakeRefresh() }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }*/

        Spacer(Modifier.weight(1f))

        Button(
            modifier = Modifier,
            onClick = onMoreClick
        ) {
            Text(stringResource(Res.string.common_more))
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(Res.string.common_play_cd)
            )
        }
    }
}

@Composable
private fun LoadableContent(
    loading: Boolean,
    initializeStatus: InitializeStatus,
    onRefresh: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLoad: () -> Unit,
    onDispose: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val slideOffset = with(LocalDensity.current) {
        32.dp.roundToPx()
    }
    AnimatedContent(
        initializeStatus,
        modifier = modifier.onFirstVisible { onLoad() },
        transitionSpec = {
            if (targetState == InitializeStatus.LOADED) {
                (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                        slideInVertically(
                            initialOffsetY = { slideOffset },
                            animationSpec = tween(220, delayMillis = 90)
                        ))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            } else {
                (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            }
        }) {
        when (it) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onRetryClick)
            InitializeStatus.LOADED -> content()
        }
    }
}