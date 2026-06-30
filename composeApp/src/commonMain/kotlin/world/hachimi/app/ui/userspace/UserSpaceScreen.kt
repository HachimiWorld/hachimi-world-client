package world.hachimi.app.ui.userspace

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.auth_logout
import hachimiworld.composeapp.generated.resources.common_play_cd
import hachimiworld.composeapp.generated.resources.follow_cancel
import hachimiworld.composeapp.generated.resources.follow_unfollow_confirm
import hachimiworld.composeapp.generated.resources.follow_unfollow_confirm_subtitle
import hachimiworld.composeapp.generated.resources.follow_unfollow_confirm_title
import hachimiworld.composeapp.generated.resources.player_play_all
import hachimiworld.composeapp.generated.resources.user_edit_profile
import hachimiworld.composeapp.generated.resources.user_space_activity_empty
import hachimiworld.composeapp.generated.resources.user_space_empty
import hachimiworld.composeapp.generated.resources.user_space_title
import hachimiworld.composeapp.generated.resources.user_space_uid_prefix
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import soup.compose.material.motion.animation.materialFadeThrough
import world.hachimi.app.model.FollowViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.UserSpaceViewModel
import world.hachimi.app.model.fromPublicDetail
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Navigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.component.Pagination
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.follow.components.UnfollowDialog
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.ui.userspace.component.Avatar
import world.hachimi.app.ui.userspace.component.Connections
import world.hachimi.app.ui.userspace.component.GenderIcon
import world.hachimi.app.ui.userspace.component.PublicPlaylistCard
import world.hachimi.app.ui.userspace.component.StatsRow
import world.hachimi.app.ui.userspace.component.TabBar
import world.hachimi.app.util.AdaptiveListSpacing
import world.hachimi.app.util.AdaptiveScreenMargin
import world.hachimi.app.util.WindowSize
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
        var selectedTab by remember { mutableIntStateOf(0) }
        val navigator = LocalNavigator.current

        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = calculateGridColumns(constraintsMaxWidth),
            contentPadding = contentPaddingForMaxWidth(PaddingValues(AdaptiveScreenMargin), constraintsMaxWidth),
            verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Header(vm, global, Modifier.fillMaxWidth())
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                TabBar(selectedIndex = selectedTab, onTabSelected = { selectedTab = it })
            }

            when (selectedTab) {
                0 -> artworkTabContents(vm, global)
                1 -> playlistsTabContents(vm, navigator)
                2 -> {
                    // Activity tab: placeholder
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(modifier = Modifier.height(200.dp), contentAlignment = Alignment.Center) {
                            Text(text = stringResource(Res.string.user_space_activity_empty))
                        }
                    }
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

private fun LazyGridScope.artworkTabContents(
    vm: UserSpaceViewModel,
    global: GlobalStore
) {
    // Songs tab: "All works" title + play all button
    item(span = { GridItemSpan(maxLineSpan) }) {
        if (vm.songs.isNotEmpty()) Button(
            modifier = Modifier.wrapContentWidth(align = Alignment.Start),
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

    if (vm.loadingSongs) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(modifier = Modifier.height(300.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    } else if (vm.songs.isEmpty()) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(modifier = Modifier.height(300.dp), contentAlignment = Alignment.Center) {
                Text(text = stringResource(Res.string.user_space_empty))
            }
        }
    } else {
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
        if (vm.total > vm.pageSize) {
            item(span = { GridItemSpan(maxLineSpan) }) {
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
    }
}

private fun LazyGridScope.playlistsTabContents(
    vm: UserSpaceViewModel,
    navigator: Navigator
) {
    if (vm.loadingPlaylists) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(modifier = Modifier.height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    } else if (vm.publicPlaylists.isEmpty()) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(modifier = Modifier.height(200.dp), contentAlignment = Alignment.Center) {
                Text(text = stringResource(Res.string.user_space_empty))
            }
        }
    } else {
        vm.publicPlaylists.forEach { playlist ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                PublicPlaylistCard(
                    playlist = playlist,
                    onClick = { navigator.push(Route.Root.PublicPlaylist(playlist.id)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun Header(
    vm: UserSpaceViewModel,
    global: GlobalStore,
    modifier: Modifier = Modifier,
) {
    val navigator = LocalNavigator.current
    val isCompact = LocalWindowSize.current.width < WindowSize.COMPACT
    val followVM: FollowViewModel = koinViewModel()

    // When a follow/unfollow action completes, update the profile state locally
    LaunchedEffect(followVM.lastActionResult) {
        followVM.lastActionResult?.let { result ->
            if (vm.profile?.uid == result.uid) {
                vm.updateFollowState(result.isFollowing, result.followerCount)
            }
            followVM.consumeLastActionResult()
        }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.user_space_title),
                style = MaterialTheme.typography.titleLarge
            )
            if (vm.myself) {
                HachimiIconButton(onClick = { navigator.push(Route.Root.EditProfile) }) {
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
                if (isCompact) {
                    // Mobile: centered Column layout
                    Column(
                        modifier,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Avatar(avatarUrl = profile.avatarUrl, size = 120.dp)

                        SelectionContainer {
                            Text(
                                text = profile.username,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (!profile.bio.isNullOrBlank()) {
                            SelectionContainer {
                                Text(
                                    text = profile.bio,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = HachimiTheme.colorScheme.onSurface.copy(0.7f),
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.Center) {
                            profile.gender?.let { GenderIcon(it, Modifier.padding(end = 4.dp)) }

                            SelectionContainer {
                                Text(
                                    text = stringResource(Res.string.user_space_uid_prefix, profile.uid),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        StatsRow(
                            profile = profile,
                            myself = vm.myself,
                            isCompact = isCompact,
                            followVM = followVM,
                            onFollowersClick = { navigator.push(Route.Root.FollowersList) },
                            onFollowingClick = { navigator.push(Route.Root.FollowingList) },
                            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
                        )

                        Connections(vm = vm)
                    }
                } else {
                    // PC: Row layout with avatar on left, info on right
                    Row(modifier, verticalAlignment = Alignment.Top) {
                        Avatar(avatarUrl = profile.avatarUrl)

                        Column(
                            Modifier.padding(start = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SelectionContainer {
                                Text(
                                    text = profile.username,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            if (!profile.bio.isNullOrBlank()) {
                                SelectionContainer {
                                    Text(
                                        text = profile.bio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = HachimiTheme.colorScheme.onSurface.copy(0.7f),
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row {
                                profile.gender?.let { GenderIcon(it, Modifier.padding(end = 4.dp)) }

                                SelectionContainer {
                                    Text(
                                        text = stringResource(Res.string.user_space_uid_prefix, profile.uid),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            StatsRow(
                                profile = profile,
                                myself = vm.myself,
                                isCompact = isCompact,
                                followVM = followVM,
                                onFollowersClick = { navigator.push(Route.Root.FollowersList) },
                                onFollowingClick = { navigator.push(Route.Root.FollowingList) },
                                modifier = Modifier.padding(vertical = 8.dp).wrapContentWidth(align = Alignment.Start)
                            )

                            // Read-only connected accounts
                            Connections(vm = vm)
                        }
                    }
                }
            }
        }
    }

    // Unfollow dialog - shown from profile page too
    followVM.unfollowDialogTarget?.let { target ->
        UnfollowDialog(
            username = target.username,
            subtitle = stringResource(Res.string.follow_unfollow_confirm_subtitle),
            confirmText = stringResource(Res.string.follow_unfollow_confirm),
            cancelText = stringResource(Res.string.follow_cancel),
            confirmTitle = stringResource(Res.string.follow_unfollow_confirm_title, target.username),
            loading = followVM.actionLoading,
            onConfirm = { followVM.confirmUnfollow() },
            onDismiss = { followVM.dismissUnfollowDialog() }
        )
    }
}