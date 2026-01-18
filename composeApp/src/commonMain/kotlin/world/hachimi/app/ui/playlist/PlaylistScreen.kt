package world.hachimi.app.ui.playlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.playlist_empty
import hachimiworld.composeapp.generated.resources.playlist_my_playlists_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.PlaylistViewModel
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.playlist.components.PlaylistItem
import world.hachimi.app.util.AdaptiveListSpacing
import world.hachimi.app.util.calculateGridColumns

@Composable
fun PlaylistScreen(vm: PlaylistViewModel = koinViewModel()) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }
    val global = koinInject<GlobalStore>()
    AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) { status ->
        when (status) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> if (vm.playlists.isEmpty()) Box(
                Modifier.fillMaxSize().navigationBarsPadding()
                    .padding(LocalContentInsets.current.asPaddingValues()),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(Res.string.playlist_empty))
            } else BoxWithConstraints {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = calculateGridColumns(maxWidth),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
                    horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            text = stringResource(Res.string.playlist_my_playlists_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    items(vm.playlists, key = { item -> item.id }) { item ->
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
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(
                            Modifier.navigationBarsPadding().padding(LocalContentInsets.current.asPaddingValues())
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