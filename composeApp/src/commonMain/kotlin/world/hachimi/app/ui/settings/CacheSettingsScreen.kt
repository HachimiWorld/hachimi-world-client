package world.hachimi.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.storage.PreferencesKeys
import world.hachimi.app.storage.SongCache
import world.hachimi.app.ui.insets.safeAreaPadding
import kotlin.math.pow
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheSettingsScreen() {
    val globalStore = koinInject<GlobalStore>()
    val songCache = koinInject<SongCache>()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var cacheSize by remember { mutableStateOf(0L) }
    var maxCacheSizeGB by remember { mutableStateOf(1.0f) }
    var maxCacheSizeInput by remember { mutableStateOf("1.0") }
    var freeSpace by remember { mutableStateOf(0L) }
    var totalSpace by remember { mutableStateOf(0L) }
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        cacheSize = songCache.getSize()
        freeSpace = songCache.getFreeSpace()
        totalSpace = songCache.getTotalSpace()
        val savedSize = globalStore.dataStore.get(PreferencesKeys.SETTINGS_CACHE_SIZE_GB)
        if (savedSize != null) {
            maxCacheSizeGB = savedSize
            maxCacheSizeInput = savedSize.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("缓存设置") },
                navigationIcon = {
                    IconButton(onClick = { globalStore.nav.back() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("当前缓存大小: ${formatSize(cacheSize)}", style = MaterialTheme.typography.bodyLarge)
            Text("剩余可用空间: ${formatSize(freeSpace)}", style = MaterialTheme.typography.bodyMedium)

            OutlinedTextField(
                value = maxCacheSizeInput,
                onValueChange = {
                    maxCacheSizeInput = it
                    val value = it.toFloatOrNull()
                    if (value != null && value > 0) {
                        val valueBytes = (value * 1024 * 1024 * 1024).toLong()
                        val maxAllowedBytes = minOf(
                            50L * 1024 * 1024 * 1024, // 50GB
                            freeSpace + cacheSize
                        )

                        if (valueBytes <= maxAllowedBytes) {
                            errorText = null
                        } else {
                            errorText = "输入值超过最大限制 (${formatSize(maxAllowedBytes)})"
                        }
                    } else {
                        errorText = "请输入有效的数字"
                    }
                },
                label = { Text("最大缓存大小 (GB)") },
                isError = errorText != null,
                supportingText = {
                    if (errorText != null) {
                        Text(errorText!!)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val value = maxCacheSizeInput.toFloatOrNull()
                    if (value != null && errorText == null) {
                        scope.launch {
                            globalStore.dataStore.set(PreferencesKeys.SETTINGS_CACHE_SIZE_GB, value)
                            maxCacheSizeGB = value

                            val valueBytes = (value * 1024 * 1024 * 1024).toLong()
                            songCache.trim(valueBytes)
                            cacheSize = songCache.getSize()
                            freeSpace = songCache.getFreeSpace()

                            snackbarHostState.showSnackbar("缓存大小设置已保存")
                        }
                    }
                },
                enabled = errorText == null && maxCacheSizeInput.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存设置")
            }

            Button(
                onClick = {
                    scope.launch {
                        songCache.clear()
                        cacheSize = songCache.getSize()
                        freeSpace = songCache.getFreeSpace()
                        snackbarHostState.showSnackbar("缓存已清除")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("清除缓存")
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1 -> "${gb.format(2)} GB"
        mb >= 1 -> "${mb.format(2)} MB"
        kb >= 1 -> "${kb.format(2)} KB"
        else -> "$bytes Bytes"
    }
}

private fun Double.format(digits: Int): String {
    val factor = 10.0.pow(digits)
    val rounded = round(this * factor) / factor
    return rounded.toString()
}
