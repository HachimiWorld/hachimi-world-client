package world.hachimi.app.ui.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.player_create_playlist_cancel
import hachimiworld.composeapp.generated.resources.player_create_playlist_create
import hachimiworld.composeapp.generated.resources.player_create_playlist_description_placeholder
import hachimiworld.composeapp.generated.resources.player_create_playlist_name_placeholder
import hachimiworld.composeapp.generated.resources.player_create_playlist_private_label
import hachimiworld.composeapp.generated.resources.player_create_playlist_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.PlaylistViewModel
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.singleLined

@Composable
fun CreatePlaylistDialog(vm: PlaylistViewModel = koinViewModel()) {
    if (vm.showCreatePlaylistDialog) CreatePlaylistDialog(
        name = vm.createPlaylistName,
        onNameChange = { vm.createPlaylistName = it.singleLined() },
        description = vm.createPlaylistDescription,
        onDescriptionChange = { vm.createPlaylistDescription = it },
        private = vm.createPlaylistPrivate,
        onPrivateChange = { vm.createPlaylistPrivate = it },
        onConfirm = { vm.confirmCreatePlaylist() },
        onDismiss = { vm.cancelCreatePlaylist() },
        processing = vm.createPlaylistOperating
    )
}

@Composable
private fun CreatePlaylistDialog(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    private: Boolean,
    onPrivateChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    processing: Boolean
) {
    AlertDialog(
        title = { Text(stringResource(Res.string.player_create_playlist_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    modifier = Modifier.width(280.dp),
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = { Text(stringResource(Res.string.player_create_playlist_name_placeholder)) },
                    singleLine = true
                )
                TextField(
                    modifier = Modifier.width(280.dp),
                    value = description,
                    onValueChange = onDescriptionChange,
                    placeholder = { Text(stringResource(Res.string.player_create_playlist_description_placeholder)) },
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
                onClick = onConfirm,
                enabled = name.isNotBlank() && !processing
            ) {
                Text(stringResource(Res.string.player_create_playlist_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.player_create_playlist_cancel))
            }
        },
        onDismissRequest = onDismiss
    )
}

@Composable
@Preview
private fun Preview() {
    PreviewTheme(background = false) {
        CreatePlaylistDialog(
            name = "",
            onNameChange = { },
            description = "",
            onDescriptionChange = { },
            private = false,
            onPrivateChange = { },
            onConfirm = { },
            onDismiss = { },
            processing = false
        )
    }
}