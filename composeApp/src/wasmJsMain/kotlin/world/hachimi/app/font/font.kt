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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.koin.compose.koinInject
import org.w3c.fetch.Response
import org.w3c.files.Blob
import org.w3c.files.FileReader
import world.hachimi.app.BuildKonfig
import world.hachimi.app.logging.Logger
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.theme.AppTheme
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise
import kotlin.time.TimeSource
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

@Composable
actual fun WithFont(
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

external interface DOMException : JsAny {
    val code: String
    val message: String
    val name: String
}

enum class FontLoadError {
    NotSupported, PermissionDenied
}

@Composable
internal fun returnsNullable(): Any? = null

suspend fun loadRes(url: String): ArrayBuffer {
    return window.fetch(url).await<Response>().arrayBuffer().await()
}

fun ArrayBuffer.toByteArray(): ByteArray {
    val source = Int8Array(this, 0, byteLength)
    return jsInt8ArrayToKotlinByteArray(source)
}

internal fun jsExportInt8ArrayToWasm(src: Int8Array, size: Int, dstAddr: Int): Unit = js(
    """{
    const mem8 = new Int8Array(wasmExports.memory.buffer, dstAddr, size);
    mem8.set(src);
}"""
)

internal fun jsInt8ArrayToKotlinByteArray(x: Int8Array): ByteArray {
    val size = x.length

    @OptIn(UnsafeWasmMemoryApi::class)
    return withScopedMemoryAllocator { allocator ->
        val memBuffer = allocator.allocate(size)
        val dstAddress = memBuffer.address.toInt()
        jsExportInt8ArrayToWasm(x, size, dstAddress)
        ByteArray(size) { i -> (memBuffer + i).loadByte() }
    }
}

external class FontData : JsAny {
    val postscriptName: String
    val fullName: String
    val family: String
    val style: String

    fun blob(): Promise<Blob>
}

suspend fun FontData.readArrayBuffer(): ArrayBuffer {
    val blob = blob().await<Blob>()
    val reader = FileReader()
    reader.readAsArrayBuffer(blob)
    suspendCoroutine<Unit> { cont ->
        reader.addEventListener("loadend") {
            cont.resume(Unit)
        }
    }
    val buffer = reader.result as ArrayBuffer
    return buffer
}

external interface PermissionStatus : JsAny {
    val name: String
    val state: String
}

fun handlePermission(): Promise<PermissionStatus> = js(
    """
  navigator.permissions.query({ name: "local-fonts" })
"""
)

fun queryLocalFonts(): Promise<JsArray<FontData>> = js("window.queryLocalFonts()")

private val preferredCJKFontFamilies = linkedSetOf("Microsoft YaHei", "PingFang SC", "Noto Sans SC", "Noto Sans CJK");
private val preferredEmojiFontFamilies = linkedSetOf("Segoe UI Emoji", "Apple Color Emoji", "Noto Color Emoji");

// Demibold Italic
private fun parseFontStyle(styleString: String): Pair<FontWeight, FontStyle> {
    val part = styleString.split(" ")
    val weightPart = part[0].lowercase()
    val stylePart = part.getOrNull(1)?.lowercase()
    val weight = when (weightPart) {
        "thin", "hairline" -> FontWeight.Thin
        "extralight", "ultralight" -> FontWeight.ExtraLight
        "light" -> FontWeight.Light
        "normal", "regular" -> FontWeight.Normal
        "medium" -> FontWeight.Medium
        "semibold", "demibold" -> FontWeight.SemiBold
        "bold" -> FontWeight.Bold
        "extrabold", "ultrabold" -> FontWeight.ExtraBold
        "black", "heavy" -> FontWeight.Black
        else -> FontWeight.Normal
    }

    val style = stylePart?.let {
        when (it) {
            "italic", "oblique" -> FontStyle.Italic
            else -> FontStyle.Normal
        }
    } ?: FontStyle.Normal

    return weight to style
}

data class LoadedLocalFont(
    val family: String,
    val weight: FontWeight,
    val style: FontStyle,
    val data: ArrayBuffer,
)

private suspend fun queryLocalFontMap(): Map<String, List<FontData>> {
    val fonts = try {
        queryLocalFonts().await<JsArray<FontData>>().toList()
    } catch (e: Throwable) {
        error("Can't load fonts")
    }
    val fontMap = fonts.groupBy { it.family }
    return fontMap
}

private suspend fun loadLocalCJKFonts(): List<LoadedLocalFont> {
    val mark = TimeSource.Monotonic.markNow()

    val fontMap = queryLocalFontMap()

    val firstFont = preferredCJKFontFamilies.firstNotNullOfOrNull {
        fontMap[it]
    } ?: error("Cant find CJK fonts in computer")

    Logger.d("Font", "CJK font was found: ${firstFont.first().family}")

    val loaded = firstFont.map { fontData ->
        val (weight, style) = parseFontStyle(fontData.style)
        Logger.d(
            "Font",
            "Loading font ${fontData.postscriptName} ${fontData.style} -> Weight: ${weight.weight}, Style: $style"
        )
        val buffer = fontData.readArrayBuffer()
        LoadedLocalFont(fontData.family, weight, style, buffer)
    }

    mark.elapsedNow().inWholeMilliseconds.let {
        Logger.d("Font", "Loaded CJK fonts in $it ms")
    }

    return loaded
}

private suspend fun loadLocalEmojiFonts(): List<LoadedLocalFont> {
    val mark = TimeSource.Monotonic.markNow()
    val fontMap = queryLocalFontMap()

    val emojiFonts = preferredEmojiFontFamilies.firstNotNullOfOrNull { fontMap[it] }
        ?: error("Can't find emoji fonts in computer")

    Logger.d("Font", "Emoji font was found: ${emojiFonts.first().family}")
    val loadedEmoji = emojiFonts.map { fontData ->
        val buffer = fontData.readArrayBuffer()
        LoadedLocalFont(fontData.family, FontWeight.Normal, FontStyle.Normal, buffer)
    }
    mark.elapsedNow().inWholeMilliseconds.let {
        Logger.d("Font", "Loaded emoji fonts in $it ms")
    }
    return loadedEmoji
}

suspend fun loadFonts(enableEmoji: Boolean): FontFamily {
    var fonts = loadLocalCJKFonts()
    if (enableEmoji) {
        fonts = fonts + loadLocalEmojiFonts()
    }

    val composeFonts = fonts.map { font ->
        Font(
            "${font.family} ${font.weight.weight} ${font.style}",
            font.data.toByteArray(),
            font.weight,
            font.style
        )
    }
    val fontFamily = FontFamily(composeFonts)
    Logger.d("Font", "Fonts loaded successfully")
    return fontFamily
}

private val fullFontUrl = BuildKonfig.ASSETS_BASE_URL + "/fonts/MiSansVF.ttf"
private val fullFontSize = 20_093_424L // MiSansVF.ttf file size
private val minFontUrl = BuildKonfig.ASSETS_BASE_URL + "/fonts/MiSansVF-Min.ttf"
private val minFontSize = 20_093_424L // Not sure

suspend fun loadFontFromWeb(
    identify: String,
    url: String,
    size: Long,
    onProgress: (bytesRead: Long, bytesTotal: Long?) -> Unit
): FontFamily {
    val client = HttpClient { }
    /*val contentLength = client.head("https://storage.hachimi.world/fonts/MiSansVF.ttf")
        .headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: -1
    Logger.d("Font", "Content length: $contentLength bytes")*/

    val buffer = client.prepareGet(url).execute {
        val buffer = Buffer()
        val channel = it.bodyAsChannel()
        var totalBytesRead = 0L

        while (!channel.exhausted()) {
            val chunk = channel.readRemaining(1024 * 8)
            totalBytesRead += chunk.remaining
            chunk.transferTo(buffer)
            onProgress(totalBytesRead, size)
        }
        buffer
    }

    val bytes = buffer.readByteArray()
    val weights = listOf(FontWeight.Light, FontWeight.Thin, FontWeight.Normal, FontWeight.Medium, FontWeight.Bold)

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

@Stable
private fun formatBytes(bytes: Long): String {
    if (bytes == 0L) return "0 MB"
    return ((bytes.toFloat() / (1024 * 1024) * 10).toInt()).toString()
        .toCharArray()
        .toMutableList()
        .also {
            if (it.size < 2) {
                it.add(0, '0')
            }
            it.add(it.lastIndex, '.')
        }
        .joinToString("")
        .plus(" MB")
}
