package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
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
import hachimiworld.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.theme.PreviewTheme

/**
 * @param changelogs list of (versionName, changelog) pairs, ordered from latest to oldest
 */
@Composable
fun UpgradeDialog(
    currentVersion: String,
    newVersion: String,
    changelogs: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.width(320.dp),
        title = {
            Text(stringResource(Res.string.update_found_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.update_current_version, currentVersion))
                Text(stringResource(Res.string.update_new_version, newVersion))
                HorizontalDivider()
                Text(
                    stringResource(Res.string.update_changelog_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Column(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    changelogs.forEachIndexed { index, (version, changelog) ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                version,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(changelog, style = MaterialTheme.typography.bodySmall)
                        }
                        if (index < changelogs.lastIndex) {
                            HorizontalDivider()
                        }
                    }
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
            newVersion = "1.2.0",
            changelogs = remember {
                listOf(
                    "1.2.0" to "- Added new feature\n- Fixed bug",
                    "1.1.0" to "- Initial improvements"
                )
            },
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
            newVersion = "1.5.0",
            changelogs = remember {
                (5 downTo 1).map { "1.$it.0" to "Test changelog ".repeat(20) }
            },
            onDismiss = {},
            onConfirm = {}
        )
    }
}