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
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_cancel
import hachimiworld.composeapp.generated.resources.player_create_playlist_create
import hachimiworld.composeapp.generated.resources.player_create_playlist_name_placeholder
import hachimiworld.composeapp.generated.resources.player_create_playlist_private_label
import hachimiworld.composeapp.generated.resources.playlist_description_placeholder
import hachimiworld.composeapp.generated.resources.playlist_edit_title
import org.jetbrains.compose.resources.stringResource
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
        title = { Text(text = stringResource(Res.string.playlist_edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = { Text(stringResource(Res.string.player_create_playlist_name_placeholder)) },
                    singleLine = true
                )
                TextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    placeholder = { Text(stringResource(Res.string.playlist_description_placeholder)) },
                    minLines = 3,
                    maxLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.player_create_playlist_private_label))
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
                Text(stringResource(Res.string.player_create_playlist_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelClick) {
                Text(stringResource(Res.string.common_cancel))
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