package world.hachimi.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.settings_client_type
import hachimiworld.composeapp.generated.resources.settings_dark_mode_follow_system
import hachimiworld.composeapp.generated.resources.settings_dark_mode_label
import hachimiworld.composeapp.generated.resources.settings_dark_mode_off
import hachimiworld.composeapp.generated.resources.settings_dark_mode_on
import hachimiworld.composeapp.generated.resources.settings_dropdown_cd
import hachimiworld.composeapp.generated.resources.settings_feedback
import hachimiworld.composeapp.generated.resources.settings_github_text
import hachimiworld.composeapp.generated.resources.settings_kids_mode
import hachimiworld.composeapp.generated.resources.settings_loudness
import hachimiworld.composeapp.generated.resources.settings_official_site_text
import hachimiworld.composeapp.generated.resources.settings_official_website
import hachimiworld.composeapp.generated.resources.settings_open_in_browser_cd
import hachimiworld.composeapp.generated.resources.settings_title
import hachimiworld.composeapp.generated.resources.settings_version_code
import hachimiworld.composeapp.generated.resources.settings_version_name
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import world.hachimi.app.BuildKonfig
import world.hachimi.app.getPlatform
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.LocalContentInsets

@Composable
fun SettingsScreen() {
    val globalStore = koinInject<GlobalStore>()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
            .navigationBarsPadding()
            .padding(LocalContentInsets.current.asPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(stringResource(Res.string.settings_title), style = MaterialTheme.typography.titleLarge)

        PropertyItem(label = {
            Text(stringResource(Res.string.settings_dark_mode_label), style = MaterialTheme.typography.bodyLarge)
        }) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(
                        when (globalStore.darkMode) {
                            true -> stringResource(Res.string.settings_dark_mode_on)
                            false -> stringResource(Res.string.settings_dark_mode_off)
                            null -> stringResource(Res.string.settings_dark_mode_follow_system)
                        }
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = stringResource(Res.string.settings_dropdown_cd))
                }
                DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = {
                        globalStore.updateDarkMode(null)
                        expanded = false
                    }, text = {
                        Text(stringResource(Res.string.settings_dark_mode_follow_system))
                    })
                    DropdownMenuItem(onClick = {
                        globalStore.updateDarkMode(true)
                        expanded = false
                    }, text = {
                        Text(stringResource(Res.string.settings_dark_mode_on))
                    })
                    DropdownMenuItem(onClick = {
                        globalStore.updateDarkMode(false)
                        expanded = false
                    }, text = {
                        Text(stringResource(Res.string.settings_dark_mode_off))
                    })
                }
            }
        }
        PropertyItem(label = { Text(stringResource(Res.string.settings_loudness)) }) {
            Switch(globalStore.enableLoudnessNormalization, {
                globalStore.updateLoudnessNormalization(it)
            })
        }
        PropertyItem(label = { Text(stringResource(Res.string.settings_kids_mode)) }) {
            Switch(globalStore.kidsMode, {
                globalStore.updateKidsMode(it)
            })
        }
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
            LinkButton(stringResource(Res.string.settings_github_text), "https://github.com/HachimiWorld/hachimi-world-client/discussions")
        }
        PropertyItem(label = { Text(stringResource(Res.string.settings_official_website)) }) {
            LinkButton(stringResource(Res.string.settings_official_site_text), "https://hachimi.world")
        }
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