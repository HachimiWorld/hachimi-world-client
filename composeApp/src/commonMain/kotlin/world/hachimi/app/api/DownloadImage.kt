package world.hachimi.app.api

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

/**
 * The bytes of an image on the storage host, fetched as the image loader fetches it, to save it.
 *
 * @since 261113
 */
suspend fun ApiClient.downloadImage(url: String): ByteArray {
    val resp = httpClient.get(url) { header(HttpHeaders.Referrer, "https://hachimi.world/") }
    check(resp.status.isSuccess()) { "HTTP ${resp.status.value} for $url" }
    return resp.readRawBytes()
}
