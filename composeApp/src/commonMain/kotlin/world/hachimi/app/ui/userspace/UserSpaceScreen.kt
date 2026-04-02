package world.hachimi.app.ui.userspace

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.auth_logout
import hachimiworld.composeapp.generated.resources.common_play_cd
import hachimiworld.composeapp.generated.resources.player_play_all
import hachimiworld.composeapp.generated.resources.user_edit_profile
import hachimiworld.composeapp.generated.resources.user_space_all_works
import hachimiworld.composeapp.generated.resources.user_space_empty
import hachimiworld.composeapp.generated.resources.user_space_female_cd
import hachimiworld.composeapp.generated.resources.user_space_male_cd
import hachimiworld.composeapp.generated.resources.user_space_title
import hachimiworld.composeapp.generated.resources.user_space_uid_prefix
import hachimiworld.composeapp.generated.resources.user_space_user_avatar_cd
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import soup.compose.material.motion.animation.materialFadeThrough
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.getPlatform
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.UserSpaceViewModel
import world.hachimi.app.model.fromPublicDetail
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.Pagination
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.ui.userspace.component.ConnectionChip
import world.hachimi.app.util.AdaptiveListSpacing
import world.hachimi.app.util.calculateGridColumns
import world.hachimi.app.util.contentPaddingForMaxWidth

@Composable
fun UserSpaceScreen(
    uid: Long?,
    vm: UserSpaceViewModel = koinViewModel(),
    global: GlobalStore = koinInject()
) {
    DisposableEffect(vm, uid) {
        vm.mounted(uid)
        onDispose {
            vm.dispose()
        }
    }

    BoxWithConstraints {
        val constraintsMaxWidth = maxWidth

        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = calculateGridColumns(constraintsMaxWidth),
            contentPadding = contentPaddingForMaxWidth(PaddingValues(24.dp), constraintsMaxWidth),
            verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Header(vm, global, Modifier.fillMaxWidth())
            }
            items(vm.songs, key = { it.id }) { song ->
                SongCard(
                    item = song,
                    onClick = {
                        global.player.insertToQueue(
                            item = GlobalStore.MusicQueueItem.fromPublicDetail(song),
                            instantPlay = true,
                            append = false
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                if (vm.total > vm.pageSize) {
                    Pagination(
                        total = vm.total.toInt(),
                        pageSize = vm.pageSize.toInt(),
                        pageIndex = vm.pageIndex.toInt(),
                        onPageChange = { pageIndex, pageSize ->
                            vm.updateSongPage(pageIndex.toLong(), pageSize.toLong())
                        },
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(
                    Modifier.navigationBarsPadding()
                        .padding(LocalContentInsets.current.asPaddingValues())
                )
            }
        }
    }
}

@Composable
private fun Header(
    vm: UserSpaceViewModel, global: GlobalStore, modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.user_space_title),
                style = MaterialTheme.typography.titleLarge
            )
            if (vm.myself) {
                HachimiIconButton(onClick = { global.nav.push(Route.Root.EditProfile) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(Res.string.user_edit_profile)
                    )
                }
                TextButton(onClick = { global.logout() }) {
                    Text(stringResource(Res.string.auth_logout))
                }
            }
        }

        AnimatedContent(
            targetState = vm.loadingProfile,
            transitionSpec = { materialFadeThrough() }
        ) {
            if (it) Box(modifier.height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            } else vm.profile?.let { profile ->
                Row(modifier) {
                    Avatar(avatarUrl = profile.avatarUrl)

                    Column(Modifier.padding(start = 24.dp)) {
                        SelectionContainer {
                            Text(
                                text = profile.username,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        SelectionContainer {
                            Text(
                                modifier = Modifier.padding(top = 4.dp),
                                text = profile.bio ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            profile.gender?.let { GenderIcon(it, Modifier.padding(end = 4.dp)) }

                            SelectionContainer {
                                Text(
                                    text = stringResource(Res.string.user_space_uid_prefix, profile.uid),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // Read-only connected accounts
                        Connections(vm = vm, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
            Text(
                text = stringResource(Res.string.user_space_all_works),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
            )
            Box(Modifier.weight(1f))
            if (vm.songs.isNotEmpty()) Button(
                modifier = Modifier,
                onClick = { vm.playAll() }
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = stringResource(Res.string.common_play_cd)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.player_play_all))
            }
        }

        if (vm.loadingSongs) Box(modifier.height(300.dp), Alignment.Center) {
            CircularProgressIndicator()
        } else if (vm.songs.isEmpty()) {
            Box(modifier.height(300.dp), Alignment.Center) {
                Text(text = stringResource(Res.string.user_space_empty))
            }
        }
    }
}

@Composable
private fun GenderIcon(
    gender: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier.size(16.dp)) {
        when (gender) {
            0 -> Icon(
                Icons.Default.Male,
                contentDescription = stringResource(Res.string.user_space_male_cd)
            )

            1 -> Icon(
                Icons.Default.Female,
                contentDescription = stringResource(Res.string.user_space_female_cd)
            )
        }
    }
}

@Composable
private fun Avatar(avatarUrl: String?) {
    Surface(
        modifier = Modifier.size(120.dp),
        shape = CircleShape
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .httpHeaders(CoilHeaders)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(Res.string.user_space_user_avatar_cd),
            modifier = Modifier.fillMaxSize(),
            filterQuality = FilterQuality.High,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun Connections(
    vm: UserSpaceViewModel,
    modifier: Modifier = Modifier
) {
    val profile = vm.profile ?: return
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        profile.connectedAccounts.fastForEach {
            ConnectionChip(
                name = it.name,
                type = it.type,
                onOpen = { openUserSpaceConnectionUrl(it.type, it.id) }
            )
        }
    }
}

private fun openUserSpaceConnectionUrl(type: String, id: String) {
    when (type) {
        "bilibili" -> getPlatform().openUrl("https://space.bilibili.com/$id")
        else -> {}
    }
}