package world.hachimi.app.update

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.jetbrains.compose.resources.StringResource
import world.hachimi.app.api.ApiClient
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferenceKey
import java.net.InetSocketAddress
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration

class UpdateManagerTest {
    private lateinit var server: HttpServer

    /** Version numbers the fake server returns; 1 is always older than the running build. */
    @Volatile
    private var versionNumbers = listOf(Int.MAX_VALUE)

    private val dataStore = InMemoryDataStore()
    private val platform = FakePlatform()
    private val alerts = mutableListOf<StringResource>()

    @BeforeTest
    fun setUp() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/version/page") { exchange ->
            val items = versionNumbers.joinToString(",") { number ->
                """{"version_name":"v$number","version_number":$number,"changelog":"","variant":"test",
                   |"url":"https://example.com/app-$number.msi","release_time":"2026-01-01T00:00:00Z",
                   |"size":3,"sha256":"${"a".repeat(64)}"}""".trimMargin()
            }
            val body = """{"ok":true,"data":{"data":[$items],"page_index":0,"page_size":50,"total":${versionNumbers.size}}}"""
                .toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
    }

    @AfterTest
    fun tearDown() {
        server.stop(0)
    }

    private fun manager() = UpdateManager(
        api = ApiClient("http://127.0.0.1:${server.address.port}"),
        dataStore = dataStore,
        alert = { synchronized(alerts) { alerts += it } },
        alertText = {},
        platform = platform,
        retryDelays = listOf(Duration.ZERO, Duration.ZERO),
    )

    @Test
    fun downloadsInTheBackgroundAndPromptsOnce() = runBlocking {
        val first = manager()
        first.start()
        waitUntil { first.downloadState is UpdateManager.DownloadState.Ready }
        assertEquals(UpdateManager.Prompt.READY, first.prompt)
        assertEquals(1, platform.downloads)
        first.dismissPrompt()

        // Next launch finds the package already downloaded and stays quiet
        val second = manager()
        second.start()
        waitUntil { second.downloadState is UpdateManager.DownloadState.Ready }
        assertNull(second.prompt)
        assertEquals(1, platform.downloads)

        // Unless the user checks manually
        second.checkManually()
        waitUntil { second.prompt == UpdateManager.Prompt.READY }

        second.install()
        assertEquals(1, platform.installs)
    }

    @Test
    fun asksBeforeDownloadingOnAMeteredNetwork() = runBlocking {
        platform.metered = true
        val updates = manager()
        updates.start()
        waitUntil { updates.prompt == UpdateManager.Prompt.AVAILABLE }
        assertIs<UpdateManager.DownloadState.Idle>(updates.downloadState)
        assertEquals(0, platform.downloads)

        updates.confirmPrompt()
        waitUntil { updates.prompt == UpdateManager.Prompt.READY }
        assertEquals(1, platform.downloads)
    }

    @Test
    fun asksBeforeDownloadingWhenAutoDownloadIsOff() = runBlocking {
        val setup = manager()
        setup.updateAutoDownload(false).join()

        val updates = manager()
        updates.start()
        waitUntil { updates.prompt == UpdateManager.Prompt.AVAILABLE }
        assertEquals(0, platform.downloads)
    }

    @Test
    fun retriesAndThenReportsFailure() = runBlocking {
        platform.failDownloads = true
        val updates = manager()
        updates.start()
        waitUntil { updates.downloadState is UpdateManager.DownloadState.Failed }
        assertEquals(3, platform.downloadAttempts)
        assertNull(updates.prompt)

        platform.failDownloads = false
        updates.retryDownload()
        waitUntil { updates.prompt == UpdateManager.Prompt.READY }
    }

    @Test
    fun clearsDownloadsOnceUpToDate() = runBlocking {
        versionNumbers = listOf(1)
        val updates = manager()
        updates.start()
        waitUntil { platform.clears == 1 }
        assertTrue(updates.newerVersions.isEmpty())
        assertNull(updates.prompt)
    }

    private suspend fun waitUntil(condition: () -> Boolean) = withTimeout(5_000) {
        while (!condition()) delay(10)
    }

    private class FakePlatform : AppUpdatePlatform {
        @Volatile var metered = false
        @Volatile var failDownloads = false
        @Volatile var downloads = 0
        @Volatile var downloadAttempts = 0
        @Volatile var installs = 0
        @Volatile var clears = 0
        private val downloaded = mutableSetOf<String>()

        override val supportsInAppUpdate = true
        override fun isMeteredNetwork() = metered

        override suspend fun findDownloaded(spec: UpdatePackageSpec): UpdatePackage? =
            synchronized(downloaded) { spec.fileName.takeIf { it in downloaded }?.let(::UpdatePackage) }

        override suspend fun download(
            spec: UpdatePackageSpec,
            onProgress: (downloaded: Long, total: Long?) -> Unit
        ): UpdatePackage {
            downloadAttempts++
            if (failDownloads) error("network down")
            onProgress(3, 3)
            downloads++
            synchronized(downloaded) { downloaded += spec.fileName }
            return UpdatePackage(spec.fileName)
        }

        override suspend fun clearDownloads() {
            clears++
            synchronized(downloaded) { downloaded.clear() }
        }

        override fun install(pkg: UpdatePackage) {
            installs++
        }
    }

    private class InMemoryDataStore : MyDataStore {
        private val values = mutableMapOf<String, Any>()

        @Suppress("UNCHECKED_CAST")
        override suspend fun <T : Any> get(key: PreferenceKey<T>): T? = synchronized(values) { values[key.name] as T? }
        override suspend fun <T : Any> set(key: PreferenceKey<T>, value: T) = synchronized(values) { values[key.name] = value }
        override suspend fun <T : Any> delete(key: PreferenceKey<T>) = synchronized(values) { values.remove(key.name); Unit }
    }
}
