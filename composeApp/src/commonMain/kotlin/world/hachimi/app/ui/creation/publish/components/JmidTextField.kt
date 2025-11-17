package world.hachimi.app.ui.creation.publish.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
        leadingIcon = {
            Text(
                modifier = Modifier.padding(start = 24.dp),
                text = "JM - $jmidPrefix - "
            )
        },
        trailingIcon = {
            when(valid) {
                true -> Icon(Icons.Default.CheckCircle, contentDescription = "Available", tint = MaterialTheme.colorScheme.primary)
                false -> Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                null -> if (jmidNumber != null) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 1.dp)
            }
        },
        supportingText = {
            Text(supportText ?: "")
        }
    )
}