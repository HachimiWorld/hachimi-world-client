package world.hachimi.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.RecommendViewModel
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.util.calculateGridColumns
import kotlin.time.Duration.Companion.seconds

@Composable
fun RecommendScreen(
    vm: RecommendViewModel = koinViewModel(),
    global: GlobalStore = koinInject(),
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.unmount() }
    }
    AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) {
        when (it) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { })
            InitializeStatus.LOADED -> Content(vm, global)
        }
    }
}

@Composable
private fun Content(vm: RecommendViewModel, global: GlobalStore) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
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
                FlowRow(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column {
                        Text(
                            text = "每日推荐", style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "并非根据你的口味进行推荐。每日 6:00 刷新",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        modifier = Modifier.align(Alignment.Top),
                        onClick = { 
                            val items = vm.songs.map { song ->
                                GlobalStore.MusicQueueItem(
                                    id = song.id,
                                    displayId = song.displayId,
                                    name = song.title,
                                    artist = song.uploaderName,
                                    duration = song.durationSeconds.seconds,
                                    coverUrl = song.coverUrl
                                )
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