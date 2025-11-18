package world.hachimi.app.ui.player.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext

@Composable
fun ShareDialog(
    onDismissRequest: () -> Unit,
    jmid: String,
    title: String,
    author: String
) {
    val ctx = LocalPlatformContext.current
    val text = remember(jmid) {
        "分享哈基米音乐《$title》by $author - 基米天堂（复制到浏览器访问）\n" +
                "https://hachimi.world/song/${jmid.lowercase()}"
    }
    var copied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("分享音乐")
        },
        text = {
            Column {
                SelectionContainer {
                    Text(text)
                }

                if (copied) Text("已复制到剪切板")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                share(ctx, text)
                copied = true
            }) {
                Text("分享")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("关闭")
            }
        }
    )
}

expect fun share(context: PlatformContext, text: String): Int