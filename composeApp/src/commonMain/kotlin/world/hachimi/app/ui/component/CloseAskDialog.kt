package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.closeask_minimize
import hachimiworld.composeapp.generated.resources.closeask_quit
import hachimiworld.composeapp.generated.resources.closeask_remember
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
    rememberChoice: Boolean,
    onRememberChoiceChange: (Boolean) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(stringResource(Res.string.closeask_title))
        },
        text = {
            androidx.compose.foundation.layout.Column {
                Text(stringResource(Res.string.closeask_text))
                Spacer(Modifier.padding(top = 12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rememberChoice, onCheckedChange = onRememberChoiceChange)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(Res.string.closeask_remember))
                }
            }
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
        onQuitClick = {},
        rememberChoice = false,
        onRememberChoiceChange = {}
    )
}