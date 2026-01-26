package world.hachimi.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
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
import hachimiworld.composeapp.generated.resources.common_no_more
import hachimiworld.composeapp.generated.resources.common_play_cd
import hachimiworld.composeapp.generated.resources.common_refresh_cd
import hachimiworld.composeapp.generated.resources.home_recent_title
import hachimiworld.composeapp.generated.resources.play_all
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.RecentPublishViewModel
import world.hachimi.app.model.fromPublicDetail
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.home.components.AdaptivePullToRefreshBox
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.util.AdaptiveListSpacing
import world.hachimi.app.util.WindowSize
import world.hachimi.app.util.calculateGridColumns
import world.hachimi.app.util.formatDaysDistance
import kotlin.time.Clock

@Composable
fun RecentPublishScreen(
    vm: RecentPublishViewModel = koinViewModel(),
    global: GlobalStore = koinInject()
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.unmount() }
    }
    AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) {
        when (it) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> Content(vm, global)
        }
    }
}


@Composable
private fun Content(vm: RecentPublishViewModel, global: GlobalStore) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxWidth = maxWidth
        AdaptivePullToRefreshBox(
            isRefreshing = vm.initializeStatus != InitializeStatus.INIT && vm.refreshing,
            onRefresh = { vm.fakeRefresh() },
            screenWidth = maxWidth
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
                columns = calculateGridColumns(maxWidth),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
                verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(Res.string.home_recent_title), style = MaterialTheme.typography.titleLarge
                        )
                        if (maxWidth >= WindowSize.COMPACT) {
                            HachimiIconButton(
                                modifier = Modifier.padding(start = 8.dp),
                                enabled = !vm.loading,
                                onClick = { vm.fakeRefresh() }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.common_refresh_cd))
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        Button(
                            modifier = Modifier,
                            onClick = { vm.playAllRecent() }
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = stringResource(Res.string.common_play_cd))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(Res.string.play_all))
                        }
                    }
                }
                vm.songs.fastForEach {
                    item(
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = "separator"
                    ) {
                        Text(
                            text = remember(it.date) {
                                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                                val daysOffset = today.toEpochDays() - it.date.toEpochDays()
                                formatDaysDistance(daysOffset.toInt()) ?: it.date.toString()
                            },
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
                if (vm.loading) {
                    item(
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = "loading_indicator"
                    ) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                if (!vm.hasMore) {
                    item(
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = "loading_indicator"
                    ) {
                        Text(text = stringResource(Res.string.common_no_more), modifier = Modifier.fillMaxWidth().height(48.dp).wrapContentSize())
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.navigationBarsPadding().padding(LocalContentInsets.current.asPaddingValues()))
                }
            }
        }
    }
}