package world.hachimi.app.ui.share

import coil3.PlatformContext
import js.array.toJsArray
import js.buffer.toArrayBuffer
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.unsafeCast
import web.blob.Blob
import web.dom.document
import web.html.HTMLAnchorElement
import web.navigator.navigator
import web.url.URL

@OptIn(ExperimentalWasmJsInterop::class)
actual fun copyToClipboard(context: PlatformContext, text: String) {
    navigator.clipboard.writeTextAsync(text)
}

@OptIn(ExperimentalWasmJsInterop::class, ExperimentalUnsignedTypes::class)
actual suspend fun savePosterImage(context: PlatformContext, bytes: ByteArray, fileName: String): Boolean {
    return try {
        val blob = Blob(listOf(bytes.toArrayBuffer()).toJsArray())
        val url = URL.createObjectURL(blob)
        val anchor = document.createElement("a").unsafeCast<HTMLAnchorElement>()
        anchor.href = url
        anchor.download = fileName
        anchor.click()
        URL.revokeObjectURL(url)
        true
    } catch (e: Throwable) {
        false
    }
}
