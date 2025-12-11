package world.hachimi.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.CategorySongsViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.fromSearchSongItem
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.util.AdaptiveListSpacing
import world.hachimi.app.util.calculateGridColumns

@Composable
fun CategorySongsScreen(
    category: String,
    vm: CategorySongsViewModel = koinViewModel(),
    global: GlobalStore = koinInject(),
) {
    DisposableEffect(category, vm) {
        vm.mounted(category)
        onDispose { vm.unmount() }
    }
    AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) {
        when (it) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> Content(category, vm, global)
        }
    }
}

@Composable
private fun Content(category: String, vm: CategorySongsViewModel, global: GlobalStore) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (vm.songs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("空空如也")
        } else LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = calculateGridColumns(maxWidth),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                FlowRow(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = "${category}专区", style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.weight(1f))

                    Button(
                        modifier = Modifier.align(Alignment.Top),
                        onClick = {
                            val items = vm.songs.map { song ->
                                GlobalStore.MusicQueueItem.fromSearchSongItem(song)
                            }
                            global.player.playAll(items)
                        }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                        Spacer(Modifier.width(8.dp))
                        Text("播放全部")
                    }
                }
            }
            items(vm.songs, key = { item -> item.id }) { item ->
                SongCard(
                    modifier = Modifier.width(width = 180.dp),
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
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.navigationBarsPadding().windowInsetsBottomHeight(LocalContentInsets.current))
            }
        }
    }
}