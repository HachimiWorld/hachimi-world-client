package world.hachimi.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.settings_auto_download_update
import hachimiworld.composeapp.generated.resources.settings_auto_download_update_wifi
import hachimiworld.composeapp.generated.resources.settings_changelog
import hachimiworld.composeapp.generated.resources.settings_check_update
import hachimiworld.composeapp.generated.resources.settings_check_update_action
import hachimiworld.composeapp.generated.resources.settings_check_update_checking
import hachimiworld.composeapp.generated.resources.settings_client_type
import hachimiworld.composeapp.generated.resources.settings_close_behavior
import hachimiworld.composeapp.generated.resources.settings_close_behavior_ask
import hachimiworld.composeapp.generated.resources.settings_close_behavior_exit
import hachimiworld.composeapp.generated.resources.settings_close_behavior_minimize_to_tray
import hachimiworld.composeapp.generated.resources.settings_dark_mode_follow_system
import hachimiworld.composeapp.generated.resources.settings_dark_mode_label
import hachimiworld.composeapp.generated.resources.settings_dark_mode_off
import hachimiworld.composeapp.generated.resources.settings_dark_mode_on
import hachimiworld.composeapp.generated.resources.settings_device_management
import hachimiworld.composeapp.generated.resources.settings_dropdown_cd
import hachimiworld.composeapp.generated.resources.settings_feedback
import hachimiworld.composeapp.generated.resources.settings_github_text
import hachimiworld.composeapp.generated.resources.settings_kids_mode
import hachimiworld.composeapp.generated.resources.settings_language_en
import hachimiworld.composeapp.generated.resources.settings_language_follow_system
import hachimiworld.composeapp.generated.resources.settings_language_label
import hachimiworld.composeapp.generated.resources.settings_language_zh
import hachimiworld.composeapp.generated.resources.settings_loudness
import hachimiworld.composeapp.generated.resources.settings_official_site_text
import hachimiworld.composeapp.generated.resources.settings_official_website
import hachimiworld.composeapp.generated.resources.settings_open_in_browser_cd
import hachimiworld.composeapp.generated.resources.settings_player_effects
import hachimiworld.composeapp.generated.resources.settings_title
import hachimiworld.composeapp.generated.resources.settings_update_downloading
import hachimiworld.composeapp.generated.resources.settings_update_install
import hachimiworld.composeapp.generated.resources.settings_update_retry
import hachimiworld.composeapp.generated.resources.settings_version_code
import hachimiworld.composeapp.generated.resources.settings_version_name
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import world.hachimi.app.BuildKonfig
import world.hachimi.app.getPlatform
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.Settings
import world.hachimi.app.update.UpdateManager
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.Select
import world.hachimi.app.ui.design.components.Switcher
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.fillMaxWidthIn
import world.hachimi.app.ui.util.listTailPadding
import world.hachimi.app.util.formatBytes

@Composable
fun SettingsScreen(
    globalStore: GlobalStore = koinInject<GlobalStore>()
) {
    val navigator = LocalNavigator.current
    ScreenScaffold(
        title = { Text(stringResource(Res.string.settings_title), maxLines = 1) },
        showBack = true,
        onBack = navigator::back,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AdaptiveScreenMargin)
                .listTailPadding()
                .fillMaxWidthIn()
                .testTag(world.hachimi.app.ui.TestTags.SETTINGS_SCREEN),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        Section(title = { Text("显示") }) {
            LanguageSetting(globalStore.settings)
            DarkModeSetting(globalStore)
            DiffusionBackgroundSetting(globalStore)
        }

        Section(title = { Text("播放") }) {
            LoudnessNormalizationSetting(globalStore)
            KidsModeSetting(globalStore)
        }

        if (getPlatform().name == "JVM") {
            Section(title = { Text("系统") }) {
                CloseBehaviorSetting(globalStore.settings)
            }
        }

        Section(title = { Text("账户") }) {
            DeviceManagementEntry()
        }

        Section(title = { Text("关于") }) {
            Info(globalStore)
        }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Section(title: @Composable () -> Unit, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleMedium) {
            title()
        }
        Card {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun LanguageSetting(settings: Settings) {
    PropertyItem(
        label = {
            Icon(Icons.Default.Language, stringResource(Res.string.settings_language_label))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.settings_language_label))
        },
        onClick = null,
    ) {
        Select(
            value = settings.locale,
            onValueChange = settings::updateLocale,
            options = listOf(null, "en", "zh"),
            expandIconContentDescription = stringResource(Res.string.settings_dropdown_cd),
            label = { locale ->
                Text(
                    when (locale) {
                        "en" -> stringResource(Res.string.settings_language_en)
                        "zh", "zh_CN", "zh-CN" -> stringResource(Res.string.settings_language_zh)
                        null -> stringResource(Res.string.settings_language_follow_system)
                        else -> locale ?: stringResource(Res.string.settings_language_follow_system)
                    }
                )
            },
        )
    }
}

@Composable
private fun DarkModeSetting(globalStore: GlobalStore) {
    PropertyItem(
        label = { Text(stringResource(Res.string.settings_dark_mode_label)) },
        onClick = null,
    ) {
        Select(
            value = globalStore.settings.darkMode,
            onValueChange = globalStore.settings::updateDarkMode,
            options = listOf(null, true, false),
            expandIconContentDescription = stringResource(Res.string.settings_dropdown_cd),
            label = { mode ->
                Text(
                    when (mode) {
                        true -> stringResource(Res.string.settings_dark_mode_on)
                        false -> stringResource(Res.string.settings_dark_mode_off)
                        null -> stringResource(Res.string.settings_dark_mode_follow_system)
                    }
                )
            },
        )
    }
}

@Composable
private fun DiffusionBackgroundSetting(globalStore: GlobalStore) {
    fun onClick() {
        globalStore.settings.updateDiffusionBackgroundEnabled(!globalStore.settings.enableDiffusionBackground)
    }
    PropertyItem(
        label = { Text(stringResource(Res.string.settings_player_effects)) },
        onClick = { onClick() }
    ) {
        Switcher(globalStore.settings.enableDiffusionBackground, { onClick() })
    }
}

@Composable
private fun LoudnessNormalizationSetting(globalStore: GlobalStore) {
    fun onClick() {
        globalStore.settings.updateLoudnessNormalization(!globalStore.settings.enableLoudnessNormalization)
    }
    PropertyItem(
        label = { Text(stringResource(Res.string.settings_loudness)) },
        onClick = { onClick() }
    ) {
        Switcher(globalStore.settings.enableLoudnessNormalization, {
            onClick()
        })
    }
}

@Composable
private fun KidsModeSetting(globalStore: GlobalStore) {
    fun onClick() {
        globalStore.settings.updateKidsMode(!globalStore.settings.kidsMode)
    }

    PropertyItem(
        label = { Text(stringResource(Res.string.settings_kids_mode)) },
        onClick = { onClick() }) {
        Switcher(globalStore.settings.kidsMode, {
            onClick()
        })
    }
}

@Composable
private fun UpdateItems(updates: UpdateManager) {
    when (val state = updates.downloadState) {
        is UpdateManager.DownloadState.Downloading -> PropertyItem(
            label = { Text(stringResource(Res.string.settings_check_update)) },
            onClick = null
        ) {
            val percent = state.progress?.let { "${(it * 100).toInt()}%" } ?: formatBytes(state.downloaded)
            Text(stringResource(Res.string.settings_update_downloading, percent))
        }

        is UpdateManager.DownloadState.Ready -> PropertyItem(
            label = { Text(stringResource(Res.string.settings_check_update)) },
            onClick = updates::install
        ) {
            TextButton(onClick = updates::install) {
                Text(stringResource(Res.string.settings_update_install, state.version.versionName))
            }
        }

        is UpdateManager.DownloadState.Failed -> PropertyItem(
            label = { Text(stringResource(Res.string.settings_check_update)) },
            onClick = updates::retryDownload
        ) {
            TextButton(onClick = updates::retryDownload) {
                Text(stringResource(Res.string.settings_update_retry))
            }
        }

        UpdateManager.DownloadState.Idle -> PropertyItem(
            label = { Text(stringResource(Res.string.settings_check_update)) },
            onClick = { updates.checkManually() }
        ) {
            TextButton(
                onClick = { updates.checkManually() },
                enabled = !updates.checking
            ) {
                if (updates.checking) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.settings_check_update_checking))
                } else {
                    Text(stringResource(Res.string.settings_check_update_action))
                }
            }
        }
    }

    if (updates.supportsInAppUpdate) {
        fun onClick() {
            updates.updateAutoDownload(!updates.autoDownload)
        }
        PropertyItem(
            label = {
                Text(
                    stringResource(
                        // Android waits for an unmetered network before downloading on its own
                        if (getPlatform().name == "Android") Res.string.settings_auto_download_update_wifi
                        else Res.string.settings_auto_download_update
                    )
                )
            },
            onClick = { onClick() }
        ) {
            Switcher(updates.autoDownload, { onClick() })
        }
    }
}

@Composable
private fun Info(globalStore: GlobalStore) {
    val navigator = LocalNavigator.current

    PropertyItem(
        label = { Text(stringResource(Res.string.settings_client_type)) },
        onClick = null
    ) {
        Text(getPlatform().name)
    }
    PropertyItem(
        label = { Text(stringResource(Res.string.settings_version_name)) },
        onClick = null
    ) {
        Text(BuildKonfig.VERSION_NAME)
    }
    PropertyItem(
        label = { Text(stringResource(Res.string.settings_version_code)) },
        onClick = null
    ) {
        Text(BuildKonfig.VERSION_CODE.toString())
    }
    UpdateItems(globalStore.updates)
    PropertyItem(
        label = { Text(stringResource(Res.string.settings_changelog)) },
        onClick = { navigator.push(Route.Root.Changelog) }
    ) {
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
    PropertyItem(
        label = { Text(stringResource(Res.string.settings_feedback)) },
        onClick = { getPlatform().openUrl("https://github.com/HachimiWorld/hachimi-world-client/discussions") }
    ) {
        LinkButton(
            stringResource(Res.string.settings_github_text),
            "https://github.com/HachimiWorld/hachimi-world-client/discussions"
        )
    }
    PropertyItem(
        label = { Text(stringResource(Res.string.settings_official_website)) },
        onClick = { getPlatform().openUrl("https://hachimi.world") }
    ) {
        LinkButton(stringResource(Res.string.settings_official_site_text), "https://hachimi.world")
    }
}

@Composable
private fun PropertyItem(
    label: @Composable () -> Unit,
    onClick: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp).then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelLarge) {
            label()
        }
        Spacer(Modifier.weight(1f))
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
            content()
        }
    }
}

@Composable
private fun LinkButton(
    text: String,
    url: String
) {
    TextButton(onClick = {
        getPlatform().openUrl(url)
    }) {
        Text(text)
        Spacer(Modifier.width(8.dp))
        Icon(
            modifier = Modifier.size(14.dp),
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = stringResource(Res.string.settings_open_in_browser_cd)
        )
    }
}

@Composable
private fun DeviceManagementEntry() {
    val navigator = LocalNavigator.current
    PropertyItem(
        label = { Text(stringResource(Res.string.settings_device_management)) },
        onClick = { navigator.push(Route.Root.DeviceManagement) }
    ) {
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

@Composable
private fun CloseBehaviorSetting(settings: Settings) {
    PropertyItem(
        label = { Text(stringResource(Res.string.settings_close_behavior)) },
        onClick = null,
    ) {
        Select(
            value = settings.closeBehavior,
            onValueChange = settings::updateCloseBehavior,
            options = Settings.CloseBehavior.entries,
            expandIconContentDescription = stringResource(Res.string.settings_dropdown_cd),
            label = { behavior ->
                Text(
                    when (behavior) {
                        Settings.CloseBehavior.ASK -> stringResource(Res.string.settings_close_behavior_ask)
                        Settings.CloseBehavior.MINIMIZE_TO_TRAY -> stringResource(Res.string.settings_close_behavior_minimize_to_tray)
                        Settings.CloseBehavior.EXIT -> stringResource(Res.string.settings_close_behavior_exit)
                    }
                )
            },
        )
    }
}
