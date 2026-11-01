package world.hachimi.app.ui.creation.publish.components

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_available_cd
import hachimiworld.composeapp.generated.resources.common_close
import hachimiworld.composeapp.generated.resources.common_confirm
import hachimiworld.composeapp.generated.resources.common_error_cd
import hachimiworld.composeapp.generated.resources.publish_init_jmid_desc_exclusive
import hachimiworld.composeapp.generated.resources.publish_init_jmid_desc_format
import hachimiworld.composeapp.generated.resources.publish_init_jmid_prefix_placeholder
import hachimiworld.composeapp.generated.resources.publish_init_jmid_title
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
import org.jetbrains.compose.resources.stringResource
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
                Text(stringResource(Res.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onDismissRequest) {
                Text(stringResource(Res.string.common_close))
            }
        },
        title = {
            Text(stringResource(Res.string.publish_init_jmid_title))
        },
        text = {
            Column {
                Text(stringResource(Res.string.publish_init_jmid_desc_format))
                Text(stringResource(Res.string.publish_init_jmid_desc_exclusive))
                Spacer(Modifier.height(24.dp))
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(stringResource(Res.string.publish_init_jmid_prefix_placeholder)) },
                    trailingIcon = {
                        when(valid) {
                            true -> Icon(Icons.Default.CheckCircle, contentDescription = stringResource(Res.string.common_available_cd), tint = MaterialTheme.colorScheme.primary)
                            false -> Icon(Icons.Default.Error, contentDescription = stringResource(Res.string.common_error_cd), tint = MaterialTheme.colorScheme.error)
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