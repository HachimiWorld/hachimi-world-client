package world.hachimi.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import world.hachimi.app.BuildKonfig
import world.hachimi.app.getPlatform
import world.hachimi.app.model.GlobalStore

@Composable
fun SettingsScreen() {
    val globalStore = koinInject<GlobalStore>()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.titleLarge)

        PropertyItem(label = {
            Text("深色模式", style = MaterialTheme.typography.bodyLarge)
        }) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(
                        when (globalStore.darkMode) {
                            true -> "始终开启"
                            false -> "始终关闭"
                            null -> "跟随系统"
                        }
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
                DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = {
                        globalStore.updateDarkMode(null)
                        expanded = false
                    }, text = {
                        Text("跟随系统")
                    })
                    DropdownMenuItem(onClick = {
                        globalStore.updateDarkMode(true)
                        expanded = false
                    }, text = {
                        Text("始终开启")
                    })
                    DropdownMenuItem(onClick = {
                        globalStore.updateDarkMode(false)
                        expanded = false
                    }, text = {
                        Text("始终关闭")
                    })
                }
            }
        }
        PropertyItem(label = { Text("响度均衡") }) {
            Switch(globalStore.enableLoudnessNormalization, {
                globalStore.updateLoudnessNormalization(it)
            })
        }
        PropertyItem(label = { Text("宝宝模式") }) {
            Switch(globalStore.kidsMode, {
                globalStore.updateKidsMode(it)
            })
        }
        PropertyItem(label = { Text("版本名") }) {
            Text(BuildKonfig.VERSION_NAME)
        }
        PropertyItem(label = { Text("版本号") }) {
            Text(BuildKonfig.VERSION_CODE.toString())
        }
        PropertyItem(label = { Text("反馈与建议") }) {
            TextButton(onClick = {
                getPlatform().openUrl("https://github.com/HachimiWorld/hachimi-world-client/discussions")
            }) {
                Text("GitHub")
            }
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