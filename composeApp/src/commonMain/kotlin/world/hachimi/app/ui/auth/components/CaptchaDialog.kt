package world.hachimi.app.ui.auth.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CaptchaDialog(onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text("请完成人机验证")
        },
        text = {
            Text("请在打开的浏览器页面中完成人机验证。\n若未正确打开，请检查浏览器安全设置中是否启用了阻止弹出式窗口选项。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("我已完成，继续")
            }
        }
    )
}


@Preview
@Composable
private fun Preview() {
    CaptchaDialog(onConfirm = {
    })
}