package world.hachimi.app.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.update_download
import hachimiworld.composeapp.generated.resources.update_install
import hachimiworld.composeapp.generated.resources.update_later
import hachimiworld.composeapp.generated.resources.update_metered_hint
import hachimiworld.composeapp.generated.resources.update_package_size
import hachimiworld.composeapp.generated.resources.update_ready_title
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.BuildKonfig
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.update.UpdateManager
import world.hachimi.app.util.formatBytes

@Composable
fun UpgradeDialog(global: GlobalStore) {
    val updates = global.updates
    val prompt = updates.prompt ?: return
    val latest = updates.latestVersion ?: return
    val changelogs = updates.newerVersions.map { it.versionName to it.changelog }

    when (prompt) {
        UpdateManager.Prompt.AVAILABLE -> if (updates.supportsInAppUpdate) {
            val metered = remember(latest) { updates.isMeteredNetwork() }
            val notes = listOfNotNull(
                if (metered) stringResource(Res.string.update_metered_hint) else null,
                latest.size?.let { stringResource(Res.string.update_package_size, formatBytes(it)) },
            )
            UpgradeDialog(
                currentVersion = BuildKonfig.VERSION_NAME,
                newVersion = latest.versionName,
                changelogs = changelogs,
                onDismiss = updates::dismissPrompt,
                onConfirm = updates::confirmPrompt,
                confirmText = stringResource(Res.string.update_download),
                notes = notes,
            )
        } else {
            UpgradeDialog(
                currentVersion = BuildKonfig.VERSION_NAME,
                newVersion = latest.versionName,
                changelogs = changelogs,
                onDismiss = updates::dismissPrompt,
                onConfirm = updates::confirmPrompt,
            )
        }

        UpdateManager.Prompt.READY -> UpgradeDialog(
            currentVersion = BuildKonfig.VERSION_NAME,
            newVersion = latest.versionName,
            changelogs = changelogs,
            onDismiss = updates::dismissPrompt,
            onConfirm = updates::confirmPrompt,
            title = stringResource(Res.string.update_ready_title),
            confirmText = stringResource(Res.string.update_install),
            dismissText = stringResource(Res.string.update_later),
        )
    }
}
