package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.update_changelog_title
import hachimiworld.composeapp.generated.resources.update_confirm
import hachimiworld.composeapp.generated.resources.update_current_version
import hachimiworld.composeapp.generated.resources.update_found_title
import hachimiworld.composeapp.generated.resources.update_icon_cd
import hachimiworld.composeapp.generated.resources.update_ignore
import hachimiworld.composeapp.generated.resources.update_new_version
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
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
            Text(stringResource(Res.string.update_found_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(Res.string.update_current_version, currentVersion))
                Text(stringResource(Res.string.update_new_version, newVersion))
                HorizontalDivider()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(Res.string.update_changelog_title))
                    Text(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        text = changelog, style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        icon = {
            Icon(Icons.Default.Upgrade, stringResource(Res.string.update_icon_cd))
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.update_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.update_ignore))
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
            changelog = remember { "Test changelog " },
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview
@Composable
private fun PreviewLongChangeLog() {
    PreviewTheme(background = false) {
        UpgradeDialog(
            currentVersion = "1.0.0",
            newVersion = "1.1.0",
            changelog = remember { "Test changelog ".repeat(100) },
            onDismiss = {},
            onConfirm = {}
        )
    }
}