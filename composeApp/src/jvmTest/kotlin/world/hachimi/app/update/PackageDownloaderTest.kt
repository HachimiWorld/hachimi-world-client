package world.hachimi.app.update

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.InetSocketAddress
import java.nio.file.Files
import java.security.MessageDigest
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PackageDownloaderTest {
    private val content = Random(42).nextBytes(300_000)
    private val sha256 = MessageDigest.getInstance("SHA-256").digest(content).joinToString("") { "%02x".format(it) }

    private lateinit var server: HttpServer
    private lateinit var dir: File
    private val rangeHeaders = mutableListOf<String?>()

    /** Bytes to send before dropping the connection, null to send everything. */
    @Volatile
    private var cutAfter: Int? = null

    @Volatile
    private var honorRange = true

    @BeforeTest
    fun setUp() {
        dir = Files.createTempDirectory("updates").toFile()
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/app.msi") { exchange ->
            val range = exchange.requestHeaders.getFirst("Range")
            synchronized(rangeHeaders) { rangeHeaders += range }
            val start = range?.takeIf { honorRange }?.removePrefix("bytes=")?.removeSuffix("-")?.toInt() ?: 0
            if (start >= content.size) {
                exchange.sendResponseHeaders(416, -1)
                exchange.close()
                return@createContext
            }
            val body = content.copyOfRange(start, content.size)
            exchange.sendResponseHeaders(if (start > 0) 206 else 200, body.size.toLong())
            try {
                exchange.responseBody.use { out ->
                    val cut = cutAfter
                    if (cut == null) out.write(body) else out.write(body, 0, cut)
                }
            } catch (_: Exception) {
                // Closing with fewer bytes than announced, as a dropped connection would
            }
        }
        server.start()
    }

    @AfterTest
    fun tearDown() {
        server.stop(0)
        dir.deleteRecursively()
    }

    private fun spec(sha256: String? = this.sha256, size: Long? = content.size.toLong()) = UpdatePackageSpec(
        url = "http://127.0.0.1:${server.address.port}/app.msi",
        fileName = "app.msi",
        size = size,
        sha256 = sha256,
    )

    @Test
    fun downloadsAndVerifies() = runBlocking {
        var lastProgress = 0L to null as Long?
        val file = PackageDownloader(dir).download(spec()) { downloaded, total -> lastProgress = downloaded to total }

        assertEquals(File(dir, "app.msi"), file)
        assertContentEquals(content, file.readBytes())
        assertEquals(content.size.toLong() to content.size.toLong(), lastProgress)
        assertFalse(File(dir, "app.msi.part").exists())
    }

    @Test
    fun resumesAnInterruptedDownload() = runBlocking {
        val downloader = PackageDownloader(dir)
        cutAfter = 100_000
        runCatching { downloader.download(spec()) { _, _ -> } }
        val partLength = File(dir, "app.msi.part").length()
        assertTrue(partLength in 1 until content.size, "part file has $partLength bytes")

        cutAfter = null
        val file = downloader.download(spec()) { _, _ -> }

        assertContentEquals(content, file.readBytes())
        assertEquals("bytes=$partLength-", rangeHeaders.last())
    }

    @Test
    fun startsOverWhenTheServerIgnoresRange() = runBlocking {
        val downloader = PackageDownloader(dir)
        cutAfter = 100_000
        runCatching { downloader.download(spec()) { _, _ -> } }

        cutAfter = null
        honorRange = false
        val file = downloader.download(spec()) { _, _ -> }

        assertContentEquals(content, file.readBytes())
    }

    @Test
    fun rejectsAPackageWithTheWrongHash() = runBlocking {
        assertFailsWith<PackageVerificationException> {
            PackageDownloader(dir).download(spec(sha256 = "0".repeat(64))) { _, _ -> }
        }
        assertFalse(File(dir, "app.msi").exists())
        assertFalse(File(dir, "app.msi.part").exists())
    }

    @Test
    fun findsOnlyAVerifiedPackage() = runBlocking {
        val downloader = PackageDownloader(dir)
        assertNull(downloader.findDownloaded(spec()))

        downloader.download(spec()) { _, _ -> }

        assertNotNull(downloader.findDownloaded(spec()))
        assertNull(downloader.findDownloaded(spec(sha256 = "0".repeat(64))))
    }

    @Test
    fun removesPackagesOfOtherVersions() = runBlocking {
        dir.mkdirs()
        File(dir, "old.msi").writeText("old")
        File(dir, "old.msi.part").writeText("old")

        PackageDownloader(dir).download(spec()) { _, _ -> }

        assertEquals(setOf("app.msi"), dir.list()!!.toSet())
    }
}
