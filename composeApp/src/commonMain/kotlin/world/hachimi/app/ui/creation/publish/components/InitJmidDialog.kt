package world.hachimi.app.ui.creation.publish.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.theme.PreviewTheme

/**
 * @param valid `null` means loading
 */
@Composable
fun InitJmidDialog(
    onDismissRequest: () -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    valid: Boolean?,
    supportText: String?,
    onConfirm: () -> Unit = {}
) {
    AlertDialog(
        modifier = Modifier.widthIn(max = 320.dp),
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = onConfirm, enabled = valid == true) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onDismissRequest) {
                Text("关闭")
            }
        },
        title = {
            Text("首次设置基米 ID 前缀")
        },
        text = {
            Column {
                Text("基米 ID 是作品的唯一标识，格式为 JM-ABCD-001")
                Text("前缀由每位创作者独占，一旦设定则不可更改")
                Spacer(Modifier.height(24.dp))
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text("前缀")},
                    trailingIcon = {
                        when(valid) {
                            true -> Icon(Icons.Default.CheckCircle, contentDescription = "Available", tint = MaterialTheme.colorScheme.primary)
                            false -> Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                            null -> if (value.isNotBlank()) { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 1.dp) }
                        }
                    },
                    supportingText = { Text(supportText ?: "") },
                    singleLine = true
                )
            }
        }
    )
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        InitJmidDialog(onDismissRequest = {}, "", {}, true, "support text",onConfirm = {})
    }
}