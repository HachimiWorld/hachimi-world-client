package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun UpgradeDialog(
    currentVersion: String,
    newVersion: String,
    changelog: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.width(280.dp),
        title = {
            Text("发现新版本")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("当前版本: ${currentVersion}")
                Text("新版本: ${newVersion}")
                HorizontalDivider()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("更新日志")
                    Text(changelog, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        icon = {
            Icon(Icons.Default.Upgrade, "Upgrade")
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("立即升级")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("忽略")
            }
        }
    )
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        UpgradeDialog(
            currentVersion = "1.0.0",
            newVersion = "1.1.0",
            changelog = "Test changelog ".repeat(20),
            onDismiss = {},
            onConfirm = {}
        )
    }
}