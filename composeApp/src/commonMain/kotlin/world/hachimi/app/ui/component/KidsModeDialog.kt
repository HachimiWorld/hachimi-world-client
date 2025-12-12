package world.hachimi.app.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun KidsModeDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("你已启用宝宝模式")
        },
        text = {
            Text("本作品被作者标记为含有儿童不宜内容，由于你已启用宝宝模式，请确认是否继续播放。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("播放")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("取消")
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