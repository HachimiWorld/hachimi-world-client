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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
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
import kotlin.concurrent.Volatile
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val TAG = "UpdateManager"

/**
 * Checks for new versions and, where the platform supports it, downloads them in the background.
 *
 * A newer version shows up as a non-blocking [notice] (a card on the side rail, a banner on phones) that follows the
 * download: progress while downloading, then an install action. The package is downloaded without asking unless auto
 * download is off or the network is metered. Dialogs ([prompt]) only open for a manual check or when the user asks
 * for the details. iOS and Web keep opening the download URL.
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

    /** What the non-blocking update notice shows. */
    sealed interface Notice {
        val version: VersionModule.LatestVersionResp

        /** Waiting for the user to start the download, or on iOS and Web to open the download page. */
        data class Available(override val version: VersionModule.LatestVersionResp) : Notice

        /** @property total package size in bytes, null when unknown */
        data class Downloading(
            override val version: VersionModule.LatestVersionResp,
            val downloaded: Long,
            val total: Long?,
        ) : Notice {
            /** Fraction in 0..1, null when the size is unknown. */
            val progress: Float? get() = total?.takeIf { it > 0 }?.let { (downloaded.toFloat() / it).coerceIn(0f, 1f) }
        }

        data class Ready(override val version: VersionModule.LatestVersionResp) : Notice

        data class Failed(override val version: VersionModule.LatestVersionResp) : Notice
    }

    /** Dialog showing the changelog with the next action. */
    enum class Prompt {
        /** A new version exists and has not been downloaded. */
        AVAILABLE,

        /** A new version has been downloaded and can be installed. */
        READY,

        /** The download failed; offers a retry and the official website. */
        FAILED,
    }

    private enum class CheckMode { AUTO, MANUAL, SILENT }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val checkMutex = Mutex()
    private var downloadJob: Job? = null
    private var startJob: Job? = null

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

    /** Version whose notice was closed. It shows again on the next launch or manual check. */
    private var noticeDismissedVersionNumber by mutableStateOf(-1)

    /** The non-blocking notice to show, null when there is nothing to show or it was closed. */
    val notice: Notice?
        get() {
            val latest = latestVersion ?: return null
            if (latest.versionNumber == noticeDismissedVersionNumber) return null
            return when (val state = downloadState) {
                is DownloadState.Downloading -> Notice.Downloading(state.version, state.downloaded, state.total)
                is DownloadState.Ready -> Notice.Ready(state.version)
                is DownloadState.Failed -> Notice.Failed(state.version)
                DownloadState.Idle -> Notice.Available(latest)
            }
        }

    /** Whether the AVAILABLE prompt should mention the metered network. Read when the prompt is shown. */
    fun isMeteredNetwork(): Boolean = platform.isMeteredNetwork()

    /** Loads preferences, checks once and then every 24 hours. Calling it again has no effect. */
    fun start() {
        if (startJob != null) return
        startJob = scope.launch {
            autoDownload = dataStore.get(Keys.AUTO_DOWNLOAD) ?: true
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

            val latest = newer.firstOrNull()
            if (latest == null) {
                // Up to date, so any package left over from the previous update is no longer needed
                downloadJob?.cancelAndJoin()
                downloadState = DownloadState.Idle
                newerVersions = emptyList()
                platform.clearDownloads()
                if (mode == CheckMode.MANUAL) alert(Res.string.global_already_latest_version)
            } else {
                val userAsked = mode == CheckMode.MANUAL
                // Settle the download state first so the notice doesn't flash "available" before "downloading"
                onNewerVersion(latest, userAsked)
                newerVersions = newer
                if (userAsked) noticeDismissedVersionNumber = -1
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
            downloadState = DownloadState.Idle
            if (userAsked) prompt = Prompt.AVAILABLE
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
            if (userAsked) prompt = Prompt.READY
        } else if (autoDownload && !platform.isMeteredNetwork()) {
            launchDownload(latest, userInitiated = userAsked)
        } else {
            downloadState = DownloadState.Idle
            if (userAsked) prompt = Prompt.AVAILABLE
        }
    }

    private fun launchDownload(version: VersionModule.LatestVersionResp, userInitiated: Boolean) {
        downloadJob?.cancel()
        val spec = version.toPackageSpec()
        downloadState = DownloadState.Downloading(version, 0, spec.size)
        downloadJob = scope.launch {
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

    /** Starts (or retries) the download, or opens the download page where packages can't be installed in the app. */
    fun download() {
        prompt = null
        val latest = latestVersion ?: return
        if (downloadState is DownloadState.Downloading) return
        if (platform.supportsInAppUpdate && latest.url.isNotBlank()) {
            launchDownload(latest, userInitiated = true)
        } else {
            getPlatform().openUrl(latest.url)
        }
    }

    /** Whether a ready package is installed when the app quits, so "later" still updates (Windows). */
    val installsOnExit: Boolean get() = platform.installsOnExit

    // A channel keeps the request until the entry point collects it, unlike a SharedFlow without subscribers
    private val exitChannel = Channel<Unit>(Channel.CONFLATED)

    /** Emits when the app should quit so the installer can replace it. Collected by the desktop entry point. */
    val exitRequests: Flow<Unit> = exitChannel.receiveAsFlow()

    /** Set once an installer that runs after exit has been started, so quitting doesn't start a second one. */
    @Volatile
    private var installerStarted = false

    fun install() {
        prompt = null
        val state = downloadState as? DownloadState.Ready ?: return
        scope.launch {
            try {
                when (platform.install(state.pkg)) {
                    InstallAction.HANDLED -> {}
                    InstallAction.EXIT_APP -> {
                        installerStarted = true
                        exitChannel.send(Unit)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to install update package ${state.pkg.path}", e)
                alert(Res.string.update_install_failed)
                getPlatform().openUrl(state.version.url)
            }
        }
    }

    /**
     * Call as the app quits normally. Installs a downloaded package quietly where the platform supports it,
     * so an update the user put off still lands before the next launch.
     */
    fun onAppExit() {
        if (installerStarted) return
        val state = downloadState as? DownloadState.Ready ?: return
        try {
            if (platform.installOnExit(state.pkg)) installerStarted = true
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to start installing update on exit", e)
        }
    }

    /** Opens the changelog dialog for the latest version. */
    fun showDetails() {
        if (latestVersion == null) return
        prompt = when (downloadState) {
            is DownloadState.Ready -> Prompt.READY
            is DownloadState.Failed -> Prompt.FAILED
            else -> Prompt.AVAILABLE
        }
    }

    /** Confirm button of the current prompt. */
    fun confirmPrompt() {
        when (prompt) {
            Prompt.AVAILABLE, Prompt.FAILED -> download()
            Prompt.READY -> install()
            null -> {}
        }
    }

    /** Opens the download section of the official website, for when the in-app download keeps failing. */
    fun openDownloadPage() {
        prompt = null
        getPlatform().openUrl(OFFICIAL_DOWNLOAD_PAGE)
    }

    /** Dismiss button of the current prompt, or the prompt was closed. */
    fun dismissPrompt() {
        prompt = null
    }

    /** Hides the notice for the latest version until the next launch or manual check. The download continues. */
    fun dismissNotice() {
        noticeDismissedVersionNumber = latestVersion?.versionNumber ?: return
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
