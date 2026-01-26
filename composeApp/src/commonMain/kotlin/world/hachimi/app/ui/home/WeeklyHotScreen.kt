package world.hachimi.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_empty
import hachimiworld.composeapp.generated.resources.common_play_cd
import hachimiworld.composeapp.generated.resources.home_weekly_subtitle
import hachimiworld.composeapp.generated.resources.home_weekly_title
import hachimiworld.composeapp.generated.resources.play_all
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.WeeklyHotViewModel
import world.hachimi.app.model.fromPublicDetail
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.util.AdaptiveListSpacing
import world.hachimi.app.util.calculateGridColumns

@Composable
fun WeeklyHotScreen(
    vm: WeeklyHotViewModel = koinViewModel(),
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
private fun Content(vm: WeeklyHotViewModel, global: GlobalStore) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (vm.songs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(Res.string.common_empty))
        } else LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = calculateGridColumns(maxWidth),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                FlowRow(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.home_weekly_title), style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = stringResource(Res.string.home_weekly_subtitle),
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
                        Icon(Icons.Default.PlayArrow, contentDescription = stringResource(Res.string.common_play_cd))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.play_all))
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
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.navigationBarsPadding().padding(LocalContentInsets.current.asPaddingValues()))
            }
        }
    }
}