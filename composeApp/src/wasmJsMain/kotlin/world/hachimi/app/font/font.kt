package world.hachimi.app.font

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.withContext
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.fetch.Response
import org.w3c.files.Blob
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator


@Composable
fun WithFont(
    content: @Composable () -> Unit
) {
    val fontsLoaded = remember { mutableStateOf(false) }
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val error = remember { mutableStateOf<FontLoadError?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            /*val result = handlePermission().await<PermissionStatus>()

            if (result.state != "granted") {
                error.value = FontLoadError.PermissionDenied
                window.alert("请授予字体访问权限，前往 [浏览器设置 - 隐私与安全 - 网站设置] 查看权限设定")
                return@withContext
            }*/

            try {
                val fontFamily = loadFonts()
                fontFamilyResolver.preload(fontFamily)
                fontsLoaded.value = true
            } catch (e: JsException) {
                val exception = e.thrownValue as? DOMException?
                when (exception?.name) {
                    "NotAllowedError", "SecurityError" ->  {
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
            }
        }
    }
    if (fontsLoaded.value) {
        content()
    } else {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),) {
                if (error.value == null) {
                    CircularProgressIndicator()
                } else {
                    Icon(Icons.Default.Error, contentDescription = "Error")
                    when (error.value) {
                        FontLoadError.NotSupported -> Text("Not supported")
                        FontLoadError.PermissionDenied -> Text("Permission denied")
                        null -> {}
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

external interface PermissionStatus : JsAny {
    val name: String
    val state: String
}

fun handlePermission(): Promise<PermissionStatus> = js("""
  navigator.permissions.query({ name: "local-fonts" })
""")

fun queryLocalFonts(): Promise<JsArray<FontData>>
    = js("window.queryLocalFonts()")

private val preferredCJKFontFamilies = linkedSetOf("Microsoft YaHei", "PingFang SC", "Noto Sans SC", "Noto Sans CJK");
private val preferredEmojiFontFamilies = linkedSetOf("Segoe UI Emoji", "Apple Color Emoji", "Noto Color Emoji");

// Demibold Italic
private fun parseFontStyle(styleString: String): Pair<FontWeight, FontStyle> {
    val part = styleString.split(" ");
    val weight = when (part[0].lowercase()) {
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

    val style = part.getOrNull(2)?.let {
        when (it.lowercase()) {
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

suspend fun loadLocalFonts(): List<LoadedLocalFont> {
    val fonts = try {
        queryLocalFonts().await<JsArray<FontData>>().toList()
    } catch (e: Exception) {
        error("Can't load fonts")
    }
    val fontMap = fonts.groupBy { it.family }

    val firstFont = preferredCJKFontFamilies.firstNotNullOfOrNull {
        fontMap[it]
    } ?: error("Cant find CJK fonts in computer")

    println("CJK font was found: ${firstFont.first().family}")

    val loaded = firstFont.map { fontData ->
        println("Reading font: ${fontData.family}, ${fontData.style}, ${fontData.postscriptName}")
        val (weight, style) = parseFontStyle(fontData.style)

        val blob = fontData.blob().await<Blob>()

        val reader = FileReader()
        reader.readAsArrayBuffer(blob)
        suspendCoroutine<Unit> { cont ->
            reader.addEventListener("loadend") {
                cont.resume(Unit)
            }
        }

        val buffer = reader.result as ArrayBuffer
        LoadedLocalFont(fontData.family, weight, style, buffer)
    }

    return loaded
}

suspend fun loadFonts(): FontFamily {
    val fonts = loadLocalFonts()
    val composeFonts = fonts.map { font ->
        Font(
            "${font.family} ${font.weight.weight} ${font.style}",
            font.data.toByteArray(),
            font.weight,
            font.style
        )
    }
    val fontFamily = FontFamily(composeFonts)
    return fontFamily
}