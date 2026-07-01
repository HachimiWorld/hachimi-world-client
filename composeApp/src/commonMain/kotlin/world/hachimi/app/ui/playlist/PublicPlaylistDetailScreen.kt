package world.hachimi.app.ui.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.PublicPlaylistViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.playlist.components.CompactHeader
import world.hachimi.app.ui.playlist.components.FavoriteButton
import world.hachimi.app.ui.playlist.components.Header
import world.hachimi.app.ui.playlist.components.SongItem
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.InitStatusScaffold
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.ui.util.fillMaxWidthIn
import world.hachimi.app.ui.util.listTailSpacerItem
import kotlin.time.Duration.Companion.seconds

@Composable
fun PublicPlaylistScreen(
    playlistId: Long,
    vm: PublicPlaylistViewModel = koinViewModel()
) {
    DisposableEffect(vm, playlistId) {
        vm.mounted(playlistId)
        onDispose {
            vm.dispose()
        }
    }

    InitStatusScaffold(
        initializeStatus = vm.initStatus,
        isLoading = vm.loading,
        onRetryClick = { vm.retry() },
    ) {
        Box(Modifier.fillMaxSize()) {
            val playlistInfo = vm.playlistInfo
            val userInfo = vm.creatorProfile
            if (playlistInfo != null && userInfo != null) LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AdaptiveScreenMargin),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Header(vm)
                }

                itemsIndexed(vm.songs, key = { _, item -> item.songId }) { index, song ->
                    SongItem(
                        modifier = Modifier.fillMaxWidthIn(),
                        orderIndex = index,
                        title = song.title,
                        onClick = { vm.play(song) },
                        coverUrl = song.coverUrl,
                        artist = song.uploaderName,
                        duration = song.durationSeconds.seconds,
                        editable = false,
                        onRemoveClick = {}
                    )
                }

                listTailSpacerItem()
            }
        }
    }
}

@Composable
private fun Header(
    vm: PublicPlaylistViewModel
) {
    val navigator = LocalNavigator.current
    val playlistInfo = vm.playlistInfo
    val userInfo = vm.creatorProfile

    if (playlistInfo != null && userInfo != null) {
        if (LocalWindowSize.current.width < WindowSize.COMPACT) {
            CompactHeader(
                modifier = Modifier.fillMaxWidthIn().padding(bottom = 8.dp),
                username = userInfo.username,
                avatarUrl = userInfo.avatarUrl,
                description = playlistInfo.description,
                title = playlistInfo.name,
                coverUrl = playlistInfo.coverUrl,
                updateTime = playlistInfo.updateTime,
                count = vm.songs.size,
                onNavToUserClick = {
                    navigator.push(
                        Route.Root.PublicUserSpace(
                            userInfo.uid
                        )
                    )
                },
                onPlayAllClick = { vm.playAll() },
                action = {
                    vm.isFavorite?.let { isFavorite ->
                        FavoriteButton(
                            isFavorite = isFavorite,
                            onFavoriteClick = { vm.favorite(it) },
                            operating = vm.operating
                        )
                    }
                }
            )
        } else {
            Header(
                modifier = Modifier.fillMaxWidthIn().padding(bottom = 8.dp),
                username = userInfo.username,
                avatarUrl = userInfo.avatarUrl,
                description = playlistInfo.description,
                title = playlistInfo.name,
                coverUrl = playlistInfo.coverUrl,
                updateTime = playlistInfo.updateTime,
                count = vm.songs.size,
                onNavToUserClick = { navigator.push(Route.Root.PublicUserSpace(userInfo.uid)) },
                onPlayAllClick = { vm.playAll() },
                extraActions = {
                    vm.isFavorite?.let { isFavorite ->
                        FavoriteButton(
                            isFavorite = isFavorite,
                            onFavoriteClick = { vm.favorite(it) },
                            operating = vm.operating
                        )
                    }
                }
            )
        }
    }
}