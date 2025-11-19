@file:OptIn(ExperimentalWasmJsInterop::class)

package world.hachimi.app.font

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import world.hachimi.app.BuildKonfig
import world.hachimi.app.logging.Logger
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.theme.AppTheme
import world.hachimi.app.util.formatBytes
import kotlin.js.ExperimentalWasmJsInterop

private val fullFontUrl = BuildKonfig.ASSETS_BASE_URL + "/fonts/MiSansVF.ttf"
private val fullFontSize = 20_093_424L // MiSansVF.ttf file size
private val minFontUrl = BuildKonfig.ASSETS_BASE_URL + "/fonts/MiSansVF-Min.ttf"
private val minFontSize = 20_093_424L // Not sure

@Composable
fun WithFont(
    content: @Composable () -> Unit
) {
    val fontsLoaded = remember { mutableStateOf(false) }
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val error = remember { mutableStateOf<FontLoadError?>(null) }
    var bytesRead by remember { mutableLongStateOf(0L) }
    var bytesTotal by remember { mutableStateOf<Long?>(null) }
    var fullBytesRead by remember { mutableLongStateOf(0L) }
    var fullBytesTotal by remember { mutableStateOf<Long?>(null) }
    var fullFontLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (fontsLoaded.value) return@LaunchedEffect

        withContext(Dispatchers.Default) {
            /*val result = handlePermission().await<PermissionStatus>()

            if (result.state != "granted") {
                error.value = FontLoadError.PermissionDenied
                window.alert("请授予字体访问权限，前往 [浏览器设置 - 隐私与安全 - 网站设置] 查看权限设定")
                return@withContext
            }*/

            /*try {
                val fontFamily = loadFonts(true)
                fontFamilyResolver.preload(fontFamily)
                fontsLoaded.value = true
            } catch (e: JsException) {
                @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
                val exception = e.thrownValue as? DOMException?
                when (exception?.name) {
                    "NotAllowedError", "SecurityError" -> {
                        error.value = FontLoadError.PermissionDenied
                        window.alert("请授予字体访问权限，前往 [浏览器设置 - 隐私与安全 - 网站设置] 查看权限设定")
                    }
                    else -> {
                        error.value = FontLoadError.NotSupported
                        window.alert("加载字体失败，当前仅支持 PC 端 Chrome / Edge 浏览器最新版本，不支持 Firefox, Safari 浏览器")
                    }
                }
            } catch (_: Throwable) {
                error.value = FontLoadError.NotSupported
                window.alert("加载字体失败，当前仅支持 PC 端 Chrome / Edge 浏览器最新版本，不支持 Firefox, Safari 浏览器")
            }*/

            try {
                val minFontFamily = loadFontFromWeb(
                    identify = "MiSansVF-Min",
                    url = minFontUrl,
                    size = minFontSize,
                    onProgress = { a, b ->
                        bytesRead = a
                        bytesTotal = b
                    })
                fontFamilyResolver.preload(minFontFamily)
                fontsLoaded.value = true
            } catch (e: Throwable) {
                error.value = FontLoadError.NotSupported
                Logger.e("Font", "Failed to load fonts from web", e)
                window.alert("加载字体失败")
                return@withContext
            }

            try {
                val fullFontFamily = loadFontFromWeb(
                    identify = "MiSansVF",
                    url = fullFontUrl,
                    size = fullFontSize,
                    onProgress = { a, b ->
                        fullBytesRead = a
                        fullBytesTotal = b
                    })
                // TODO: Will this correctly override the min font?
                fontFamilyResolver.preload(fullFontFamily)
                fontsLoaded.value = true
                fullFontLoaded = true
                Logger.i("font", "Full font loaded")
            } catch (e: Throwable) {
                Logger.e("font", "Failed to load full font", e)
            }
        }
    }

    if (fontsLoaded.value) {
        Box(Modifier.fillMaxSize()) {
            key(fullFontLoaded) {
                content()
            }

            val global = koinInject<GlobalStore>()
            val darkMode = global.darkMode ?: isSystemInDarkTheme()
            AppTheme(darkTheme = darkMode) {
                if (!fullFontLoaded) {
                    val bytesTotal = fullBytesTotal
                    if (bytesTotal != null) {
                        val animatedProgress = animateFloatAsState(targetValue = bytesTotal.let {
                            (fullBytesRead.toFloat() / it.toFloat()).coerceIn(0f, 1f)
                        })
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            progress = { animatedProgress.value })
                    } else {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
            }
        }
    } else {
        val global = koinInject<GlobalStore>()
        val darkMode = global.darkMode ?: isSystemInDarkTheme()
        AppTheme(darkTheme = darkMode) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                    ) {
                        if (error.value == null) {
                            val bytesTotal = bytesTotal
                            if (bytesTotal != null) {
                                val animatedProgress = animateFloatAsState(targetValue = bytesTotal.let {
                                    (bytesRead.toFloat() / it.toFloat()).coerceIn(0f, 1f)
                                })
                                CircularProgressIndicator(progress = { animatedProgress.value })

                                Text("${formatBytes(bytesRead)} / ${formatBytes(bytesTotal)}")
                            } else {
                                CircularProgressIndicator()

                                Text("Loading...")
                            }
                        } else {
                            Icon(Icons.Default.Error, contentDescription = "Error")
                            when (error.value) {
                                FontLoadError.NotSupported -> Text("Not supported")
                                FontLoadError.PermissionDenied -> Text("Permission denied")
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class FontLoadError {
    NotSupported, PermissionDenied
}

expect suspend fun loadFontFromWeb(
    identify: String,
    url: String,
    size: Long,
    onProgress: (bytesRead: Long, bytesTotal: Long?) -> Unit
): FontFamily