package world.hachimi.app.ui.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.PlaylistViewModel
import world.hachimi.app.util.singleLined

@Composable
fun CreatePlaylistDialog(vm: PlaylistViewModel = koinViewModel()) {
    if (vm.showCreatePlaylistDialog) AlertDialog(
        title = { Text("新建歌单") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = vm.createPlaylistName,
                    onValueChange = { vm.createPlaylistName = it.singleLined() },
                    label = { Text("名称") },
                    singleLine = true
                )
                TextField(
                    value = vm.createPlaylistDescription,
                    onValueChange = { vm.createPlaylistDescription = it },
                    label = { Text("描述") },
                    minLines = 3,
                    maxLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("私有歌单")
                    Switch(
                        modifier = Modifier.padding(start = 16.dp),
                        checked = vm.createPlaylistPrivate,
                        onCheckedChange = { vm.createPlaylistPrivate = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { vm.confirmCreatePlaylist() },
                enabled = vm.createPlaylistName.isNotBlank() && !vm.createPlaylistOperating
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.cancelCreatePlaylist() }) {
                Text("取消")
            }
        },
        onDismissRequest = {
            vm.cancelCreatePlaylist()
        }
    )
}