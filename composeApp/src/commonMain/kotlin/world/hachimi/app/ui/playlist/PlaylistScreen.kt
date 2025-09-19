package world.hachimi.app.ui.playlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.time.Instant
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.PlaylistViewModel
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.playlist.components.PlaylistItem

@Composable
fun PlaylistScreen(vm: PlaylistViewModel = koinViewModel()) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }
    val global = koinInject<GlobalStore>()
    Column(Modifier.fillMaxSize()) {
        Text(modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 24.dp), text = "我的歌单", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(24.dp))

        AnimatedContent(vm.initializeStatus, modifier = Modifier.weight(1f)) {
            when (it) {
                InitializeStatus.INIT -> LoadingPage()
                InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
                InitializeStatus.LOADED -> Box(Modifier.fillMaxSize()) {
                    if (vm.playlists.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("空空如也")
                    } else LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Adaptive(minSize = 180.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        itemsIndexed(vm.playlists, key = { index, item -> item.id }) { index, item ->
                            PlaylistItem(
                                modifier = Modifier.fillMaxWidth(),
                                coverUrl = item.coverUrl,
                                title = item.name,
                                songCount = item.songsCount,
                                createTime = item.createTime,
                                onEnter = {
                                    global.nav.push(Route.Root.MyPlaylist.Detail(item.id))
                                }
                            )
                        }
                    }
                    if (vm.playlistIsLoading) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

