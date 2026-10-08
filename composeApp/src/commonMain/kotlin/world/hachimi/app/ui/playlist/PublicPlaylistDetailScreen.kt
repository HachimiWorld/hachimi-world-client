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
import org.koin.compose.koinInject
import world.hachimi.app.api.module.ReportModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.ReportStore
import world.hachimi.app.ui.report.ReportMenuButton
import world.hachimi.app.model.PublicPlaylistViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.playlist.components.CompactHeader
import world.hachimi.app.ui.playlist.components.FavoriteButton
import world.hachimi.app.ui.playlist.components.Header
import world.hachimi.app.ui.playlist.components.SongItem
import world.hachimi.app.ui.playlist.components.UnavailableSongItem
import world.hachimi.app.ui.playlist.components.PlaylistEntry
import world.hachimi.app.ui.playlist.components.playlistEntries
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.InitStatusScaffold
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.ui.util.fillMaxWidthIn
import world.hachimi.app.ui.util.listHeadInsetsSpacerItem
import world.hachimi.app.ui.util.listTailSpacerItem
import kotlin.time.Duration.Companion.seconds

@Composable
fun PublicPlaylistScreen(
    playlistId: Long,
    vm: PublicPlaylistViewModel = koinViewModel(),
    global: GlobalStore = koinInject(),
    reports: ReportStore = koinInject(),
) {
    val navigator = LocalNavigator.current

    DisposableEffect(vm, playlistId) {
        vm.mounted(playlistId)
        onDispose {
            vm.dispose()
        }
    }

    ScreenScaffold(
        title = { Text(vm.playlistInfo?.name.orEmpty(), maxLines = 1) },
        showBack = true,
        onBack = navigator::back,
        actions = {
            val owner = vm.creatorProfile?.uid
            if (owner != null && owner != global.userInfo?.uid) {
                ReportMenuButton { reports.open(ReportModule.TARGET_PLAYLIST, playlistId) }
            }
        },
    ) {
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
                    listHeadInsetsSpacerItem()
                    item {
                        Header(vm)
                    }

                    itemsIndexed(playlistEntries(vm.songs, vm.unavailableSongs), key = { _, entry -> entry.key }) { index, entry ->
                        if (entry is PlaylistEntry.Unavailable) {
                            UnavailableSongItem(onRemoveClick = null, modifier = Modifier.fillMaxWidthIn())
                            return@itemsIndexed
                        }
                        val song = (entry as PlaylistEntry.Song).song
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