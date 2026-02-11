package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.player_play_failed
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readBuffer
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.api.ok
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger
import world.hachimi.app.model.GlobalStore.MusicQueueItem
import world.hachimi.app.player.PlayEvent
import world.hachimi.app.player.Player
import world.hachimi.app.player.SongItem
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferencesKeys
import world.hachimi.app.storage.SongCache
import kotlin.random.Random

private val downloadHttpClient = HttpClient() {
    install(HttpTimeout) {
        connectTimeoutMillis = 60_000
        requestTimeoutMillis = 60_000
        socketTimeoutMillis = 60_000
    }
    install(UserAgent) {
        agent = getPlatform().userAgent
    }
}

private const val TAG = "PlayerService"

/**
 * TODO(player): There are too fucking many racing conditions here. I think I should totally rewrite this.
 */
class PlayerService(
    private val global: GlobalStore,
    private val dataStore: MyDataStore,
    private val api: ApiClient,
    private val player: Player,
    private val songCache: SongCache
) {
    val playerState = PlayerUIState()
    var musicQueue by mutableStateOf<List<MusicQueueItem>>(emptyList())
        private set
    private var shuffledQueue = emptyList<MusicQueueItem>()
    private var shuffleIndex = -1

    var shuffleMode by mutableStateOf(false)
    var repeatMode by mutableStateOf(false)
    private val queueMutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default)
    private val profileCache = mutableMapOf<Long, UserModule.PublicUserProfile?>()

    init {
        player.addListener(object : Player.Listener {
            override fun onEvent(event: PlayEvent) {
                when (event) {
                    PlayEvent.End -> {
                        playerState.isPlaying = false
                        autoNext()
                    }

                    is PlayEvent.Error -> {
                        global.alert(event.e.message)
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

        scope.launch {
            player.initialize()
            Logger.i(TAG, "Inner player initialized")
            restorePlayerState()

            // Apply initial replay gain toggle immediately for the current runtime.
            player.setReplayGainEnabled(global.enableLoudnessNormalization)

            startSyncingJob()
        }
    }

    private fun startSyncingJob() {
        scope.launch(Dispatchers.Default) {
            while (isActive) {
                if (player.isPlaying()) {
                    val currentPosition = player.currentPosition()
                    playerState.updateCurrentMillis(currentPosition)
                }
                playerState.isPlaying = player.isPlaying()
                if (player.supportRemotePlay && player.isPlaying()) {
                    playerState.downloadProgress = player.bufferedProgress()
                }
                delay(100)
            }
        }
    }

    private var playerMutex = Mutex()
    private var playerJobSign: Int = 0

    /**
     * This is only for play current song. Not interested in the music queue.
     */
    private suspend fun play(item: MusicQueueItem, instantPlay: Boolean, sign: Int) = coroutineScope {
        player.stop()
        playerMutex.withLock {
            playerJobSign = sign
        }

        playerState.downloadProgress = 0f
        playerState.updatePreviewMetadata(
            PlayerUIState.PreviewMetadata(
                id = item.id,
                displayId = item.displayId,
                title = item.name,
                author = item.artist,
                coverUrl = item.coverUrl,
                duration = item.duration,
                explicit = item.explicit
            )
        )
        playerState.fetchingMetadata = true

        val item = getSongItemCacheable(
            songId = item.id,
            jmid = item.displayId,
            onMetadata = { songInfo ->
                playerMutex.withLock {
                    val authorProfile = profileCache[songInfo.uploaderUid]

                    if (playerJobSign == sign) {
                        playerState.updateSongInfo(songInfo)
                        playerState.fetchingMetadata = false
                        playerState.hasSong = true
                        playerState.updateCurrentMillis(0L)
                        playerState.updateAuthorProfile(authorProfile)
                    }
                    if (authorProfile == null) {
                        loadAuthorProfileAsync(sign, songInfo.uploaderUid)
                    }
                }
            },
            onProgress = { progress ->
                playerMutex.withLock {
                    if (playerJobSign == sign) {
                        playerState.buffering = true
                        playerState.downloadProgress = progress
                    }
                }
            }
        )

        if (playerJobSign == sign) {
            player.prepare(item, autoPlay = instantPlay)
        }

        // Touch in the global scope
        scope.launch {
            try {
                val songId = item.id.toLong()
                // Touch playing
                if (global.isLoggedIn) {
                    api.playHistoryModule.touch(songId)
                } else {
                    api.playHistoryModule.touchAnonymous(songId)
                }
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to touch song", e)
            }
        }
    }

    private suspend fun playWithRetry(maxAttempts: Int = 5, item: MusicQueueItem, instantPlay: Boolean) {
        var attempt = 0
        var delay = 1000L // Start with 1 second delay
        var lastError: Throwable? = null
        val sign = Random.nextInt()

        try {
            while (attempt < maxAttempts) {
                try {
                    play(item, instantPlay, sign)
                    return
                } catch (_: CancellationException) {
                    // It's canceled, do not retry anymore
                    Logger.i(TAG, "Preparing cancelled")
                    return
                } catch (e: Throwable) {
                    lastError = e
                    attempt++
                    if (attempt == maxAttempts) {
                        break
                    }
                    // Add jitter by randomizing delay by ±30%
                    val jitter = (delay * 0.7 + Random.nextFloat() * (delay * 0.6)).toLong()
                    delay *= 2 // Exponential backoff
                    Logger.w("player", "Retry attempt $attempt after ${jitter}ms delay", e)
                    delay(jitter)
                }
            }
        } finally {
            playerMutex.withLock {
                if (playerJobSign == sign) {
                    playerState.fetchingMetadata = false
                    playerState.buffering = false
                }
            }
        }

        Logger.e(TAG, "Failed to play song", lastError)
        global.alert(Res.string.player_play_failed, lastError?.message ?: "")
    }

    private var playPrepareJob: Job? = null

    private fun loadAuthorProfileAsync(sign: Int, uid: Long) {
        scope.launch {
            try {
                val resp = api.userModule.profile(uid)

                if (resp.ok) {
                    val data = resp.ok()
                    profileCache[uid] = data
                    playerMutex.withLock {
                        if (playerJobSign == sign) {
                            playerState.updateAuthorProfile(data)
                        }
                    }
                }
            } catch (_: CancellationException) {
                return@launch
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to load author profile", e)
                return@launch
            }
        }
    }

    fun playSongInQueue(id: Long, instantPlay: Boolean = true) = scope.launch {
        if (playPrepareJob?.isActive == true) {
            // We don't use cancelAndJoin because we want the operation to be instant
            Logger.d(TAG, "Cancel prepare job")
            playPrepareJob?.cancel()
        }
        val song = queueMutex.withLock {
            val index = musicQueue.indexOfFirst { it.id == id }
            if (index != -1) {
                musicQueue[index]
            } else {
                null
            }
        }
        if (song != null) {
            playPrepareJob = scope.launch {
                Logger.d(TAG, "playSongInQueue: Playing song $song")
                playWithRetry(5, song, instantPlay)
                savePlayerState()
            }
        }
    }

    // TODO[refactor](player): Should queue be a builtin feature in player? To make GlobalStore more clear
    fun queuePrevious() = scope.launch {
        val targetSong = queueMutex.withLock {
            if (musicQueue.isNotEmpty()) {
                val currentSongId =
                    if (playerState.fetchingMetadata) playerState.previewMetadata?.id else playerState.songInfo?.id

                val currentIndex = musicQueue.indexOfFirst {
                    it.id == currentSongId
                }
                val targetIdx = when {
                    currentIndex == -1 -> 0 // First
                    currentIndex == 0 -> musicQueue.lastIndex // Ring
                    else -> currentIndex - 1 // Previous
                }
                val targetSong = musicQueue[targetIdx]
                return@withLock targetSong
            }
            return@withLock null
        }
        targetSong?.let {
            playSongInQueue(it.id)
        }
    }

    fun queueNext() = scope.launch {
        val targetSong = queueMutex.withLock {
            if (musicQueue.isNotEmpty()) {
                // Get current song index (including the fetching/buffering)
                val currentSongId =
                    if (playerState.fetchingMetadata) playerState.previewMetadata?.id else playerState.songInfo?.id

                val currentIndex = musicQueue.indexOfFirst {
                    it.id == currentSongId
                }
                val targetIdx = when {
                    currentIndex == -1 -> 0 // First
                    currentIndex >= musicQueue.lastIndex -> 0 // Ring
                    else -> currentIndex + 1 // Next
                }
                val targetSong = musicQueue[targetIdx]
                return@withLock targetSong
            }
            return@withLock null
        }

        targetSong?.let {
            playSongInQueue(it.id)
        }
    }

    private var fetchMetadataJob: Job? = null

    fun insertToQueueWithFetch(
        songDisplayId: String,
        instantPlay: Boolean,
        append: Boolean
    ) {
        if (fetchMetadataJob?.isActive == true) {
            fetchMetadataJob?.cancel()
        }

        fetchMetadataJob = scope.launch {
            playerState.fetchingMetadata = true
            try {
                val resp = api.songModule.detail(songDisplayId)
                val data = if (resp.ok) {
                    val data = resp.ok<SongModule.PublicSongDetail>()
                    songCache.saveMetadata(data)
                    data
                } else {
                    val err = resp.err()
                    global.alert(err.msg)
                    return@launch
                }

                val item = MusicQueueItem.fromPublicDetail(data)
                insertToQueue(item, instantPlay, append).join()
            } catch (e: CancellationException) {
                // Do nothing, it's just cancelled
                Logger.i(TAG, "Cancelled")
            } catch (e: Throwable) {
                global.alert(e.message)
                Logger.e(TAG, "Failed to insert song to music queue", e)
            } finally {
                fetchMetadataJob = null
                playerState.fetchingMetadata = false
            }
        }
    }

    /**
     * Add song to queue
     * @param instantPlay instantly play after inserting
     * @param append appends to tail or insert after current playing
     */
    fun insertToQueue(
        item: MusicQueueItem,
        instantPlay: Boolean,
        append: Boolean
    ) = scope.launch {
        if (item.explicit == true && global.kidsMode) {
            if (!global.askKidsPlay()) {
                return@launch
            }
        }

        queueMutex.withLock {
            val indexInQueue = musicQueue.indexOfFirst { it.id == item.id }

            if (indexInQueue != -1) {
                // If the music was already in the queue, just play it
                // Or we can reorder it after
            } else {
                // Add to queue
                val currentPlayingIndex = musicQueue.indexOfFirst { it.id == playerState.songInfo?.id }

                val queue = musicQueue.toMutableList()
                val shuffledQueue = shuffledQueue.toMutableList()

                if (append) {
                    // Append to tail
                    queue.add(item)
                    // Randomly add to shuffled queue
                    shuffledQueue.add(Random.nextInt(shuffleIndex, shuffledQueue.size + 1), item)
                } else {
                    // Insert to next
                    queue.add(currentPlayingIndex + 1, item)
                    shuffledQueue.add(shuffleIndex + 1, item)
                }
                musicQueue = queue
                this@PlayerService.shuffledQueue = shuffledQueue
            }
        }

        if (instantPlay) {
            playerState.hasSong = true
            playSongInQueue(item.id)
        }
    }

    fun playAll(items: List<MusicQueueItem>, filterExplicit: Boolean = global.kidsMode) = scope.launch {
        replaceQueue(if (filterExplicit) items.filter { it.explicit != true } else items)
        next()
    }

    suspend fun replaceQueue(items: List<MusicQueueItem>) {
        queueMutex.withLock {
            player.stop()
            musicQueue = items
            shuffledQueue = items.shuffled()
            shuffleIndex = -1
        }
    }

    fun removeFromQueue(id: Long) = scope.launch {
        queueMutex.withLock {
            val currentPlayingIndex = musicQueue.indexOfFirst { it.id == playerState.songInfo?.id }
            val targetIndex = musicQueue.indexOfFirst { it.id == id }

            if (currentPlayingIndex == targetIndex) {
                if (musicQueue.size > 1) {
                    queueNext()
                } else {
                    player.stop()
                    playerState.clear()
                }
            }
            musicQueue = musicQueue.toMutableList().apply {
                removeAt(targetIndex)
            }.toList()
            shuffledQueue = shuffledQueue.toMutableList().apply {
                removeAll { it.id == id }
            }.toList()
        }
    }

    fun playOrPause() = scope.launch {
        // TODO: Redownload, if the song download failed
        if (!playerState.fetchingMetadata && !playerState.buffering) {
            if (player.isPlaying()) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    fun setSongProgress(progress: Float) = scope.launch {
        if (!playerState.fetchingMetadata && !playerState.buffering) {
            playerState.songInfo?.let { songInfo ->
                val millis = (progress * (songInfo.durationSeconds * 1000L)).toLong()
                player.seek(millis, true)

                // Update UI instantly
                // FIXME(player): This might be overwrite by progress syncing job
                playerState.updateCurrentMillis(millis)
            }
        }
    }

    fun updateVolume(volume: Float) = scope.launch {
        playerState.volume = volume
        player.setVolume(volume)
        dataStore.set(PreferencesKeys.PLAYER_VOLUME, volume)
    }

    private suspend fun getSongItemCacheable(
        songId: Long,
        jmid: String,
        onMetadata: suspend (SongDetailInfo) -> Unit,
        onProgress: suspend (Float) -> Unit
    ): SongItem = coroutineScope {
        val cacheFromId = songCache.get(songId.toString())
        val cacheFromJmid = songCache.get(jmid)
        val cache = cacheFromId ?: cacheFromJmid
        val metadata: SongDetailInfo

        if (cache != null) {
            Logger.i(TAG, "Cache hit")
            val buffer = Buffer()
            metadata = cache.metadata
            onMetadata(cache.metadata)
            onProgress(1f)
            cache.audio.use {
                it.transferTo(buffer)
            }
            val coverBytes: ByteArray = cache.cover.readByteArray()
            val audioBytes: ByteArray = buffer.readByteArray()

            if (cacheFromId == null && cacheFromJmid != null) {
                // Migrate to id
                Logger.i(TAG, "Migrating cache from jmid to id")
                songCache.save(SongCache.Item(
                    key = songId.toString(),
                    metadata = metadata,
                    audio = Buffer().also { it.write(audioBytes) },
                    cover = Buffer().also { it.write(coverBytes) }
                ))
                songCache.delete(jmid)
            }

            // Get the latest data and update the cache
            fetchCacheAsync(songId, cache, jmid)

            val filename = metadata.audioUrl.substringAfterLast("/")
            val extension = filename.substringAfterLast(".")
            val item = SongItem.Local(
                id = metadata.id.toString(),
                title = metadata.title,
                artist = metadata.uploaderName,
                audioBytes = audioBytes,
                coverBytes = coverBytes,
                format = extension,
                durationSeconds = metadata.durationSeconds,
                replayGainDB = metadata.gain ?: 0f
            )
            return@coroutineScope item
        } else {
            Logger.i(TAG, "Cache not hit, getting metadata")

            onProgress(0f)
            val metadata = songCache.getMetadata(songId.toString()) ?: run {
                val resp = api.songModule.detailById(songId)
                if (!resp.ok) error(resp.err().msg)
                resp.ok()
            }
            onMetadata(metadata)

            val filename = metadata.audioUrl.substringAfterLast("/")
            val extension = filename.substringAfterLast(".")

            if (player.supportRemotePlay) {
                Logger.i(TAG, "Remote play")

                val item = SongItem.Remote(
                    id = metadata.id.toString(),
                    title = metadata.title,
                    artist = metadata.uploaderName,
                    audioUrl = metadata.audioUrl,
                    coverUrl = metadata.coverUrl,
                    format = extension,
                    durationSeconds = metadata.durationSeconds,
                    replayGainDB = metadata.gain ?: 0f
                )

                // TODO: Reuse the cached data from player core
                cacheInBackground(metadata)
                return@coroutineScope item
            } else {
                Logger.i(TAG, "Starting full download")

                val coverBytesAsync = async { api.httpClient.get(metadata.coverUrl).bodyAsBytes() }
                val audioBuffer = downloadAudio(metadata.audioUrl, onProgress)
                val coverBytes = coverBytesAsync.await()

                val cacheItem = SongCache.Item(
                    key = songId.toString(),
                    metadata = metadata,
                    audio = audioBuffer.copy(),
                    cover = Buffer().also { it.write(coverBytes) }
                )
                songCache.save(cacheItem)
                Logger.i(TAG, "Full download completed")

                val item = SongItem.Local(
                    id = metadata.id.toString(),
                    title = metadata.title,
                    artist = metadata.uploaderName,
                    audioBytes = audioBuffer.readByteArray(),
                    coverBytes = coverBytes,
                    format = extension,
                    durationSeconds = metadata.durationSeconds,
                    replayGainDB = metadata.gain ?: 0f
                )
                return@coroutineScope item
            }
        }
    }

    private suspend fun downloadAudio(url: String, onProgress: suspend (Float) -> Unit): Buffer {
        // Do not use HttpCache plugin because it will affect the progress (Bugs)
        val statement = downloadHttpClient.prepareGet(url)

        // FIXME(wasm)(player): Due to the bugs of ktor client, we can't get the content length header in wasm target
        //  KTOR-8377 JS/WASM: response doesn't contain the Content-Length header in a browser
        //  https://youtrack.jetbrains.com/issue/KTOR-8377/JS-WASM-response-doesnt-contain-the-Content-Length-header-in-a-browser
        //  KTOR-7934 JS/WASM fails with "IllegalStateException: Content-Length mismatch" on requesting gzipped content
        //  https://youtrack.jetbrains.com/issue/KTOR-7934/JS-WASM-fails-with-IllegalStateException-Content-Length-mismatch-on-requesting-gzipped-content
        var headContentLength: Long? = null
        // Workaround for wasm, we can use HEAD request to get the content length
        if (getPlatform().name.startsWith("Web")) {
            val resp = api.httpClient.head(url)
            headContentLength = resp.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: 0L
            Logger.i(TAG, "Head content length: $headContentLength bytes")
        }
        val buffer = statement.execute { resp ->
            val contentLength = resp.headers[HttpHeaders.ContentLength]?.toLongOrNull()
            Logger.i(TAG, "Content length: $contentLength bytes")

            val bestContentLength = contentLength ?: headContentLength
            val channel = resp.body<ByteReadChannel>()

            val buffer = if (bestContentLength != null) {
                val buffer = Buffer()
                var count = 0L
                while (!channel.exhausted()) {
                    val chunk = channel.readRemaining(1024 * 8)
                    count += chunk.transferTo(buffer)
                    val progress = count.toFloat() / bestContentLength
                    onProgress(progress.coerceIn(0f, 1f))
                }
                buffer
            } else {
                Logger.i(TAG, "Content-Length not found, progress is disabled")
                channel.readBuffer()
            }

            buffer
        }
        return buffer
    }

    private var cacheQueueMutex = Mutex()
    private var cacheQueue = ArrayDeque<Job>()
    private suspend fun cacheInBackground(metadata: SongModule.PublicSongDetail) {
        cacheQueueMutex.withLock {
            if (cacheQueue.size >= 3) {
                try {
                    cacheQueue.removeFirstOrNull()?.cancel()
                    Logger.d(TAG, "Cancelled background downloading task")
                } catch (e: Throwable) {
                    Logger.w(TAG, "Failed to cancel background downloading task", e)
                }
            }
        }

        val job = scope.launch {
            try {
                Logger.d(TAG, "Caching ${metadata.id} in background")
                val audioBuffer = downloadAudio(metadata.audioUrl, onProgress = {
                    // Do nothing
                })
                val coverBytes = api.httpClient.get(metadata.coverUrl).bodyAsBytes()

                val cacheItem = SongCache.Item(
                    key = metadata.id.toString(),
                    metadata = metadata,
                    audio = audioBuffer,
                    cover = Buffer().also { it.write(coverBytes) }
                )
                songCache.save(cacheItem)
                Logger.d(TAG, "Caching ${metadata.id} completed")
            } catch (e: CancellationException) {
                Logger.w(TAG, "Caching ${metadata.id} cancelled")
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to cache ${metadata.id}", e)
            }
        }

        cacheQueueMutex.withLock {
            cacheQueue.addLast(job)
        }
    }

    private fun fetchCacheAsync(songId: Long, cache: SongCache.Item, jmid: String) = scope.launch {
        try {
            Logger.i(TAG, "Fetching cache")
            val resp = api.songModule.detailById(songId)
            if (!resp.ok) {
                Logger.e(TAG, "Failed to refresh song metadata: ${resp.err().msg}")
                return@launch
            }

            val data = resp.ok()
            if (cache.metadata != data) {
                Logger.i(TAG, "Metadata updated, invalidate cache")
                // If the audio file has updated, just invalidate the cache
                if (cache.metadata.audioUrl != data.audioUrl || cache.metadata.coverUrl != data.coverUrl) {
                    songCache.delete(songId.toString())
                    songCache.delete(jmid)
                }
                // Update the metadata
                songCache.saveMetadata(data)
            } else {
                Logger.i(TAG, "Metadata already up to date")
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to refresh song metadata", e)
        }
    }

    fun previous() = scope.launch {
        if (shuffleMode) {
            queueMutex.withLock {
                val index = if (shuffleIndex <= 0) {
                    shuffledQueue.lastIndex
                } else {
                    shuffleIndex - 1
                }
                val song = shuffledQueue[index]
                shuffleIndex = index
                playSongInQueue(song.id)
            }
            /*// Play the previously played song, (not a song in music queue)
            val index = if (historyCursor > 0) {
                playHistory.lastIndex
            } else {
                historyCursor
            }
            val previousSong = playHistory[index]
            historyCursor = index

            playSongInQueue(previousSong.songId)*/
        } else {
            // Play previous song in the queue
            queuePrevious()
        }
    }

    fun next() = scope.launch {
        Logger.i(TAG, "next clicked")
        if (shuffleMode) {
            queueMutex.withLock {
                val index = if (shuffleIndex >= shuffledQueue.lastIndex) {
                    0
                } else {
                    shuffleIndex + 1
                }
                val song = shuffledQueue[index]
                shuffleIndex = index
                playSongInQueue(song.id)
            }

            /*if (historyCursor >= playHistory.lastIndex) {
                // Get new random song
                val queue = musicQueue.map { it.id }.toSet()
                val played = playHistory.map { it.songId }.toSet()
                val remain = queue - played
                val randomSongId = remain.random()
                playSongInQueue(randomSongId).join()
            } else {
                val song = playHistory[historyCursor + 1]
                playSongInQueue(song.songId).join()
                historyCursor += 1
            }*/
        } else {
            queueNext()
        }
    }

    /**
     * Automatically play next song, generally triggered by player service.
     *
     * Only the autoNext abides by the `repeatMode`
     */
    private fun autoNext() = scope.launch {
        if (repeatMode) {
            // Just play this song
            playerState.songInfo?.id?.let {
                playSongInQueue(it).join()
            }
        } else {
            next()
        }
    }

    fun updateShuffleMode(value: Boolean) {
        shuffleMode = value
    }

    fun updateRepeatMode(value: Boolean) {
        repeatMode = value
    }

    fun clearQueue() = scope.launch {
        queueMutex.withLock {
            musicQueue = emptyList()
            shuffledQueue = emptyList()
            shuffleIndex = -1

            player.stop()
            playerState.clear()
            savePlayerState()
        }
    }

    @Serializable
    data class PlayerStatePersistence(
        val playingSongId: Long?,
        val queue: List<MusicQueueItem>
    )

    suspend fun restorePlayerState() {
        val volume = dataStore.get(PreferencesKeys.PLAYER_VOLUME) ?: 1f
        playerState.volume = volume
        player.setVolume(volume)

        val data = dataStore.get(PreferencesKeys.PLAYER_MUSIC_QUEUE) ?: run {
            Logger.i(TAG, "Music queue was not found")
            return
        }

        val result = try {
            Json.decodeFromString<PlayerStatePersistence>(data)
        } catch (e: Throwable) {
            Logger.w(TAG, "Failed to restore music queue", e)
            return
        }

        replaceQueue(result.queue)
        result.playingSongId?.let {
            playSongInQueue(it, instantPlay = false)
        }
    }

    suspend fun savePlayerState() {
        val playingSongId = playerState.songInfo?.id
        val data = PlayerStatePersistence(playingSongId, musicQueue)
        dataStore.set(PreferencesKeys.PLAYER_MUSIC_QUEUE, Json.encodeToString(data))
    }

    suspend fun setReplayGainEnabled(enabled: Boolean) {
        player.setReplayGainEnabled(enabled)
    }
}