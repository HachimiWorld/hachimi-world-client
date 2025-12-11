package world.hachimi.app.ui.playlist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.hachimi.app.model.PlaylistDetailViewModel
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.singleLined

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDialog(vm: PlaylistDetailViewModel) {
    if (vm.showEditDialog) BasicAlertDialog(
        onDismissRequest = { vm.cancelEdit() }
    ) {
        ElevatedCard(elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)) {

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "编辑歌单信息", style = MaterialTheme.typography.titleLarge)
                TextField(
                    value = vm.editName,
                    onValueChange = { vm.editName = it.singleLined() },
                    label = { Text("名称") },
                    singleLine = true
                )
                TextField(
                    value = vm.editDescription,
                    onValueChange = { vm.editDescription = it },
                    label = { Text("描述") },
                    minLines = 3,
                    maxLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("私有歌单")
                    Switch(
                        modifier = Modifier.padding(start = 16.dp),
                        checked = vm.editPrivate,
                        onCheckedChange = { vm.editPrivate = it }
                    )
                }

                Row(Modifier.align(Alignment.End)) {
                    TextButton(onClick = { vm.cancelEdit() }) {
                        Text("取消")
                    }
                    TextButton(
                        onClick = { vm.confirmEdit() },
                        enabled = vm.editName.isNotBlank() && !vm.editOperating
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}