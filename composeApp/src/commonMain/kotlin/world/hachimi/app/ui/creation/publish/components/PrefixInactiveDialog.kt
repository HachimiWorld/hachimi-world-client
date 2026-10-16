package world.hachimi.app.ui.creation.publish.components

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_back
import hachimiworld.composeapp.generated.resources.publish_prefix_inactive_text
import hachimiworld.composeapp.generated.resources.publish_prefix_inactive_title
import org.jetbrains.compose.resources.stringResource
import androidx.compose.runtime.Composable
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton

@Composable
fun PrefixInactiveDialog(
    onExit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(stringResource(Res.string.publish_prefix_inactive_title))
        },
        text = {
            Text(stringResource(Res.string.publish_prefix_inactive_text))
        },
        confirmButton = {
            TextButton(onClick = onExit) {
                Text(stringResource(Res.string.common_back))
            }
        }
    )
}