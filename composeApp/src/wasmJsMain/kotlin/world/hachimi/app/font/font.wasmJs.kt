@file:OptIn(ExperimentalWasmJsInterop::class)

package world.hachimi.app.font

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import io.ktor.util.toJsArray
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.fetch.Response
import org.w3c.files.Blob
import org.w3c.files.FileReader
import org.w3c.workers.Cache
import org.w3c.workers.CacheQueryOptions
import world.hachimi.app.logging.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise
import kotlin.time.TimeSource
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

external interface DOMException : JsAny {
    val code: String
    val message: String
    val name: String
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

actual suspend fun loadFontFromCache(url: String) : ByteArray? {
    val caches = window.caches.open("font-cache").await<Cache>()
    val response = caches.match(url, CacheQueryOptions()).await<Response?>()
        ?: return null

    val arrayBuffer = response.arrayBuffer().await<ArrayBuffer>()
    val bytes = arrayBuffer.toByteArray()
    return bytes
}

actual suspend fun saveFontCache(url: String, data: ByteArray) {
    val caches = window.caches.open("font-cache").await<Cache>()
    caches.put(url, Response(data.toJsArray()))
}