@file:OptIn(ExperimentalWasmJsInterop::class)

package world.hachimi.app.font

import io.ktor.util.toJsArray
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.Int8Array
import org.w3c.fetch.Response
import org.w3c.workers.CacheQueryOptions

actual suspend fun loadFontFromCache(url: String): ByteArray? {
    val caches = window.caches.open("font-cache").await()
    val response = caches.match(url, CacheQueryOptions()).await() as? Response?
        ?: return null

    val arrayBuffer = response.arrayBuffer().await()
    val bytes = Int8Array(arrayBuffer).unsafeCast<ByteArray>()
    return bytes
}

actual suspend fun saveFontCache(url: String, data: ByteArray) {
    val caches = window.caches.open("font-cache").await()
    caches.put(url, Response(data.toJsArray()))
}