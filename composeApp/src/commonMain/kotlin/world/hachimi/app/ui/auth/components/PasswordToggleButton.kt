package world.hachimi.app.ui.auth.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.components.Icon

@Composable
fun PasswordToggleButton(showPassword: Boolean, onValueChange: (Boolean) -> Unit) {
    IconButton(
        modifier = Modifier.size(28.dp),
        onClick = { onValueChange(!showPassword) }
    ) {
        if (showPassword) Icon(Icons.Default.Visibility, "Password Visibility On")
        else Icon(Icons.Default.VisibilityOff, "Password Visibility Off")
    }
}
