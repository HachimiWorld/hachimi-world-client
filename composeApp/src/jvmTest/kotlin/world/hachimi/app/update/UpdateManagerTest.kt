package world.hachimi.app.update

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
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
    fun downloadsInTheBackgroundWithoutDialogs() = runBlocking {
        val first = manager()
        first.start()
        waitUntil { first.downloadState is UpdateManager.DownloadState.Ready }
        assertIs<UpdateManager.Notice.Ready>(first.notice)
        assertNull(first.prompt)
        assertEquals(1, platform.downloads)

        // Next launch finds the package already downloaded
        val second = manager()
        second.start()
        waitUntil { second.notice is UpdateManager.Notice.Ready }
        assertNull(second.prompt)
        assertEquals(1, platform.downloads)

        // A manual check answers with a dialog
        second.checkManually()
        waitUntil { second.prompt == UpdateManager.Prompt.READY }

        second.install()
        waitUntil { platform.installs == 1 }
    }

    @Test
    fun noticeNeverShowsAvailableBeforeTheDownloadStarts() = runBlocking {
        val updates = manager()
        val seen = mutableSetOf<String>()
        val watcher = launch(Dispatchers.Default) {
            while (true) {
                updates.notice?.let { seen += it::class.simpleName!! }
                yield()
            }
        }
        updates.start()
        waitUntil { updates.notice is UpdateManager.Notice.Ready }
        watcher.cancel()
        assertTrue("Available" !in seen, "notice went through $seen")
    }

    @Test
    fun dismissedNoticeComesBackOnManualCheck() = runBlocking {
        val updates = manager()
        updates.start()
        waitUntil { updates.notice is UpdateManager.Notice.Ready }

        updates.dismissNotice()
        assertNull(updates.notice)
        assertIs<UpdateManager.DownloadState.Ready>(updates.downloadState)

        updates.checkManually()
        waitUntil { updates.notice is UpdateManager.Notice.Ready }
    }

    @Test
    fun quitsForAnInstallerThatRunsAfterExit() = runBlocking {
        platform.installAction = InstallAction.EXIT_APP
        platform.installsOnExitSupported = true
        val updates = manager()
        updates.start()
        waitUntil { updates.notice is UpdateManager.Notice.Ready }

        val exitRequested = async { updates.exitRequests.first() }
        updates.install()
        withTimeout(5_000) { exitRequested.await() }

        // The installer already runs, so quitting must not start another one
        updates.onAppExit()
        assertEquals(0, platform.exitInstalls)
    }

    @Test
    fun installsAPutOffUpdateOnExit() = runBlocking {
        platform.installsOnExitSupported = true
        val updates = manager()
        updates.onAppExit()
        assertEquals(0, platform.exitInstalls, "nothing downloaded yet")

        updates.start()
        waitUntil { updates.notice is UpdateManager.Notice.Ready }
        updates.onAppExit()
        assertEquals(1, platform.exitInstalls)
        assertEquals(0, platform.installs)
    }

    @Test
    fun asksBeforeDownloadingOnAMeteredNetwork() = runBlocking {
        platform.metered = true
        val updates = manager()
        updates.start()
        waitUntil { updates.notice is UpdateManager.Notice.Available }
        assertNull(updates.prompt)
        assertEquals(0, platform.downloads)

        updates.download()
        waitUntil { updates.notice is UpdateManager.Notice.Ready }
        assertEquals(1, platform.downloads)
    }

    @Test
    fun asksBeforeDownloadingWhenAutoDownloadIsOff() = runBlocking {
        val setup = manager()
        setup.updateAutoDownload(false).join()

        val updates = manager()
        updates.start()
        waitUntil { updates.notice is UpdateManager.Notice.Available }
        assertEquals(0, platform.downloads)
    }

    @Test
    fun retriesAndThenReportsFailure() = runBlocking {
        platform.failDownloads = true
        val updates = manager()
        updates.start()
        waitUntil { updates.notice is UpdateManager.Notice.Failed }
        assertEquals(3, platform.downloadAttempts)
        assertNull(updates.prompt)

        // Tapping the notice offers a retry and the website
        updates.showDetails()
        assertEquals(UpdateManager.Prompt.FAILED, updates.prompt)

        platform.failDownloads = false
        updates.confirmPrompt()
        assertNull(updates.prompt)
        waitUntil { updates.notice is UpdateManager.Notice.Ready }
    }

    @Test
    fun clearsDownloadsOnceUpToDate() = runBlocking {
        versionNumbers = listOf(1)
        val updates = manager()
        updates.start()
        waitUntil { platform.clears == 1 }
        assertTrue(updates.newerVersions.isEmpty())
        assertNull(updates.notice)
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

        @Volatile var installAction = InstallAction.HANDLED
        @Volatile var installsOnExitSupported = false
        @Volatile var exitInstalls = 0

        override val installsOnExit: Boolean get() = installsOnExitSupported

        override suspend fun install(pkg: UpdatePackage): InstallAction {
            installs++
            return installAction
        }

        override fun installOnExit(pkg: UpdatePackage): Boolean {
            if (!installsOnExitSupported) return false
            exitInstalls++
            return true
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
