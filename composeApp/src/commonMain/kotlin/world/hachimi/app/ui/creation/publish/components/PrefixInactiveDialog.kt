package world.hachimi.app.ui.creation.publish.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun PrefixInactiveDialog(
    onExit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text("首次审核中")
        },
        text = {
            Text("您首次发布的作品正在审核中，请等待首个作品审核完毕后再发布其他作品")
        },
        confirmButton = {
            TextButton(onClick = onExit) {
                Text("返回")
            }
        }
    )
}