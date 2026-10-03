package world.hachimi.app.update

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.UserAgent
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.hachimi.app.getPlatform
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

class PackageVerificationException(message: String) : IOException(message)

/**
 * Downloads update packages into [dir], one package at a time.
 *
 * The download goes to `<name>.part` and resumes with a Range request after interruptions. Only a package whose size
 * and SHA-256 match is renamed to `<name>`, so a file under its final name is always complete.
 */
class PackageDownloader(
    private val dir: File,
    private val client: HttpClient = defaultClient,
) {
    suspend fun findDownloaded(spec: UpdatePackageSpec): File? = withContext(Dispatchers.IO) {
        File(dir, spec.fileName).takeIf { it.isFile && verify(it, spec) }
    }

    suspend fun download(spec: UpdatePackageSpec, onProgress: (downloaded: Long, total: Long?) -> Unit): File =
        withContext(Dispatchers.IO) {
            dir.mkdirs()
            val target = File(dir, spec.fileName)
            val part = File(dir, "${spec.fileName}.part")
            // Packages of older releases are no longer needed
            dir.listFiles()?.forEach { if (it.name != target.name && it.name != part.name) it.deleteRecursively() }

            if (target.isFile) {
                if (verify(target, spec)) return@withContext target
                target.delete()
            }

            var offset = if (part.isFile) part.length() else 0L
            if (spec.size != null && offset > spec.size) {
                part.delete()
                offset = 0
            }
            if (spec.size == null || offset < spec.size) {
                fetch(spec, part, offset, onProgress)
            }

            if (!verify(part, spec)) {
                part.delete()
                throw PackageVerificationException("Package ${spec.fileName} does not match the expected size or SHA-256")
            }
            Files.move(part.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
            target
        }

    private suspend fun fetch(
        spec: UpdatePackageSpec,
        part: File,
        offset: Long,
        onProgress: (downloaded: Long, total: Long?) -> Unit,
    ) {
        client.prepareGet(spec.url) {
            if (offset > 0) header(HttpHeaders.Range, "bytes=$offset-")
        }.execute { resp ->
            val append = when (resp.status) {
                HttpStatusCode.PartialContent -> true
                // The server ignored the Range header, start over
                HttpStatusCode.OK -> false
                // The part file already holds the whole package
                HttpStatusCode.RequestedRangeNotSatisfiable -> return@execute
                else -> throw IOException("Unexpected status ${resp.status} downloading ${spec.url}")
            }
            var downloaded = if (append) offset else 0L
            val total = resp.contentLength()?.let { it + downloaded } ?: spec.size
            onProgress(downloaded, total)

            val channel = resp.bodyAsChannel()
            val buffer = ByteArray(64 * 1024)
            FileOutputStream(part, append).use { out ->
                while (true) {
                    val read = channel.readAvailable(buffer, 0, buffer.size)
                    if (read == -1) break
                    out.write(buffer, 0, read)
                    downloaded += read
                    onProgress(downloaded, total)
                }
            }
        }
    }

    suspend fun clear(): Unit = withContext(Dispatchers.IO) {
        dir.listFiles()?.forEach { it.deleteRecursively() }
    }

    private fun verify(file: File, spec: UpdatePackageSpec): Boolean {
        if (spec.size != null && file.length() != spec.size) return false
        if (spec.sha256 != null && sha256(file) != spec.sha256) return false
        return true
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private companion object {
        val defaultClient by lazy {
            HttpClient {
                install(HttpTimeout) {
                    connectTimeoutMillis = 30_000
                    socketTimeoutMillis = 60_000
                    // Packages are large; rely on the socket timeout to detect stalls instead
                    requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                }
                install(UserAgent) {
                    agent = getPlatform().userAgent
                }
            }
        }
    }
}
