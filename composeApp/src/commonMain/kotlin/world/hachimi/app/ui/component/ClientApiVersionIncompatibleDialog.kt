package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text

@Composable
fun ClientApiVersionIncompatibleDialog(global: GlobalStore) {
    if (global.showApiVersionIncompatible) {
        AlertDialog(
            modifier = Modifier.width(280.dp),
            title = {
                Text("客户端版本过低")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("您的客户端已低于服务器支持的最低版本，请更新客户端至最新版本，否则将无法使用！")
                    HorizontalDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Client API Ver: ${global.clientApiVersion}", style = MaterialTheme.typography.bodySmall)
                        Text("Server API Ver: ${global.serverVersion}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Server Min API Ver: ${global.serverMinVersion}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            icon = {
                Icon(Icons.Default.Warning, "Warning")
            },
            onDismissRequest = {
                // Do nothing
            },
            confirmButton = {
                // No confirm button
            },
        )
    }
}
