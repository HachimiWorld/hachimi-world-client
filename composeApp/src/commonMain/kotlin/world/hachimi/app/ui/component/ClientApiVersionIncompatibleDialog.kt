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
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.player_client_api_version_client_label
import hachimiworld.composeapp.generated.resources.player_client_api_version_message
import hachimiworld.composeapp.generated.resources.player_client_api_version_server_label
import hachimiworld.composeapp.generated.resources.player_client_api_version_server_min_label
import hachimiworld.composeapp.generated.resources.player_client_api_version_title
import org.jetbrains.compose.resources.stringResource
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
                Text(stringResource(Res.string.player_client_api_version_title))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(Res.string.player_client_api_version_message))
                    HorizontalDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${stringResource(Res.string.player_client_api_version_client_label)} ${global.clientApiVersion}", style = MaterialTheme.typography.bodySmall)
                        Text("${stringResource(Res.string.player_client_api_version_server_label)} ${global.serverVersion}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${stringResource(Res.string.player_client_api_version_server_min_label)} ${global.serverMinVersion}",
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
