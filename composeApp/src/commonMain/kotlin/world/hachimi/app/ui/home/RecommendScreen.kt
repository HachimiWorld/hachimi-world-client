package world.hachimi.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import world.hachimi.app.model.fromPublicDetail
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.util.AdaptiveListSpacing
import world.hachimi.app.util.calculateGridColumns

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
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
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
            horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing)
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
                            text = "并非根据你的口味进行推荐",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        modifier = Modifier.align(Alignment.Top),
                        onClick = { 
                            val items = vm.songs.map { song ->
                                GlobalStore.MusicQueueItem.fromPublicDetail(song)
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
                    modifier = Modifier.fillMaxWidth(),
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
            item(span = { GridItemSpan(maxLineSpan) }){
                Spacer(Modifier.navigationBarsPadding().windowInsetsBottomHeight(LocalContentInsets.current))
            }
        }
    }
}