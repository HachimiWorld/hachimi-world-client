@file:OptIn(ExperimentalWasmJsInterop::class)

package world.hachimi.app.font

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import js.buffer.ArrayBuffer
import js.buffer.toByteArray
import kotlinx.coroutines.await
import web.blob.Blob
import web.blob.arrayBuffer
import world.hachimi.app.logging.Logger
import kotlin.js.Promise
import kotlin.time.TimeSource

external interface DOMException : JsAny {
    val code: String
    val message: String
    val name: String
}

@Composable
internal fun returnsNullable(): Any? = null

external class FontData : JsAny {
    val postscriptName: String
    val fullName: String
    val family: String
    val style: String

    fun blob(): Promise<Blob>
}

suspend fun FontData.readArrayBuffer(): ArrayBuffer {
    val blob = blob().await<Blob>()
    return blob.arrayBuffer()
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

private val preferredCJKFontFamilies =
    linkedSetOf("Microsoft YaHei", "PingFang SC", "Noto Sans SC", "Noto Sans CJK");
private val preferredEmojiFontFamilies =
    linkedSetOf("Segoe UI Emoji", "Apple Color Emoji", "Noto Color Emoji");

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
