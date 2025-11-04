package world.hachimi.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.HomeViewModel
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.DevelopingPage
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.util.WindowSize
import kotlin.collections.emptyList
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(content: Route.Root.Home) {
    AnimatedContent(content) { content ->
        when (content) {
            Route.Root.Home.Main -> HomeMainScreen()
            Route.Root.Home.Recent -> RecentPublishScreen()
            Route.Root.Home.Recommend -> RecommendScreen()
            else -> DevelopingPage()
        }
    }
}

@Composable
fun HomeMainScreen(
    vm: HomeViewModel = koinViewModel(),
    global: GlobalStore = koinInject()
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.unmount() }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxWidth = maxWidth
        AdaptivePullToRefreshBox(
            isRefreshing = vm.recentStatus != InitializeStatus.INIT && vm.refreshing,
            onRefresh = { vm.fakeRefresh() },
            screenWidth = maxWidth
        ) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                SegmentHeader("最近发布", onMoreClick = {
                    global.nav.push(Route.Root.Home.Recent)
                })
                Spacer(Modifier.height(24.dp))
                LoadableContent(
                    modifier = Modifier.fillMaxWidth().height(520.dp),
                    initializeStatus = vm.recentStatus,
                    loading = vm.recentLoading,
                    onRefresh = { },
                    onRetryClick = { vm.retryRecent() }
                ) {
                    if (vm.recentSongs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("空空如也")
                    } else LazyHorizontalGrid(
                        modifier = Modifier.fillMaxSize(),
                        rows = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(vm.recentSongs, key = { item -> item.id }) { item ->
                            SongCard(
                                modifier = Modifier.width(width = 180.dp),
                                item = item,
                                onClick = {
                                    global.player.insertToQueue(
                                        GlobalStore.MusicQueueItem(
                                            id = item.id,
                                            displayId = item.displayId,
                                            name = item.title,
                                            artist = item.uploaderName,
                                            duration = item.durationSeconds.seconds,
                                            coverUrl = item.coverUrl
                                        ), true, false
                                    )
                                },
                            )
                        }
                    }
                }

                SegmentHeader("每日推荐", onMoreClick = {
                    global.nav.push(Route.Root.Home.Recommend)
                })
                Spacer(Modifier.height(24.dp))
                LoadableContent(
                    modifier = Modifier.fillMaxWidth().height(520.dp),
                    initializeStatus = vm.recommendStatus,
                    loading = vm.recommendLoading,
                    onRefresh = { },
                    onRetryClick = { vm.retryRecommend() }
                ) {
                    if (vm.recommendSongs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("空空如也")
                    } else LazyHorizontalGrid(
                        modifier = Modifier.fillMaxWidth(),
                        rows = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(vm.recommendSongs, key = { item -> item.id }) { item ->
                            SongCard(
                                modifier = Modifier.width(width = 180.dp),
                                item = item,
                                onClick = {
                                    global.player.insertToQueue(
                                        GlobalStore.MusicQueueItem(
                                            id = item.id,
                                            displayId = item.displayId,
                                            name = item.title,
                                            artist = item.uploaderName,
                                            duration = item.durationSeconds.seconds,
                                            coverUrl = item.coverUrl
                                        ), true, false
                                    )
                                },
                            )
                        }
                    }
                }

                SegmentHeader("本周热门", onMoreClick = {
                    global.nav.push(Route.Root.Home.WeeklyTop)
                })
                Spacer(Modifier.height(24.dp))
                LoadableContent(
                    modifier = Modifier.fillMaxWidth().height(520.dp),
                    initializeStatus = vm.hotStatus,
                    loading = vm.hotLoading,
                    onRefresh = { },
                    onRetryClick = { vm.retryHot() }
                ) {
                    if (vm.hotSongs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("空空如也")
                    } else LazyHorizontalGrid(
                        modifier = Modifier.fillMaxSize(),
                        rows = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(vm.hotSongs, key = { item -> item.id }) { item ->
                            SongCard(
                                modifier = Modifier.width(width = 180.dp),
                                item = item,
                                onClick = {
                                    global.player.insertToQueue(
                                        GlobalStore.MusicQueueItem(
                                            id = item.id,
                                            displayId = item.displayId,
                                            name = item.title,
                                            artist = item.uploaderName,
                                            duration = item.durationSeconds.seconds,
                                            coverUrl = item.coverUrl
                                        ), true, false
                                    )
                                },
                            )
                        }
                    }
                }

                SegmentHeader("纯净哈基米专区", onMoreClick = {
                    global.nav.push(Route.Root.Home.HiddenGem)
                })
                Spacer(Modifier.height(24.dp))
                LoadableContent(
                    modifier = Modifier.fillMaxWidth().height(520.dp),
                    initializeStatus = vm.pureStatus,
                    loading = vm.pureLoading,
                    onRefresh = { },
                    onRetryClick = { vm.retryPure() }
                ) {
                    if (vm.pureSongs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("空空如也")
                    } else LazyHorizontalGrid(
                        modifier = Modifier.fillMaxSize(),
                        rows = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(vm.pureSongs, key = { item -> item.id }) { item ->
                            SongCard(
                                modifier = Modifier.width(width = 180.dp),
                                coverUrl = item.coverArtUrl,
                                title = item.title,
                                subtitle = item.subtitle,
                                author = item.uploaderName,
                                tags = remember { emptyList<String>() },
                                playCount = item.playCount,
                                likeCount = item.likeCount,
                                onClick = {
                                    global.player.insertToQueue(
                                        GlobalStore.MusicQueueItem(
                                            id = item.id,
                                            displayId = item.displayId,
                                            name = item.title,
                                            artist = item.uploaderName,
                                            duration = item.durationSeconds.seconds,
                                            coverUrl = item.coverArtUrl
                                        ), true, false
                                    )
                                },
                            )
                        }
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
) {
    Row(
        modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp),
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
    content: @Composable () -> Unit,
) {
    val slideOffset = with(LocalDensity.current) {
        32.dp.roundToPx()
    }
    AnimatedContent(initializeStatus, modifier = modifier, transitionSpec = {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptivePullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    screenWidth: Dp,
    content: @Composable BoxScope.() -> Unit
) {
    if (screenWidth >= WindowSize.COMPACT) {
        Box(content = content)
    } else PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        content = content
    )
}
