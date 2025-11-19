@file:OptIn(ExperimentalWasmJsInterop::class)

package world.hachimi.app.font

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

actual suspend fun loadFontFromWeb(
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