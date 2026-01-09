package world.hachimi.app.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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
            Text("要最小化吗？")
        },
        text = {
            Text("最小化后可从系统托盘唤出窗口")
        },
        confirmButton = {
            TextButton(onClick = onMinimizeClick) {
                Text("最小化")
            }
            SubtleButton(onClick = onQuitClick) {
                Text("直接退出")
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