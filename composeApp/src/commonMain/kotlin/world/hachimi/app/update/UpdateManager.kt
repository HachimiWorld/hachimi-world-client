package world.hachimi.app.update

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.global_already_latest_version
import hachimiworld.composeapp.generated.resources.global_check_update_failed
import hachimiworld.composeapp.generated.resources.update_download_failed
import hachimiworld.composeapp.generated.resources.update_install_failed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.StringResource
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.VersionModule
import world.hachimi.app.api.ok
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferenceKey
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val TAG = "UpdateManager"

/**
 * Checks for new versions and, where the platform supports it, downloads them in the background.
 *
 * Flow: a newer version is found, then downloaded silently (unless auto download is off or the network is metered,
 * in which case the user is asked first), then the user is prompted once to install it. iOS and Web keep the old
 * behavior of opening the download URL.
 */
class UpdateManager(
    private val api: ApiClient,
    private val dataStore: MyDataStore,
    private val alert: (StringResource) -> Unit,
    private val alertText: (String?) -> Unit,
    private val platform: AppUpdatePlatform = createAppUpdatePlatform(),
    private val retryDelays: List<Duration> = listOf(10.seconds, 1.minutes, 5.minutes),
) {
    private object Keys {
        val AUTO_DOWNLOAD: PreferenceKey<Boolean> = PreferenceKey("update_auto_download", Boolean::class)
        val READY_PROMPTED_VERSION: PreferenceKey<Int> = PreferenceKey("update_ready_prompted_version", Int::class)
    }

    sealed interface DownloadState {
        data object Idle : DownloadState

        data class Downloading(
            val version: VersionModule.LatestVersionResp,
            val downloaded: Long,
            val total: Long?,
        ) : DownloadState {
            val progress: Float? get() = total?.takeIf { it > 0 }?.let { (downloaded.toFloat() / it).coerceIn(0f, 1f) }
        }

        data class Ready(val version: VersionModule.LatestVersionResp, val pkg: UpdatePackage) : DownloadState

        data class Failed(val version: VersionModule.LatestVersionResp) : DownloadState
    }

    enum class Prompt {
        /** A new version exists and has not been downloaded. */
        AVAILABLE,

        /** A new version has been downloaded and can be installed. */
        READY,
    }

    private enum class CheckMode { AUTO, MANUAL, SILENT }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val checkMutex = Mutex()
    private var downloadJob: Job? = null

    /** Version dismissed from the AVAILABLE prompt in this session, so periodic checks don't show it again. */
    private var dismissedVersionNumber = -1

    /** Version whose READY prompt has been shown, persisted so it's shown only once. */
    private var readyPromptedVersionNumber = -1

    val supportsInAppUpdate: Boolean get() = platform.supportsInAppUpdate

    var checking by mutableStateOf(false)
        private set

    /** Versions newer than the running one, newest first. */
    var newerVersions by mutableStateOf<List<VersionModule.LatestVersionResp>>(emptyList())
        private set

    val latestVersion: VersionModule.LatestVersionResp? get() = newerVersions.firstOrNull()

    var downloadState by mutableStateOf<DownloadState>(DownloadState.Idle)
        private set

    var prompt by mutableStateOf<Prompt?>(null)
        private set

    var autoDownload by mutableStateOf(true)
        private set

    /** Whether the AVAILABLE prompt should mention the metered network. Read when the prompt is shown. */
    fun isMeteredNetwork(): Boolean = platform.isMeteredNetwork()

    private var startJob: Job? = null

    /** Loads preferences, checks once and then every 24 hours. Calling it again has no effect. */
    fun start() {
        if (startJob != null) return
        startJob = scope.launch {
            autoDownload = dataStore.get(Keys.AUTO_DOWNLOAD) ?: true
            readyPromptedVersionNumber = dataStore.get(Keys.READY_PROMPTED_VERSION) ?: -1
            check(CheckMode.AUTO)
            while (true) {
                delay(24.hours)
                check(CheckMode.SILENT)
            }
        }
    }

    fun checkManually() = scope.launch {
        check(CheckMode.MANUAL)
    }

    private suspend fun check(mode: CheckMode) = checkMutex.withLock {
        checking = true
        try {
            val resp = api.versionModule.page(
                VersionModule.PageVersionReq(variant = getPlatform().variant, pageIndex = 0, pageSize = 50)
            )
            if (!resp.ok) {
                if (mode != CheckMode.SILENT) alertText(resp.err().msg)
                return@withLock
            }
            val newer = resp.ok().data
                .filter { it.versionNumber > BuildKonfig.VERSION_CODE }
                .sortedByDescending { it.versionNumber }
            newerVersions = newer

            val latest = newer.firstOrNull()
            if (latest == null) {
                // Up to date, so any package left over from the previous update is no longer needed
                downloadJob?.cancelAndJoin()
                downloadState = DownloadState.Idle
                platform.clearDownloads()
                if (mode == CheckMode.MANUAL) alert(Res.string.global_already_latest_version)
            } else {
                onNewerVersion(latest, userAsked = mode == CheckMode.MANUAL)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to check update", e)
            if (mode != CheckMode.SILENT) alert(Res.string.global_check_update_failed)
        } finally {
            checking = false
        }
    }

    private suspend fun onNewerVersion(latest: VersionModule.LatestVersionResp, userAsked: Boolean) {
        if (!platform.supportsInAppUpdate || latest.url.isBlank()) {
            if (userAsked || latest.versionNumber != dismissedVersionNumber) prompt = Prompt.AVAILABLE
            return
        }

        when (val state = downloadState) {
            is DownloadState.Downloading -> if (state.version.versionNumber == latest.versionNumber) return
            is DownloadState.Ready -> if (state.version.versionNumber == latest.versionNumber) {
                if (userAsked) prompt = Prompt.READY
                return
            }
            else -> {}
        }
        // A newer release supersedes a download in progress
        downloadJob?.cancelAndJoin()

        val downloaded = platform.findDownloaded(latest.toPackageSpec())
        if (downloaded != null) {
            downloadState = DownloadState.Ready(latest, downloaded)
            promptReady(latest, force = userAsked)
            return
        }

        downloadState = DownloadState.Idle
        if (autoDownload && !platform.isMeteredNetwork()) {
            launchDownload(latest, userInitiated = userAsked)
        } else if (userAsked || latest.versionNumber != dismissedVersionNumber) {
            prompt = Prompt.AVAILABLE
        }
    }

    private fun launchDownload(version: VersionModule.LatestVersionResp, userInitiated: Boolean) {
        downloadJob?.cancel()
        downloadJob = scope.launch {
            val spec = version.toPackageSpec()
            var attempt = 0
            while (true) {
                downloadState = DownloadState.Downloading(version, 0, spec.size)
                try {
                    var reported = -1L
                    val pkg = platform.download(spec) { downloaded, total ->
                        // Report about every 0.5%, enough for a progress bar without flooding recompositions
                        val step = maxOf((total ?: 0L) / 200, 256L * 1024)
                        if (reported < 0 || downloaded - reported >= step || downloaded == total) {
                            reported = downloaded
                            downloadState = DownloadState.Downloading(version, downloaded, total)
                        }
                    }
                    downloadState = DownloadState.Ready(version, pkg)
                    promptReady(version, force = userInitiated)
                    return@launch
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Logger.e(TAG, "Failed to download update ${version.versionName}, attempt ${attempt + 1}", e)
                    if (attempt >= retryDelays.size) {
                        downloadState = DownloadState.Failed(version)
                        if (userInitiated) alert(Res.string.update_download_failed)
                        return@launch
                    }
                    delay(retryDelays[attempt])
                    attempt++
                }
            }
        }
    }

    private suspend fun promptReady(version: VersionModule.LatestVersionResp, force: Boolean) {
        if (!force && readyPromptedVersionNumber == version.versionNumber) return
        prompt = Prompt.READY
        readyPromptedVersionNumber = version.versionNumber
        dataStore.set(Keys.READY_PROMPTED_VERSION, version.versionNumber)
    }

    /** Confirm button of the current prompt. */
    fun confirmPrompt() {
        when (prompt) {
            Prompt.AVAILABLE -> {
                prompt = null
                val latest = latestVersion ?: return
                if (platform.supportsInAppUpdate && latest.url.isNotBlank()) {
                    launchDownload(latest, userInitiated = true)
                } else {
                    getPlatform().openUrl(latest.url)
                }
            }

            Prompt.READY -> install()
            null -> {}
        }
    }

    /** Dismiss button of the current prompt, or the prompt was closed. */
    fun dismissPrompt() {
        if (prompt == Prompt.AVAILABLE) {
            dismissedVersionNumber = latestVersion?.versionNumber ?: -1
        }
        prompt = null
    }

    fun install() {
        prompt = null
        val state = downloadState as? DownloadState.Ready ?: return
        try {
            platform.install(state.pkg)
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to open update package ${state.pkg.path}", e)
            alert(Res.string.update_install_failed)
            getPlatform().openUrl(state.version.url)
        }
    }

    fun retryDownload() {
        val latest = latestVersion ?: return
        launchDownload(latest, userInitiated = true)
    }

    fun updateAutoDownload(enabled: Boolean) = scope.launch {
        autoDownload = enabled
        dataStore.set(Keys.AUTO_DOWNLOAD, enabled)
    }
}

private fun VersionModule.LatestVersionResp.toPackageSpec() = UpdatePackageSpec(
    url = url,
    fileName = packageFileName(url, versionNumber),
    size = size,
    sha256 = sha256?.lowercase(),
)
