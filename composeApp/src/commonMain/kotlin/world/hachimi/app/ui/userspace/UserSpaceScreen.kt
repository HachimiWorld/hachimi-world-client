package world.hachimi.app.ui.userspace

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
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
import coil3.compose.AsyncImage
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.auth_logout
import hachimiworld.composeapp.generated.resources.common_cancel
import hachimiworld.composeapp.generated.resources.common_change
import hachimiworld.composeapp.generated.resources.common_play_cd
import hachimiworld.composeapp.generated.resources.player_play_all
import hachimiworld.composeapp.generated.resources.user_change_bio
import hachimiworld.composeapp.generated.resources.user_change_nickname
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
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.UserSpaceViewModel
import world.hachimi.app.model.fromPublicDetail
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.util.AdaptiveListSpacing
import world.hachimi.app.util.calculateGridColumns
import world.hachimi.app.util.fillMaxWidthIn

@Composable
fun UserSpaceScreen(uid: Long?, vm: UserSpaceViewModel = koinViewModel()) {
    DisposableEffect(vm) {
        vm.mounted(uid)
        onDispose {
            vm.dispose()
        }
    }
    val global = koinInject<GlobalStore>()

    BoxWithConstraints {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize().fillMaxWidthIn(),
            columns = calculateGridColumns(maxWidth),
            verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            contentPadding = PaddingValues(24.dp)
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
                Spacer(
                    Modifier.navigationBarsPadding()
                        .padding(LocalContentInsets.current.asPaddingValues())
                )
            }
        }
    }

    ChangeBioDialog(vm)
    ChangeUsernameDialog(vm)
}

@Composable
private fun Header(
    vm: UserSpaceViewModel, global: GlobalStore, modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(modifier, verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.user_space_title),
                style = MaterialTheme.typography.titleLarge
            )
            if (vm.myself) TextButton(onClick = { global.logout() }) {
                Text(stringResource(Res.string.auth_logout))
            }
        }

        AnimatedContent(
            targetState = vm.loadingProfile,
            transitionSpec = { materialFadeThrough() }
        ) {
            if (it) Box(modifier.height(300.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            } else vm.profile?.let { profile ->
                Row(modifier) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(128.dp),
                            shape = CircleShape
                        ) {
                            Box(
                                modifier = Modifier.clickable(
                                    enabled = vm.myself,
                                    onClick = { vm.editAvatar() }
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = profile.avatarUrl,
                                    contentDescription = stringResource(Res.string.user_space_user_avatar_cd),
                                    modifier = Modifier.fillMaxSize(),
                                    filterQuality = FilterQuality.High,
                                    contentScale = ContentScale.Crop
                                )

                                if (vm.avatarUploading) {
                                    if (vm.avatarUploadProgress > 0f && vm.avatarUploadProgress < 1f)
                                        CircularProgressIndicator(progress = { vm.avatarUploadProgress })
                                    else CircularProgressIndicator()
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SelectionContainer {
                                Text(
                                    modifier = Modifier.clickable(enabled = vm.myself) { vm.editUsername() },
                                    text = profile.username,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            profile.gender?.let {
                                Box(Modifier.padding(start = 4.dp).size(16.dp)) {
                                    when (profile.gender) {
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
                        }
                    }
                    Spacer(Modifier.width(24.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectionContainer {
                            Text(
                                text = stringResource(
                                    Res.string.user_space_uid_prefix,
                                    profile.uid
                                ), style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Surface(
                            modifier = modifier.height(100.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                Modifier.clickable(
                                    enabled = vm.myself,
                                    onClick = { vm.editBio() }
                                ).padding(12.dp)
                            ) {
                                SelectionContainer {
                                    Text(
                                        text = profile.bio ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
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
private fun ChangeUsernameDialog(vm: UserSpaceViewModel) {
    if (vm.showEditUsername) AlertDialog(
        onDismissRequest = { vm.cancelEdit() },
        title = {
            Text(stringResource(Res.string.user_change_nickname))
        },
        text = {
            TextField(
                modifier = Modifier.requiredWidth(280.dp),
                value = vm.editUsernameValue,
                onValueChange = { vm.editUsernameValue = it }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { vm.confirmEditUsername() },
                enabled = !vm.operating && vm.editUsernameValue.isNotBlank()
            ) {
                Text(stringResource(Res.string.common_change))
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.cancelEdit() }) {
                Text(stringResource(Res.string.common_cancel))
            }
        }
    )
}

@Composable
private fun ChangeBioDialog(vm: UserSpaceViewModel) {
    if (vm.showEditBio) AlertDialog(
        onDismissRequest = { vm.cancelEdit() },
        title = {
            Text(stringResource(Res.string.user_change_bio))
        },
        text = {
            TextField(
                modifier = Modifier.requiredWidth(280.dp),
                value = vm.editBioValue, onValueChange = { vm.editBioValue = it },
                minLines = 3
            )
        },
        confirmButton = {
            TextButton(
                onClick = { vm.confirmEditBio() },
                enabled = !vm.operating && vm.editBioValue.isNotBlank()
            ) {
                Text(stringResource(Res.string.common_change))
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.cancelEdit() }) {
                Text(stringResource(Res.string.common_cancel))
            }
        }
    )
}