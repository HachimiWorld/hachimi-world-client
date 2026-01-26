package world.hachimi.app.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.closeask_minimize
import hachimiworld.composeapp.generated.resources.closeask_quit
import hachimiworld.composeapp.generated.resources.closeask_text
import hachimiworld.composeapp.generated.resources.closeask_title
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.SubtleButton
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton

@Composable
fun CloseAskDialog(
    onCancel: () -> Unit,
    onMinimizeClick: () -> Unit,
    onQuitClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(stringResource(Res.string.closeask_title))
        },
        text = {
            Text(stringResource(Res.string.closeask_text))
        },
        confirmButton = {
            TextButton(onClick = onMinimizeClick) {
                Text(stringResource(Res.string.closeask_minimize))
            }
            SubtleButton(onClick = onQuitClick) {
                Text(stringResource(Res.string.closeask_quit))
            }
        }
    )
}

@Preview
@Composable
private fun PreviewCloseAskDialog() {
    CloseAskDialog(
        onCancel = {},
        onMinimizeClick = {},
        onQuitClick = {}
    )
}