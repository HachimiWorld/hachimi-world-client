package world.hachimi.app.ui.userspace.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.getPlatform
import world.hachimi.app.model.UserSpaceViewModel
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun Connections(
    accounts: List<UserModule.ConnectedAccountItem>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        accounts.fastForEach {
            ConnectionChip(
                name = it.name,
                type = it.type,
                onOpen = { openUserSpaceConnectionUrl(it.type, it.id) }
            )
        }
    }
}

@Composable
fun Connections(
    vm: UserSpaceViewModel,
    modifier: Modifier = Modifier
) {
    val profile = vm.profile ?: return
    Connections(accounts = profile.connectedAccounts, modifier = modifier)
}

fun openUserSpaceConnectionUrl(type: String, id: String) {
    when (type) {
        UserModule.CONNECTION_TYPE_BILIBILI -> getPlatform().openUrl("https://space.bilibili.com/$id")
        else -> {}
    }
}

@Composable
@Preview
private fun Preview() {
    PreviewTheme(background = true) {
        Connections(accounts = listOf(
            UserModule.ConnectedAccountItem(type = "bilibili", id = "123", name = "B站"),
        ))
    }
}
