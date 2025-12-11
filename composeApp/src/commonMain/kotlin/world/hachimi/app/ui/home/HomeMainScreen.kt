package world.hachimi.app.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onFirstVisible
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.model.*
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.home.components.AdaptivePullToRefreshBox
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.util.AdaptiveListSpacing
import world.hachimi.app.util.WindowSize

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
        modifier = Modifier.fillMaxSize().wrapContentWidth().widthIn(max = WindowSize.EXPANDED),
        isRefreshing = vm.recentStatus != InitializeStatus.INIT && vm.refreshing,
        onRefresh = { vm.fakeRefresh() },
        screenWidth = LocalWindowSize.current.width
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .windowInsetsPadding(LocalContentInsets.current)
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing)
        ) {
            Segment(
                label = "最近发布",
                status = vm.recentStatus,
                loading = vm.recentLoading,
                items = vm.recentSongs,
                onMoreClick = { global.nav.push(Route.Root.Home.Recent) },
                onLoad = vm::mountRecent,
                onRefresh = {},
                onRetryClick = vm::retryRecent
            )

            Segment(
                label = "每日推荐",
                status = vm.recommendStatus,
                loading = vm.recommendLoading,
                items = vm.recommendSongs,
                onMoreClick = { global.nav.push(Route.Root.Home.Recommend) },
                onLoad = vm::mountRecommend,
                onRefresh = {},
                onRetryClick = vm::retryRecommend
            )

            Segment(
                label = "本周热门",
                status = vm.hotStatus,
                loading = vm.hotLoading,
                items = vm.hotSongs,
                onMoreClick = { global.nav.push(Route.Root.Home.WeeklyHot) },
                onLoad = vm::mountHot,
                onRefresh = {},
                onRetryClick = vm::retryHot
            )

            CategorySegment(category = "纯净哈基米")
            CategorySegment(category = "古典")
            CategorySegment(category = "原曲不使用")
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
        LoadableContent(
            modifier = Modifier.fillMaxWidth().height(228.dp * 2 + AdaptiveListSpacing),
            initializeStatus = status,
            loading = loading,
            onRefresh = onRefresh,
            onRetryClick = onRetryClick,
            onLoad = {}
        ) {
            if (items.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("空空如也")
            } else LazyHorizontalGrid(
                modifier = Modifier.fillMaxSize(),
                rows = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
                verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            ) {
                items(items = items, key = { it.id }) { item ->
                    SongCard(
                        modifier = Modifier.size(width = 180.dp, height = 228.dp),
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
        val state = vm.categoryState[category]
        val status = state?.status?.value ?: InitializeStatus.INIT
        val loading = state?.loading?.value ?: false
        LoadableContent(
            modifier = Modifier.fillMaxWidth().height(228.dp * 2 + AdaptiveListSpacing),
            initializeStatus = status,
            loading = loading,
            onRefresh = { },
            onRetryClick = { vm.retryCategory(category) },
            onLoad = { }
        ) {
            val songs = state?.songs?.value ?: emptyList()
            if (songs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("空空如也")
                }
            } else LazyHorizontalGrid(
                modifier = Modifier.fillMaxSize(),
                rows = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
                verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            ) {
                items(songs, key = { item -> item.id }) { item ->
                    SongCard(
                        modifier = Modifier.size(width = 180.dp, height = 228.dp),
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

        TextButton(
            modifier = Modifier,
            onClick = onMoreClick
        ) {
            Text("更多")
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Play")
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
    AnimatedContent(initializeStatus, modifier = modifier.onFirstVisible { onLoad() }, transitionSpec = {
        if (targetState == InitializeStatus.LOADED) {
            (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                    slideInVertically(initialOffsetY = { slideOffset }, animationSpec = tween(220, delayMillis = 90)))
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