package world.hachimi.app.model

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import hachimiworld.composeapp.generated.resources.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.*
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.module.VersionModule
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger
import world.hachimi.app.nav.Navigator
import world.hachimi.app.nav.Route
import world.hachimi.app.player.PlayerEngine
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferencesKeys
import world.hachimi.app.storage.SongCache
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * Global shared data and logic. Can work without UI displaying (But still requires compose snapshot runtime)
 *
 * // TODO: Decouple the logics here
 */
class GlobalStore(
    private val dataStore: MyDataStore,
    private val api: ApiClient,
    private val engine: PlayerEngine,
    songCache: SongCache
) {
    var initialized by mutableStateOf(false)
    val settings by lazy { Settings(dataStore, player) }
    val nav = Navigator(Route.Root.Home.Main)
    var isLoggedIn by mutableStateOf(false)
        private set
    var userInfo by mutableStateOf<UserInfo?>(null)
        private set
    var playerExpanded by mutableStateOf(false)
        private set
    val player = PlayerService(this, dataStore, api, engine, songCache)
    private val scope = CoroutineScope(Dispatchers.Default)
    val snackbarHostState = SnackbarHostState()

    @Serializable
    data class MusicQueueItem(
        val id: Long,
        val displayId: String,
        val name: String,
        val artist: String,
        val duration: Duration,
        val coverUrl: String,
        val explicit: Boolean?
    )

    fun initialize() = scope.launch {
        if (!initialized) {
            launch(Dispatchers.Default) {
                coroutineScope {
                    launch { settings.loadSettings() }
                    launch { loadLoginStatus() }
                    launch {
                        try {
                            player.initialize()
                        } catch (e: Throwable) {
                            Logger.e("global", "Failed to initialize player", e)
                            alert("播放器载入失败")
                        }
                    }
                }
                initialized = true
            }
        }
        launch { checkMinApiVersion() }
        launch { checkUpdate(UpdateCheckMode.AUTO) }
        startPeriodicUpdateCheck()
    }

    private var periodicCheckJob: Job? = null

    private fun startPeriodicUpdateCheck() {
        periodicCheckJob?.cancel()
        periodicCheckJob = scope.launch {
            while (true) {
                delay(24.hours)
                checkUpdate(UpdateCheckMode.SILENT)
            }
        }
    }

    private suspend fun loadLoginStatus() {
        val uid = dataStore.get(PreferencesKeys.USER_UID)
        val username = dataStore.get(PreferencesKeys.USER_NAME)
        val avatar = dataStore.get(PreferencesKeys.USER_AVATAR)
        val accessToken = dataStore.get(PreferencesKeys.AUTH_ACCESS_TOKEN)
        val refreshToken = dataStore.get(PreferencesKeys.AUTH_REFRESH_TOKEN)

        if (uid != null && username != null && accessToken != null && refreshToken != null) {
            api.setToken(accessToken, refreshToken)
            api.setAuthListener(object : AuthenticationListener {
                override suspend fun onTokenChange(accessToken: String, refreshToken: String) {
                    dataStore.set(PreferencesKeys.AUTH_ACCESS_TOKEN, accessToken)
                    dataStore.set(PreferencesKeys.AUTH_REFRESH_TOKEN, refreshToken)
                }

                override suspend fun onAuthenticationError(err: AuthError) {
                    // TODO: Should we process other errors?
                    when (err) {
                        is AuthError.RefreshTokenError -> {
                            logout()
                            alert(Res.string.auth_auth_token_invalid)
                            nav.push(Route.Auth())
                        }

                        is AuthError.ErrorHttpResponse -> {}
                        is AuthError.UnknownError -> {}
                        is AuthError.UnauthorizedDuringRequest -> {}
                    }
                }
            })
            isLoggedIn = true
            userInfo = UserInfo(uid, username, avatarUrl = avatar)
        }
    }

    fun logout() = scope.launch {
        api.setToken(null, null)
        dataStore.delete(PreferencesKeys.USER_UID)
        dataStore.delete(PreferencesKeys.USER_NAME)
        dataStore.delete(PreferencesKeys.USER_AVATAR)
        dataStore.delete(PreferencesKeys.AUTH_ACCESS_TOKEN)
        dataStore.delete(PreferencesKeys.AUTH_REFRESH_TOKEN)
        nav.replace(Route.Root.Home.Main)
        isLoggedIn = false
        userInfo = null
    }

    //    @Deprecated("Use alert with i18n instead")
    fun alert(text: String?) {
        scope.launch {
            snackbarHostState.showSnackbar(
                text?.take(64) ?: "Unknown Error",
                withDismissAction = true
            )
        }
    }

    fun alert(text: StringResource, vararg params: Any) {
        // Resolve a StringResource to a localized String and delegate to the existing alert(text: String?)
        scope.launch {
            try {
                val resolved = org.jetbrains.compose.resources.getString(text, *params)
                alert(resolved)
            } catch (e: Throwable) {
                // Fallback to a simple unknown error message if getString isn't available on the platform
                alert("Unknown Error")
            }
        }
    }

    fun expandPlayer() {
        playerExpanded = true
    }

    fun shrinkPlayer() {
        playerExpanded = false
    }

    fun setLoginUser(uid: Long, name: String, avatarUrl: String?, update: Boolean) {
        Snapshot.withMutableSnapshot {
            if (update && userInfo == null) {
                return
            }
            userInfo = UserInfo(
                uid = uid,
                name = name,
                avatarUrl = avatarUrl
            )
            isLoggedIn = true
        }
        scope.launch {
            dataStore.set(PreferencesKeys.USER_NAME, name)
            dataStore.set(PreferencesKeys.USER_UID, uid)

            avatarUrl?.let {
                dataStore.set(PreferencesKeys.USER_AVATAR, avatarUrl)
            }
        }
    }


    var showApiVersionIncompatible by mutableStateOf(false)
        private set
    var serverVersion by mutableStateOf("")
    var serverMinVersion by mutableStateOf("")
    val clientApiVersion by mutableStateOf(ApiClient.VERSION)

    private suspend fun checkMinApiVersion() {
        try {
            val resp = api.versionModule.server()
            // We assume it won't return false
            val data = resp.ok()
            serverVersion = data.version.toString()
            serverMinVersion = data.minVersion.toString()

            if (ApiClient.VERSION < data.minVersion) {
                showApiVersionIncompatible = true
            }
        } catch (e: Throwable) {
            Logger.e("player", "Failed to check min API version", e)
            alert(Res.string.global_error_check_min_api_failed)
        }
    }

    var checkingUpdate by mutableStateOf(false)
        private set
    var showUpdateDialog by mutableStateOf(false)
        private set
    var currentVersion by mutableStateOf(BuildKonfig.VERSION_NAME)
        private set
    var newVersionInfo by mutableStateOf<VersionModule.LatestVersionResp?>(null)
        private set
    var updateVersions by mutableStateOf<List<VersionModule.LatestVersionResp>>(emptyList())
        private set

    // Track dismissed version to avoid re-showing on periodic check
    private var lastDismissedVersionNumber: Int = -1

    private enum class UpdateCheckMode { AUTO, MANUAL, SILENT }

    private suspend fun checkUpdate(mode: UpdateCheckMode = UpdateCheckMode.AUTO) {
        checkingUpdate = true
        try {
            val variant = getPlatform().variant
            val resp = api.versionModule.page(
                VersionModule.PageVersionReq(
                    variant = variant,
                    pageIndex = 0,
                    pageSize = 50
                )
            )
            if (resp.ok) {
                val data = resp.ok()
                val newerVersions = data.data
                    .filter { it.versionNumber > BuildKonfig.VERSION_CODE }
                    .sortedByDescending { it.versionNumber }
                if (newerVersions.isNotEmpty()) {
                    val latestVersion = newerVersions.first()
                    updateVersions = newerVersions
                    newVersionInfo = latestVersion
                    // Show dialog unless user dismissed the same latest version (except manual check)
                    if (mode == UpdateCheckMode.MANUAL || latestVersion.versionNumber != lastDismissedVersionNumber) {
                        showUpdateDialog = true
                    }
                } else if (mode == UpdateCheckMode.MANUAL) {
                    alert(Res.string.global_already_latest_version)
                }
            } else {
                if (mode != UpdateCheckMode.SILENT) alert(resp.err().msg)
            }
        } catch (e: Throwable) {
            Logger.e("global", "Failed to check update", e)
            if (mode != UpdateCheckMode.SILENT) alert(Res.string.global_check_update_failed)
        } finally {
            checkingUpdate = false
        }
    }

    fun manualCheckUpdate() = scope.launch {
        checkUpdate(UpdateCheckMode.MANUAL)
    }

    fun dismissUpgrade() {
        showUpdateDialog = false
        lastDismissedVersionNumber = newVersionInfo?.versionNumber ?: -1
    }

    fun confirmUpgrade() {
        showUpdateDialog = false
        getPlatform().openUrl(newVersionInfo!!.url)
    }


    var showKidsDialog by mutableStateOf(false)
        private set

    private val kidsContMutex = Mutex()
    private var kidsCont: Continuation<Boolean>? = null

    suspend fun askKidsPlay(): Boolean {
        return kidsContMutex.withLock {
            showKidsDialog = true
            val result = suspendCoroutine<Boolean> { cont ->
                kidsCont = cont
            }
            kidsCont = null
            result
        }
    }

    fun confirmKidsPlay(choice: Boolean) {
        showKidsDialog = false
        kidsCont?.resume(choice)
    }
}

fun GlobalStore.MusicQueueItem.Companion.fromPublicDetail(value: SongModule.PublicSongDetail): GlobalStore.MusicQueueItem {
    return GlobalStore.MusicQueueItem(
        id = value.id,
        displayId = value.displayId,
        name = value.title,
        artist = value.uploaderName,
        duration = value.durationSeconds.seconds,
        coverUrl = value.coverUrl,
        explicit = value.explicit
    )
}

fun GlobalStore.MusicQueueItem.Companion.fromSearchSongItem(value: SongModule.SearchSongItem): GlobalStore.MusicQueueItem {
    return GlobalStore.MusicQueueItem(
        id = value.id,
        displayId = value.displayId,
        name = value.title,
        artist = value.uploaderName,
        duration = value.durationSeconds.seconds,
        coverUrl = value.coverArtUrl,
        explicit = value.explicit
    )
}