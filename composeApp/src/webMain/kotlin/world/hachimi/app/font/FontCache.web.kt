package world.hachimi.app.font

import js.buffer.toArrayBuffer
import web.cache.caches
import web.cache.match
import web.cache.open
import web.cache.put
import web.http.BodyInit
import web.http.Response
import web.http.byteArray

suspend fun loadFontFromCache(url: String): ByteArray? {
    val cache = caches.open("font-cache")
    val response = cache.match(url) ?: return null
    return response.byteArray()
}

suspend fun saveFontCache(url: String, data: ByteArray) {
    val cache = caches.open("font-cache")
    cache.put(url, Response(BodyInit(data.toArrayBuffer())))
}
