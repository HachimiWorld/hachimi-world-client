package world.hachimi.app.ui.auth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun CaptchaDialog(
    processing: Boolean,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text("请完成人机验证")
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                var showHelp by remember { mutableStateOf(false) }

                Text("请在打开的浏览器页面中完成人机验证。")

                Row(
                    modifier = Modifier.padding(top = 8.dp).clickable(
                        onClick = { showHelp = true },
                        interactionSource = null,
                        indication = null
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = "Info")
                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        style = TextStyle(fontSize = 12.sp),
                        text = "未弹出页面？"
                    )
                }

                AnimatedVisibility(showHelp) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = "Safari 浏览器请前往“设置”>“App”>“Safari 浏览器” 中关闭 “阻止弹出式窗口”，或使用 Chrome 浏览器访问；\n电脑端请检查是否被安全软件拦截",
                        style = TextStyle(fontSize = 12.sp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !processing) {
                Text("我已完成，继续")
            }
        }
    )
}


@Preview(widthDp = 1280)
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        CaptchaDialog(
            processing = false,
            onConfirm = {}
        )
    }
}