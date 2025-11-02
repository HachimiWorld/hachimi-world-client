package world.hachimi.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.RecentPublishViewModel
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.util.WindowSize
import world.hachimi.app.util.calculateGridColumns
import kotlin.time.Duration.Companion.seconds

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
            isRefreshing = vm.initializeStatus != InitializeStatus.INIT && vm.isLoading,
            onRefresh = { vm.fakeRefresh() },
            screenWidth = maxWidth
        ) {
            if (vm.songs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("空空如也")
            } else LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = calculateGridColumns(maxWidth),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "最近发布", style = MaterialTheme.typography.titleLarge
                        )
                        if (maxWidth >= WindowSize.COMPACT) {
                            IconButton(
                                modifier = Modifier.padding(start = 8.dp),
                                enabled = !vm.isLoading,
                                onClick = { vm.fakeRefresh() }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        Button(
                            modifier = Modifier,
                            onClick = { vm.playAllRecent() }
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                            Spacer(Modifier.width(8.dp))
                            Text("播放全部")
                        }
                    }
                }
                itemsIndexed(vm.songs, key = { index, item -> item.id }) { index, item ->
                    SongCard(
                        modifier = Modifier.fillMaxWidth(),
                        coverUrl = item.coverUrl,
                        title = item.title,
                        subtitle = item.subtitle,
                        author = item.uploaderName,
                        tags = item.tags.map { it.name },
                        likeCount = item.likeCount,
                        playCount = item.playCount,
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
    }
}