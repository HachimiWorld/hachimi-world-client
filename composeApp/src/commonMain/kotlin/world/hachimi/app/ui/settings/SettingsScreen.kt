package world.hachimi.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import world.hachimi.app.BuildKonfig
import world.hachimi.app.getPlatform
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.Settings
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.design.components.DropdownMenu
import world.hachimi.app.ui.design.components.DropdownMenuItem
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.util.fillMaxWidthIn

@Composable
fun SettingsScreen(
    globalStore: GlobalStore = koinInject<GlobalStore>()
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
            .navigationBarsPadding()
            .padding(LocalContentInsets.current.asPaddingValues())
            .fillMaxWidthIn(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(stringResource(Res.string.settings_title), style = MaterialTheme.typography.titleLarge)

        LanguageSetting(globalStore.settings)
        DarkModeSetting(globalStore)
        DiffusionBackgroundSetting(globalStore)
        LoudnessNormalizationSetting(globalStore)
        KidsModeSetting(globalStore)

        if (getPlatform().name == "JVM") {
            CloseBehaviorSetting(globalStore.settings)
        }

        Info()
    }
}

@Composable
private fun LanguageSetting(settings: Settings) {
    PropertyItem(label = {
        Icon(Icons.Default.Language, stringResource(Res.string.settings_language_label))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(Res.string.settings_language_label)) }
    ) {
        var expandedLang by remember { mutableStateOf(false) }
        Box {
            TextButton(onClick = { expandedLang = true }) {
                Text(
                    when (settings.locale) {
                        "en" -> stringResource(Res.string.settings_language_en)
                        "zh", "zh_CN", "zh-CN" -> stringResource(Res.string.settings_language_zh)
                        null -> stringResource(Res.string.settings_language_follow_system)
                        else -> settings.locale
                            ?: stringResource(Res.string.settings_language_follow_system)
                    }
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(Res.string.settings_dropdown_cd)
                )
            }
            DropdownMenu(expandedLang, onDismissRequest = { expandedLang = false }) {
                DropdownMenuItem(onClick = {
                    settings.updateLocale(null)
                    expandedLang = false
                }, text = { Text(stringResource(Res.string.settings_language_follow_system)) })
                DropdownMenuItem(onClick = {
                    settings.updateLocale("en")
                    expandedLang = false
                }, text = { Text(stringResource(Res.string.settings_language_en)) })
                DropdownMenuItem(onClick = {
                    settings.updateLocale("zh")
                    expandedLang = false
                }, text = { Text(stringResource(Res.string.settings_language_zh)) })
            }
        }
    }
}

@Composable
private fun DarkModeSetting(globalStore: GlobalStore) {
    PropertyItem(label = {
        Text(
            stringResource(Res.string.settings_dark_mode_label),
            style = MaterialTheme.typography.bodyLarge
        )
    }) {
        var expanded by remember { mutableStateOf(false) }
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(
                    when (globalStore.settings.darkMode) {
                        true -> stringResource(Res.string.settings_dark_mode_on)
                        false -> stringResource(Res.string.settings_dark_mode_off)
                        null -> stringResource(Res.string.settings_dark_mode_follow_system)
                    }
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(Res.string.settings_dropdown_cd)
                )
            }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(onClick = {
                    globalStore.settings.updateDarkMode(null)
                    expanded = false
                }, text = {
                    Text(stringResource(Res.string.settings_dark_mode_follow_system))
                })
                DropdownMenuItem(onClick = {
                    globalStore.settings.updateDarkMode(true)
                    expanded = false
                }, text = {
                    Text(stringResource(Res.string.settings_dark_mode_on))
                })
                DropdownMenuItem(onClick = {
                    globalStore.settings.updateDarkMode(false)
                    expanded = false
                }, text = {
                    Text(stringResource(Res.string.settings_dark_mode_off))
                })
            }
        }
    }
}

@Composable
private fun DiffusionBackgroundSetting(globalStore: GlobalStore) {
    PropertyItem(label = { Text(stringResource(Res.string.settings_player_effects)) }) {
        Switch(globalStore.settings.enableDiffusionBackground, {
            globalStore.settings.updateDiffusionBackgroundEnabled(it)
        })
    }
}

@Composable
private fun LoudnessNormalizationSetting(globalStore: GlobalStore) {
    PropertyItem(label = { Text(stringResource(Res.string.settings_loudness)) }) {
        Switch(globalStore.settings.enableLoudnessNormalization, {
            globalStore.settings.updateLoudnessNormalization(it)
        })
    }
}

@Composable
private fun KidsModeSetting(globalStore: GlobalStore) {
    PropertyItem(label = { Text(stringResource(Res.string.settings_kids_mode)) }) {
        Switch(globalStore.settings.kidsMode, {
            globalStore.settings.updateKidsMode(it)
        })
    }
}

@Composable
private fun Info() {
    PropertyItem(label = { Text(stringResource(Res.string.settings_client_type)) }) {
        Text(getPlatform().name)
    }
    PropertyItem(label = { Text(stringResource(Res.string.settings_version_name)) }) {
        Text(BuildKonfig.VERSION_NAME)
    }
    PropertyItem(label = { Text(stringResource(Res.string.settings_version_code)) }) {
        Text(BuildKonfig.VERSION_CODE.toString())
    }
    PropertyItem(label = { Text(stringResource(Res.string.settings_feedback)) }) {
        LinkButton(
            stringResource(Res.string.settings_github_text),
            "https://github.com/HachimiWorld/hachimi-world-client/discussions"
        )
    }
    PropertyItem(label = { Text(stringResource(Res.string.settings_official_website)) }) {
        LinkButton(stringResource(Res.string.settings_official_site_text), "https://hachimi.world")
    }
}

@Composable
private fun PropertyItem(
    label: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        label()
        Spacer(Modifier.weight(1f))
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
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
private fun CloseBehaviorSetting(settings: Settings) {
    PropertyItem(label = { Text(stringResource(Res.string.settings_close_behavior)) }) {
        var expanded by remember { mutableStateOf(false) }
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(
                    when (settings.closeBehavior) {
                        Settings.CloseBehavior.ASK -> stringResource(Res.string.settings_close_behavior_ask)
                        Settings.CloseBehavior.MINIMIZE_TO_TRAY -> stringResource(Res.string.settings_close_behavior_minimize_to_tray)
                        Settings.CloseBehavior.EXIT -> stringResource(Res.string.settings_close_behavior_exit)
                    }
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = stringResource(Res.string.settings_dropdown_cd))
            }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(onClick = {
                    settings.updateCloseBehavior(Settings.CloseBehavior.ASK)
                    expanded = false
                }, text = { Text(stringResource(Res.string.settings_close_behavior_ask)) })
                DropdownMenuItem(onClick = {
                    settings.updateCloseBehavior(Settings.CloseBehavior.MINIMIZE_TO_TRAY)
                    expanded = false
                }, text = { Text(stringResource(Res.string.settings_close_behavior_minimize_to_tray)) })
                DropdownMenuItem(onClick = {
                    settings.updateCloseBehavior(Settings.CloseBehavior.EXIT)
                    expanded = false
                }, text = { Text(stringResource(Res.string.settings_close_behavior_exit)) })
            }
        }
    }
}
