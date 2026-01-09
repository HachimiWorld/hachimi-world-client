package world.hachimi.app.ui.creation.publish.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun JmidTextField(
    jmidNumber: String?,
    jmidPrefix: String,
    valid: Boolean?,
    supportText: String?,
    onNumberChange: (String) -> Unit,
) {
    TextField(
        modifier = Modifier.width(300.dp),
        value = jmidNumber ?: "",
        onValueChange = onNumberChange,
        singleLine = true,
        prefix = {
            Text(text = "JM - $jmidPrefix - ")
        },
        trailingIcon = {
            when(valid) {
                true -> Icon(Icons.Default.CheckCircle, contentDescription = "Available", tint = HachimiTheme.colorScheme.secondary)
                false -> Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                null -> if (jmidNumber != null) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 1.dp)
            }
        },
        supportingText = {
            Text(supportText ?: "", maxLines = 1)
        }
    )
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        JmidTextField(
            "",
            "ABCD",
            true,
            null,
            onNumberChange = {}
        )
    }
}