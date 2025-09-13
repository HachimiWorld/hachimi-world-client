package world.hachimi.app.model

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.readBuffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import org.jetbrains.compose.resources.StringResource
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.AuthError
import world.hachimi.app.api.AuthenticationListener
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.err
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.module.VersionModule
import world.hachimi.app.api.ok
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger
import world.hachimi.app.nav.Route
import world.hachimi.app.nav.Navigator
import world.hachimi.app.player.PlayEvent
import world.hachimi.app.player.Player
import world.hachimi.app.player.SongItem
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferencesKeys
import world.hachimi.app.storage.SongCache
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Global shared data and logic. Can work without UI displaying
 */
class GlobalStore(
    private val dataStore: MyDataStore,
    private val api: ApiClient,
    private val player: Player,
    private val songCache: SongCache
) {
    var initialized by mutableStateOf(false)
    var darkMode by mutableStateOf<Boolean?>(null)
        private set
    val nav = Navigator(Route.Root.Home)
    var isLoggedIn by mutableStateOf(false)
        private set
    var userInfo by mutableStateOf<UserInfo?>(null)
        private set
    var playerExpanded by mutableStateOf(false)
        private set

    val playerState = PlayerUIState()

    private val scope = CoroutineScope(Dispatchers.Default)
    val snackbarHostState = SnackbarHostState()

    val musicQueue = mutableStateListOf<MusicQueueItem>()
    var queueCurrentIndex by mutableStateOf(-1)
    private val queueMutex = Mutex()

    data class MusicQueueItem(
        val id: Long,
        val displayId: String,
        val name: String,
        val artist: String,
        val duration: Duration,
        val coverUrl: String
    )

    fun initialize() {
        scope.launch(Dispatchers.Default) {
            coroutineScope {
                launch { this@GlobalStore.darkMode = dataStore.get(PreferencesKeys.SETTINGS_DARK_MODE) }
                launch { loadLoginStatus() }
            }
            initialized = true
        }
        scope.launch(Dispatchers.Default) {
            player.addListener(object : Player.Listener {
                override fun onEvent(event: PlayEvent) {
                    when (event) {
                        PlayEvent.End -> {
                            playerState.isPlaying = false
                            queueNext()
                        }

                        is PlayEvent.Error -> {
                            alert(event.e.message)
                        }

                        PlayEvent.Pause -> {
                            playerState.isPlaying = false
                        }

                        PlayEvent.Play -> {
                            playerState.isPlaying = true
                        }

                        is PlayEvent.Seek -> {

                        }
                    }
                }

            })
            while (isActive) {
                if (player.isPlaying()) {
                    val currentPosition = player.currentPosition()
                    playerState.updateCurrentMillis(currentPosition)
                }
                delay(100)
            }
        }
        scope.launch {
            checkMinApiVersion()
        }
        scope.launch {
            checkUpdate()
        }
    }

    fun updateDarkMode(darkMode: Boolean?) = scope.launch {
        this@GlobalStore.darkMode = darkMode
        if (darkMode == null) {
            dataStore.delete(PreferencesKeys.SETTINGS_DARK_MODE)
        } else {
            dataStore.set(PreferencesKeys.SETTINGS_DARK_MODE, darkMode)
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
                            alert("登录令牌失效，请重新登录")
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

    fun logout() {
        scope.launch {
            dataStore.delete(PreferencesKeys.USER_UID)
            dataStore.delete(PreferencesKeys.USER_NAME)
            dataStore.delete(PreferencesKeys.USER_AVATAR)
            dataStore.delete(PreferencesKeys.AUTH_ACCESS_TOKEN)
            dataStore.delete(PreferencesKeys.AUTH_REFRESH_TOKEN)
            api.setToken(null, null)
            nav.replace(Route.Root.Home)
            isLoggedIn = false
            userInfo = null
        }
    }

    @Deprecated("Use alert with i18n instead")
    fun alert(text: String?) {
        scope.launch {
            snackbarHostState.showSnackbar(text?.take(64) ?: "Unknown Error", withDismissAction = true)
        }
    }

    fun alert(text: StringResource, vararg params: Any?) {
        TODO()
    }

    fun playSongInQueue(id: String) {
        scope.launch {
            queueMutex.withLock {
                val index = musicQueue.indexOfFirst { it.displayId == id }
                if (index != -1) {
                    val song = musicQueue[index]

                    queueCurrentIndex = index
                    play(song.displayId)
                }
            }
        }
    }

    // TODO[refactor](player): Should queue be a builtin feature in player? To make GlobalStore more clear
    fun queuePrevious() {
        scope.launch {
            queueMutex.withLock {
                if (musicQueue.isNotEmpty()) {
                    val currentIndex = musicQueue.indexOfFirst { it.displayId == playerState.songDisplayId }
                    if (currentIndex == -1) {
                        playSongInQueue(musicQueue.first().displayId)
                    } else {
                        if (currentIndex == 0) {
                            playSongInQueue(musicQueue.last().displayId)
                        } else {
                            playSongInQueue(musicQueue[currentIndex - 1].displayId)
                        }
                    }
                }
            }
        }
    }

    fun queueNext() {
        scope.launch {
            queueMutex.withLock {
                if (musicQueue.isNotEmpty()) {
                    val currentIndex = musicQueue.indexOfFirst { it.displayId == playerState.songDisplayId }
                    if (currentIndex == -1) {
                        playSongInQueue(musicQueue.first().displayId)
                    } else {
                        if (currentIndex >= musicQueue.lastIndex) {
                            playSongInQueue(musicQueue.first().displayId)
                        } else {
                            playSongInQueue(musicQueue[currentIndex + 1].displayId)
                        }
                    }
                }
            }
        }
    }

    /**
     * Add song to queue
     * @param instantPlay instantly play after inserting
     * @param append appends to tail or insert after current playing
     */
    fun insertToQueue(songDisplayId: String, instantPlay: Boolean, append: Boolean) = scope.launch {
        queueMutex.withLock {
            playerState.isFetching = true
            val indexInQueue = musicQueue.indexOfFirst { it.displayId == songDisplayId }

            // Remove and reinsert
            if (indexInQueue != -1) {
                musicQueue.removeAt(indexInQueue)
            }

            val currentPlayingIndex = musicQueue.indexOfFirst { it.displayId == playerState.songDisplayId }
            try {
                val resp = api.songModule.detail(songDisplayId)
                if (resp.ok) {
                    val data = resp.okData<SongModule.DetailResp>()
                    val item = MusicQueueItem(
                        id = data.id,
                        displayId = data.displayId,
                        name = data.title,
                        artist = data.uploaderName,
                        duration = data.durationSeconds.seconds,
                        coverUrl = data.coverUrl
                    )

                    if (append) {
                        musicQueue.add(currentPlayingIndex + 1, item)
                    } else {
                        musicQueue.add(item)
                    }

                    if (instantPlay) {
                        playerState.hasSong = true
                        queueNext()
                    }
                } else {
                    val err = resp.errData<CommonError>()
                    alert(err.msg)
                }
            } catch (e: Exception) {
                Logger.e("player", "Failed to insert song to queue", e)
                alert(e.message)
            } finally {
                playerState.isFetching = false
            }
        }
    }

    fun insertToQueue(item: MusicQueueItem, instantPlay: Boolean, append: Boolean) = scope.launch {

    }

    fun playAll(items: List<MusicQueueItem>) {
        scope.launch {
            queueMutex.withLock {
                player.pause()
                musicQueue.clear()
                musicQueue.addAll(items)
                queueCurrentIndex = -1
                queueNext()
            }
        }
    }

    fun removeFromQueue(id: String) = scope.launch {
        queueMutex.withLock {
            val currentPlayingIndex = musicQueue.indexOfFirst { it.displayId == playerState.songDisplayId }
            val targetIndex = musicQueue.indexOfFirst { it.displayId == id }

            if (currentPlayingIndex == targetIndex) {
                if (musicQueue.size > 1) {
                    queueNext()
                } else {
                    player.pause()
                    playerState.hasSong = false
                }
            }
            musicQueue.removeAt(targetIndex)
        }
    }

    fun playOrPause() = scope.launch {
        if (player.isPlaying()) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun setSongProgress(progress: Float) = scope.launch {
        val millis = (progress * (playerState.songDurationSecs * 1000L)).toLong()
        player.seek(millis, true)

        // Update UI instantly
        // FIXME(player): This might be overwrite by progress syncing job
        playerState.updateCurrentMillis(millis)
    }

    fun expandPlayer() {
        playerExpanded = true
    }

    fun shrinkPlayer() {
        playerExpanded = false
    }

    fun setLoginUser(uid: Long, name: String, avatarUrl: String?) {
        // TODO: Store user here
        Snapshot.withMutableSnapshot {
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

    /**
     * This is only for play current song. Not interested in the music queue.
     */
    private suspend fun play(id: String) = coroutineScope {
        player.pause()

        playerState.isBuffering = true
        try {
            val resp = api.songModule.detail(id)
            if (resp.ok) {
                Logger.i("global", "Reading detail")
                val data = resp.okData<SongModule.DetailResp>()
                playerState.updateSongInfo(data)
                playerState.hasSong = true
                playerState.updateCurrentMillis(0L)

                val coverBytes = async<ByteArray>(Dispatchers.Default) {
                    api.httpClient.get(data.coverUrl).bodyAsBytes()
                }
                val filename = data.audioUrl.substringAfterLast("/")

                val cache = songCache.get(filename)
                val buffer = if (cache != null) {
                    val buffer = Buffer()
                    cache.use {
                        it.transferTo(buffer)
                    }
                    buffer
                } else {
                    Logger.i("global", "Downloading")
                    playerState.downloadProgress = 0f
                    val downloadResponse = api.httpClient.get(data.audioUrl)

                    val contentLength = downloadResponse.headers[HttpHeaders.ContentLength]?.toLongOrNull()
                    val buffer = if (contentLength != null) {
                        val buffer = Buffer()
                        Logger.i("global", "Has content length")
                        playerState.downloadProgress = 0.01f

                        val channel = downloadResponse.bodyAsChannel()
                        var totalBytesRead = 0L

                        while (true) {
                            val byteBuffer = ByteArray(4096)
                            val bytesRead = channel.readAvailable(byteBuffer, 0, byteBuffer.size)

                            if (bytesRead == -1) break

                            totalBytesRead += bytesRead
                            buffer.write(byteBuffer, 0, bytesRead)
                            val progress = totalBytesRead.toDouble() / contentLength.toDouble()
                            playerState.downloadProgress = progress.toFloat()
                        }
                        buffer
                    } else {
                        // Oh, copy occurs here
                        downloadResponse.bodyAsChannel().readBuffer()
                    }

                    songCache.save(buffer, filename)
                    buffer
                }
                player.prepare(SongItem(
                    id = data.id.toString(),
                    title = data.title,
                    artist = data.uploaderName,
                    audioBytes = buffer.readByteArray(),
                    coverBytes = coverBytes.await(),
                ), autoPlay = true)

                // Touch playing
                if (isLoggedIn) {
                    api.playHistoryModule.touch(data.id)
                } else {
                    api.playHistoryModule.touchAnonymous(data.id)
                }
            } else {
                alert(resp.errData<CommonError>().msg)
                return@coroutineScope
            }
        } catch (e: Exception) {
            Logger.e("player", "Failed to play song", e)
            alert(e.message)
            return@coroutineScope
        } finally {
            playerState.isBuffering = false
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
        } catch (e: Exception) {
            Logger.e("player", "Failed to check min API version", e)
            alert("Failed to check min API version")
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
    private suspend fun checkUpdate() {
        checkingUpdate = true
        try {
            val variant = getPlatform().variant
            val resp = api.versionModule.latest(VersionModule.LatestVersionReq(variant))
            if (resp.ok) {
                val data = resp.ok()
                if (data != null && data.versionNumber > BuildKonfig.VERSION_CODE) {
                    newVersionInfo = data
                    showUpdateDialog = true
                }
            } else {
                alert(resp.err().msg)
            }
        } catch (e: Exception) {
            Logger.e("global", "Failed to check update", e)
            alert("检查更新失败")
        } finally {
            checkingUpdate = false
        }
    }

    fun dismissUpgrade() {
        // TODO: ignore this version anymore
        showUpdateDialog = false
    }

    fun confirmUpgrade() {
        showUpdateDialog = false
        getPlatform().openUrl(newVersionInfo!!.url)
    }
}