package world.hachimi.app.ui.playlist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import world.hachimi.app.model.PlaylistDetailViewModel
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.singleLined

@Composable
fun EditDialog(vm: PlaylistDetailViewModel) {
    if (vm.showEditDialog) EditDialog(
        name = vm.editName,
        onNameChange = { vm.editName = it.singleLined() },
        description = vm.editDescription,
        onDescriptionChange = { vm.editDescription = it },
        private = vm.editPrivate,
        onPrivateChange = { vm.editPrivate = it },
        onCancelClick = { vm.cancelEdit() },
        onConfirmClick = { vm.confirmEdit() },
        processing = vm.editOperating
    )
}

@Composable
private fun EditDialog(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    private: Boolean,
    onPrivateChange: (Boolean) -> Unit,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit,
    processing: Boolean
) {
    AlertDialog(
        onDismissRequest = onCancelClick,
        title = { Text(text = "编辑歌单") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = { Text("名称") },
                    singleLine = true
                )
                TextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    placeholder = { Text("描述") },
                    minLines = 3,
                    maxLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("私有歌单")
                    Switch(
                        modifier = Modifier.padding(start = 16.dp),
                        checked = private,
                        onCheckedChange = onPrivateChange
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmClick,
                enabled = name.isNotBlank() && !processing
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelClick) {
                Text("取消")
            }
        }
    )
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        EditDialog(
            name = "",
            onNameChange = { },
            description = "",
            onDescriptionChange = { },
            private = false,
            onPrivateChange = { },
            onCancelClick = { },
            onConfirmClick = { },
            processing = false
        )
    }
}