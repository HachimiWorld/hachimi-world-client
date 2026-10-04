package world.hachimi.app.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.update_current_version
import hachimiworld.composeapp.generated.resources.update_download
import hachimiworld.composeapp.generated.resources.update_install
import hachimiworld.composeapp.generated.resources.update_install_restart
import hachimiworld.composeapp.generated.resources.update_installs_on_exit
import hachimiworld.composeapp.generated.resources.update_later
import hachimiworld.composeapp.generated.resources.update_metered_hint
import hachimiworld.composeapp.generated.resources.update_notice_available
import hachimiworld.composeapp.generated.resources.update_notice_failed
import hachimiworld.composeapp.generated.resources.update_notice_open
import hachimiworld.composeapp.generated.resources.update_notice_ready
import hachimiworld.composeapp.generated.resources.update_open_website
import hachimiworld.composeapp.generated.resources.update_retry
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
    val current = stringResource(Res.string.update_current_version, BuildKonfig.VERSION_NAME)

    when (prompt) {
        UpdateManager.Prompt.AVAILABLE -> {
            val inApp = updates.supportsInAppUpdate
            val metered = remember(latest) { inApp && updates.isMeteredNetwork() }
            val size = latest.size?.takeIf { inApp }?.let { " · ${formatBytes(it)}" } ?: ""
            UpdateDialog(
                title = stringResource(Res.string.update_notice_available, latest.versionName),
                subtitle = listOfNotNull(
                    current + size,
                    if (metered) stringResource(Res.string.update_metered_hint) else null,
                ),
                versions = updates.newerVersions,
                confirmText = stringResource(if (inApp) Res.string.update_download else Res.string.update_notice_open),
                onConfirm = updates::confirmPrompt,
                dismissText = stringResource(Res.string.update_later),
                onDismiss = updates::dismissPrompt,
            )
        }

        UpdateManager.Prompt.READY -> UpdateDialog(
            title = stringResource(Res.string.update_notice_ready, latest.versionName),
            // Where installing restarts the app, putting it off still installs it on quit
            subtitle = listOfNotNull(
                current,
                if (updates.installsOnExit) stringResource(Res.string.update_installs_on_exit) else null,
            ),
            versions = updates.newerVersions,
            confirmText = stringResource(
                if (updates.installsOnExit) Res.string.update_install_restart else Res.string.update_install
            ),
            onConfirm = updates::confirmPrompt,
            dismissText = stringResource(Res.string.update_later),
            onDismiss = updates::dismissPrompt,
        )

        UpdateManager.Prompt.FAILED -> UpdateDialog(
            title = stringResource(Res.string.update_notice_failed, latest.versionName),
            subtitle = listOf(current),
            versions = updates.newerVersions,
            confirmText = stringResource(Res.string.update_retry),
            onConfirm = updates::confirmPrompt,
            dismissText = stringResource(Res.string.update_open_website),
            onDismiss = updates::openDownloadPage,
            onDismissRequest = updates::dismissPrompt,
        )
    }
}
