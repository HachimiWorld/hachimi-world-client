package world.hachimi.app.ui.component

import androidx.compose.runtime.Composable
import world.hachimi.app.model.GlobalStore

@Composable
fun UpgradeDialog(global: GlobalStore) {
    if (global.showUpdateDialog) UpgradeDialog(
        currentVersion = global.currentVersion,
        newVersion = global.newVersionInfo!!.versionName,
        changelogs = global.updateVersions.map { it.versionName to it.changelog },
        onDismiss = {
            global.dismissUpgrade()
        },
        onConfirm = {
            global.confirmUpgrade()
        }
    )
}