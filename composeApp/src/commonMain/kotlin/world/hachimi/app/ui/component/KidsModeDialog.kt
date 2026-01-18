package world.hachimi.app.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.player_kids_mode_cancel
import hachimiworld.composeapp.generated.resources.player_kids_mode_message
import hachimiworld.composeapp.generated.resources.player_kids_mode_play
import hachimiworld.composeapp.generated.resources.player_kids_mode_title
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun KidsModeDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(stringResource(Res.string.player_kids_mode_title))
        },
        text = {
            Text(stringResource(Res.string.player_kids_mode_message))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.player_kids_mode_play))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.player_kids_mode_cancel))
            }
        }
    )
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        KidsModeDialog(onDismissRequest = {}, onConfirm = {})
    }
}