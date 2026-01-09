package world.hachimi.app.ui.player.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.theme.PreviewTheme

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

                if (copied) Text(
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.End),
                    text = "已复制到剪切板",
                    fontSize = 12.sp
                )
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

@Composable
@Preview
private fun Preview() {
    PreviewTheme(background = false) {
        ShareDialog(
            onDismissRequest = {},
            jmid = "JM-ABCD-001",
            title = "Test Song",
            author = "Test Author"
        )
    }
}