package world.hachimi.app.ui.auth.components

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.auth_hide_password_cd
import hachimiworld.composeapp.generated.resources.auth_show_password_cd
import org.jetbrains.compose.resources.stringResource
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
        if (showPassword) Icon(Icons.Default.Visibility, stringResource(Res.string.auth_hide_password_cd))
        else Icon(Icons.Default.VisibilityOff, stringResource(Res.string.auth_show_password_cd))
    }
}
