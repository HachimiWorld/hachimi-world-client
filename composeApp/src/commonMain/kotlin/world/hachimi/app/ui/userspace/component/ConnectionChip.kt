package world.hachimi.app.ui.userspace.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.user_connections_open_bilibili
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.ui.component.AmbientChip
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.util.PlatformIcons

@Composable
fun ConnectionChip(
    name: String,
    type: String,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    AmbientChip(modifier = modifier, onClick = onOpen) {
        PlatformIcon(type)

        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = name,
            style = MaterialTheme.typography.labelMedium
        )

        Icon(
            modifier = Modifier.padding(start = 4.dp).size(14.dp),
            imageVector = Icons.Filled.ArrowOutward,
            contentDescription = stringResource(Res.string.user_connections_open_bilibili),
        )
    }
}

@Composable
private fun PlatformIcon(type: String) {
    when (type) {
        UserModule.CONNECTION_TYPE_BILIBILI -> Icon(
            painter = painterResource(PlatformIcons.bilibili),
            contentDescription = type,
            tint = Color(0xFFFF6699),
            modifier = Modifier.size(16.dp)
        )
        else -> Icon(Icons.Default.LinkOff, contentDescription = type)
    }
}

@Composable
@Stable
private fun platformLabel(type: String): String {
    return when (type) {
        UserModule.CONNECTION_TYPE_BILIBILI -> "哔哩哔哩"
        else -> type
    }
}

@Preview
@Composable
private fun Preview() {
    ConnectionChip(
        name = "bilibili_user",
        type = UserModule.CONNECTION_TYPE_BILIBILI,
        onOpen = {}
    )
}