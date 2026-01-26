@file:OptIn(ExperimentalWasmJsInterop::class)

package world.hachimi.app.font

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.head
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readRemaining
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import world.hachimi.app.BuildKonfig
import world.hachimi.app.logging.Logger
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.bodyTextStyle
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.AppTheme
import world.hachimi.app.util.formatBytes
import kotlin.js.ExperimentalWasmJsInterop

@Composable
fun WithFont(
    global: GlobalStore,
    content: @Composable () -> Unit
) {
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val state = remember(fontFamilyResolver) { FontState(fontFamilyResolver) }

    LaunchedEffect(Unit) {
        state.load()
    }

    val darkMode = global.darkMode ?: isSystemInDarkTheme()
    val typography = bodyTextStyle.copy(
        fontFamily = state.fullFontFamily ?: state.minFontFamily ?: FontFamily.Default
    )
    Logger.d("Font", "current typo: $typography")

    AppTheme(darkTheme = darkMode, typography = typography) {
        Box(Modifier.fillMaxSize()) {
            when {
                state.fontsLoaded -> content()
                state.loadingMinFont -> MinFontLoadingPage(state)
                else -> SplashPage()
            }

            if (state.loadingFullFont) {
                FullFontLoadingIndicator(state)
            }
        }
    }
}

@Composable
private fun SplashPage() {
    Surface(Modifier.fillMaxSize(), color = HachimiTheme.colorScheme.background) {

    }
}

@Composable
private fun MinFontLoadingPage(state: FontState) {
    Surface(Modifier.fillMaxSize(), color = HachimiTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
            ) {
                if (state.error == null) {
                    val bytesTotal = state.bytesTotal
                    if (bytesTotal != null) {
                        val animatedProgress =
                            animateFloatAsState(targetValue = bytesTotal.let {
                                (state.bytesRead.toFloat() / it.toFloat()).coerceIn(0f, 1f)
                            })
                        CircularProgressIndicator(
                            progress = { animatedProgress.value },
                            color = LocalContentColor.current,
                            trackColor = LocalContentColor.current.copy(0.12f)
                        )
                        Text("${formatBytes(state.bytesRead)} / ${formatBytes(bytesTotal)}")
                    } else {
                        CircularProgressIndicator(
                            color = LocalContentColor.current,
                            trackColor = LocalContentColor.current.copy(0.12f)
                        )
                        Text("LOADING")
                    }
                } else {
                    Icon(Icons.Default.Error, contentDescription = "Error")
                    when (state.error) {
                        FontLoadError.NotSupported -> Text("Not supported")
                        FontLoadError.PermissionDenied -> Text("Permission denied")
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun FullFontLoadingIndicator(state: FontState) {
    val bytesTotal = state.fullBytesTotal
    if (bytesTotal != null) {
        val animatedProgress = animateFloatAsState(targetValue = bytesTotal.let {
            (state.fullBytesRead.toFloat() / it.toFloat()).coerceIn(0f, 1f)
        })
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = { animatedProgress.value },
            strokeCap = StrokeCap.Square,
            color = LocalContentColor.current,
            trackColor = Color.Transparent,
        )
    } else {
        LinearProgressIndicator(
            Modifier.fillMaxWidth(),
            strokeCap = StrokeCap.Square,
            color = LocalContentColor.current,
            trackColor = Color.Transparent,
        )
    }
}

class FontState(
    private val fontFamilyResolver: FontFamily.Resolver
) {
    private val TAG = "Font"
    private val fullFontUrl = BuildKonfig.ASSETS_BASE_URL + "/fonts/MiSansVF.ttf"
    private val fullFontSize = 20_093_424L // MiSansVF.ttf file size
    private val minFontUrl = BuildKonfig.ASSETS_BASE_URL + "/fonts/MiSansVF-Min.ttf"
    private val minFontSize = 20_093_424L // Not sure

    var error by mutableStateOf<FontLoadError?>(null)
    var fontsLoaded by mutableStateOf(false)
    var bytesRead by mutableLongStateOf(0L)
    var bytesTotal by mutableStateOf<Long?>(null)
    var loadingMinFont by mutableStateOf(false)
    var fullBytesRead by mutableLongStateOf(0L)
    var fullBytesTotal by mutableStateOf<Long?>(null)
    var loadingFullFont by mutableStateOf(false)
    var fullFontLoaded by mutableStateOf(false)
    var minFontFamily by mutableStateOf<FontFamily?>(null)
    var fullFontFamily by mutableStateOf<FontFamily?>(null)

    /**
     * Load fonts from cache.
     * Or load min font from web and then load full font in the background.
     */
    suspend fun load() = withContext(Dispatchers.Default) {
        if (fontsLoaded) return@withContext

        try {
            Logger.i(TAG, "Loading font from cache")
            val bytes = loadFontFromCache(fullFontUrl)
            if (bytes != null) {
                Logger.i(TAG, "Font cache hit")
                val fontFamily = resolveToFontFamily("MiSansVF", bytes)
                fontFamilyResolver.preload(fontFamily)
                fullFontFamily = fontFamily
                fontsLoaded = true
                fullFontLoaded = true
                Logger.i(TAG, "Font loaded from cache")
                return@withContext
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to load font from cache", e)
        }

        try {
            loadingMinFont = true
            Logger.i(TAG, "Loading min font from web")
            val bytes = loadFontFromWeb(
                url = minFontUrl,
                size = minFontSize,
                onProgress = { a, b ->
                    bytesRead = a
                    bytesTotal = b
                }
            )
            val minFontFamily = resolveToFontFamily("MiSansVF-Min", bytes)
            fontFamilyResolver.preload(minFontFamily)
            this@FontState.minFontFamily = minFontFamily
            fontsLoaded = true
            Logger.i(TAG, "Min font loaded from web")
        } catch (e: Throwable) {
            error = FontLoadError.NotSupported
            Logger.e(TAG, "Failed to load min font from web", e)
            window.alert("加载字体失败")
            return@withContext
        } finally {
            loadingMinFont = false
        }

        try {
            loadingFullFont = true
            Logger.i(TAG, "Loading full font from web")
            val bytes = loadFontFromWeb(
                url = fullFontUrl,
                size = fullFontSize,
                onProgress = { a, b ->
                    fullBytesRead = a
                    fullBytesTotal = b
                }
            )
            val fullFontFamily = resolveToFontFamily("MiSansVF", bytes)
            fontFamilyResolver.preload(fullFontFamily)
            this@FontState.fullFontFamily = fullFontFamily
            fontsLoaded = true
            fullFontLoaded = true
            Logger.i(TAG, "Full font loaded from web")
            saveFontCache(fullFontUrl, bytes)
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to load full font", e)
        } finally {
            loadingFullFont = false
        }
    }

    // These code demonstrated a way to load font from local system.
    // This is only supported in Chrome browser, and it requires user to grant permission.
    // So we finally decided not to use it.
    private fun handlePermission() {
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
    }
}

enum class FontLoadError {
    NotSupported, PermissionDenied
}

expect suspend fun loadFontFromCache(
    url: String
): ByteArray?

expect suspend fun saveFontCache(
    url: String,
    data: ByteArray
)

suspend fun loadFontFromWeb(
    url: String,
    size: Long,
    onProgress: (bytesRead: Long, bytesTotal: Long?) -> Unit
): ByteArray {
    val client = HttpClient {
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 60_000
            socketTimeoutMillis = 60_000
        }
    }
    val contentLength = client.head(url)
        .headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: -1
    Logger.d("Font", "Content length: $contentLength bytes")

    val buffer = client.prepareGet(url).execute {
        val buffer = Buffer()
        val channel = it.bodyAsChannel()
        var totalBytesRead = 0L

        while (!channel.exhausted()) {
            val chunk = channel.readRemaining(1024 * 8)
            totalBytesRead += chunk.remaining
            chunk.transferTo(buffer)
            onProgress(totalBytesRead, contentLength)
        }
        buffer
    }

    val bytes = buffer.readByteArray()
    return bytes
}

fun resolveToFontFamily(
    identify: String,
    bytes: ByteArray
): FontFamily {
    val weights = listOf(
//        FontWeight.Light,
//        FontWeight.Thin,
        FontWeight.Normal,
//        FontWeight.Medium,
//        FontWeight.Bold
    )

    val fonts = weights.map { weight ->
        Font(
            identity = "$identify w_${weight.weight}",
            getData = { bytes },
            variationSettings = FontVariation.Settings(
                FontVariation.weight(weight.weight),
            )
        )
    }
    return FontFamily(fonts)
}