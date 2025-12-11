package world.hachimi.app.ui.userspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.UserSpaceViewModel
import world.hachimi.app.model.fromPublicDetail
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.util.AdaptiveListSpacing
import world.hachimi.app.util.calculateGridColumns

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
            modifier = Modifier.fillMaxSize(),
            columns = calculateGridColumns(maxWidth),
            verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            contentPadding = PaddingValues(24.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Header(vm, global)
            }
            items(vm.songs, key = { it.id }) { song ->
                SongCard(
                    item = song,
                    onClick = {
                        global.player.insertToQueue(
                            GlobalStore.MusicQueueItem.fromPublicDetail(song),
                            true,
                            false
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.navigationBarsPadding().windowInsetsBottomHeight(LocalContentInsets.current))
            }
        }
    }

    ChangeBioDialog(vm)
    ChangeUsernameDialog(vm)
}

@Composable
private fun Header(vm: UserSpaceViewModel, global: GlobalStore) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = "神人空间",
                style = MaterialTheme.typography.titleLarge
            )
            if (vm.myself) TextButton(onClick = { global.logout() }) {
                Text("退出登录")
            }
        }

        if (vm.loadingProfile) Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        } else vm.profile?.let { profile ->
            Row(Modifier.fillMaxWidth()) {
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
                                contentDescription = "User Avatar",
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
                        Text(
                            modifier = Modifier.clickable(enabled = vm.myself) { vm.editUsername() },
                            text = profile.username,
                            style = MaterialTheme.typography.labelMedium
                        )
                        profile.gender?.let {
                            Box(Modifier.padding(start = 4.dp).size(16.dp)) {
                                when (profile.gender) {
                                    0 -> Icon(Icons.Default.Male, contentDescription = "Male")
                                    1 -> Icon(Icons.Default.Female, contentDescription = "Female")
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.width(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectionContainer {
                        Text(text = "UID: ${profile.uid}", style = MaterialTheme.typography.labelMedium)
                    }
                    Text(text = "介绍", style = MaterialTheme.typography.labelMedium)

                    Surface(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
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

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "全部作品",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                )
                Box(Modifier.weight(1f))
                if (vm.songs.isNotEmpty()) Button(
                    modifier = Modifier,
                    onClick = { vm.playAll() }
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    Spacer(Modifier.width(8.dp))
                    Text("播放全部")
                }
            }

            if (vm.loadingSongs) Box(Modifier.fillMaxWidth().height(300.dp), Alignment.Center) {
                CircularProgressIndicator()
            } else if (vm.songs.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(300.dp), Alignment.Center) {
                    Text(text = "什么也没有")
                }
            }
        }
    }
}


@Composable
private fun ChangeUsernameDialog(vm: UserSpaceViewModel) {
    if (vm.showEditUsername) AlertDialog(
        onDismissRequest = { vm.cancelEdit() },
        title = {
            Text("更改昵称")
        },
        text = {
            TextField(value = vm.editUsernameValue, onValueChange = { vm.editUsernameValue = it })
        },
        confirmButton = {
            TextButton(
                onClick = { vm.confirmEditUsername() },
                enabled = !vm.operating && vm.editUsernameValue.isNotBlank()
            ) {
                Text("更改")
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.cancelEdit() }) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun ChangeBioDialog(vm: UserSpaceViewModel) {
    if (vm.showEditBio) AlertDialog(
        onDismissRequest = { vm.cancelEdit() },
        title = {
            Text("更改简介")
        },
        text = {
            TextField(value = vm.editBioValue, onValueChange = { vm.editBioValue = it })
        },
        confirmButton = {
            TextButton(
                onClick = { vm.confirmEditBio() },
                enabled = !vm.operating && vm.editBioValue.isNotBlank()
            ) {
                Text("更改")
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.cancelEdit() }) {
                Text("取消")
            }
        }
    )
}