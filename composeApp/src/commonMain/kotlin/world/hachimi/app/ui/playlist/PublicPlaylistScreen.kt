package world.hachimi.app.ui.playlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.play_all
import hachimiworld.composeapp.generated.resources.playlist_cover_cd
import hachimiworld.composeapp.generated.resources.playlist_description_placeholder
import hachimiworld.composeapp.generated.resources.playlist_favorite
import hachimiworld.composeapp.generated.resources.playlist_song_count
import hachimiworld.composeapp.generated.resources.playlist_songs_list
import hachimiworld.composeapp.generated.resources.playlist_unfavorite
import hachimiworld.composeapp.generated.resources.song_cover_cd
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import soup.compose.material.motion.animation.materialFadeThrough
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.PublicPlaylistViewModel
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LinearProgressIndicator
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.player.fullscreen.components.UserChip
import world.hachimi.app.ui.playlist.components.SongItem
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.fillMaxWidthIn
import kotlin.time.Duration.Companion.seconds

@Composable
fun PublicPlaylistScreen(
    playlistId: Long,
    vm: PublicPlaylistViewModel = koinViewModel(),
    global: GlobalStore = koinInject()
) {
    DisposableEffect(vm, playlistId) {
        vm.mounted(playlistId)
        onDispose {
            vm.dispose()
        }
    }

    AnimatedContent(
        targetState = vm.initStatus,
        transitionSpec = { materialFadeThrough() }
    ) { initStatus ->
        when (initStatus) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> Box(Modifier.fillMaxSize()) {
                val playlistInfo = vm.playlistInfo
                val userInfo = vm.creatorProfile
                if (playlistInfo != null && userInfo != null) LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Header(
                            modifier = Modifier.fillMaxWidthIn(),
                            username = userInfo.username,
                            avatarUrl = userInfo.avatarUrl,
                            description = playlistInfo.description,
                            title = playlistInfo.name,
                            coverUrl = playlistInfo.coverUrl,
                            count = vm.songs.size,
                            onNavToUserClick = { global.nav.push(Route.Root.PublicUserSpace(userInfo.uid)) },
                            onPlayAllClick = { vm.playAll() },
                            isFavorite = vm.isFavorite,
                            operating = vm.operating,
                            onFavoriteClick = { vm.favorite(it) }
                        )
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

                    item {
                        Spacer(
                            Modifier.navigationBarsPadding()
                                .padding(LocalContentInsets.current.asPaddingValues())
                        )
                    }
                }
                if (vm.loading) LinearProgressIndicator(
                    Modifier.align(Alignment.TopStart).fillMaxWidthIn()
                )
            }
        }
    }
}

@Composable
private fun Header(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    coverUrl: String?,
    username: String,
    avatarUrl: String?,
    count: Int,
    onPlayAllClick: () -> Unit,
    onNavToUserClick: () -> Unit,
    isFavorite: Boolean?,
    operating: Boolean,
    onFavoriteClick: (Boolean) -> Unit
) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.size(128.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(LocalContentColor.current.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                val hazeState = rememberHazeState()
                AsyncImage(
                    modifier = Modifier.hazeSource(hazeState).fillMaxSize(),
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(Res.string.playlist_cover_cd),
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(LocalContentColor.current.copy(alpha = 0.12f))
                )
            }

            Column(
                modifier = Modifier.weight(1f).padding(start = 24.dp).height(120.dp)
                    .clip(MaterialTheme.shapes.small)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    text = description
                        ?: stringResource(Res.string.playlist_description_placeholder),
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis
                )


                val avatar = avatarUrl?.let {
                    rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalPlatformContext.current)
                            .crossfade(true)
                            .data(it)
                            .build()
                    )
                }

                UserChip(
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = onNavToUserClick,
                    avatar = avatar,
                    name = username
                )
            }
        }


        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.playlist_songs_list),
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = stringResource(Res.string.playlist_song_count, count),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.weight(1f))
            Button(
                modifier = Modifier,
                onClick = onPlayAllClick
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = stringResource(Res.string.song_cover_cd)
                )
                Spacer(Modifier.width(16.dp))
                Text(stringResource(Res.string.play_all))
            }

            isFavorite?.let { isFavorite ->
                if (isFavorite) Button(onClick = {
                    onFavoriteClick(!isFavorite)
                }, enabled = !operating) {
                    Text(stringResource(Res.string.playlist_unfavorite))
                } else AccentButton(onClick = {
                    onFavoriteClick(!isFavorite)
                }, enabled = !operating) {
                    Text(stringResource(Res.string.playlist_favorite))
                }
            }
        }
    }
}


@Composable
@Preview
private fun PreviewHeader() {
    PreviewTheme(background = true) {
        Header(
            modifier = Modifier.fillMaxWidthIn(),
            title = "Title",
            description = "Description",
            coverUrl = null,
            username = "username",
            avatarUrl = null,
            count = 10,
            onPlayAllClick = {},
            onNavToUserClick = {},
            isFavorite = false,
            onFavoriteClick = {},
            operating = false
        )
    }
}