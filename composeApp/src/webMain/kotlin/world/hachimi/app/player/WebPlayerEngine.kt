
package world.hachimi.app.player

import howler.Howl
import howler.HowlOptions
import howler.buildHowl
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import js.array.toJsArray as toBrowserArray
import js.buffer.toArrayBuffer
import js.objects.unsafeJso
import web.blob.Blob
import web.mediasession.MediaImage
import web.mediasession.MediaMetadata
import web.mediasession.MediaMetadataInit
import web.mediasession.MediaSession
import web.navigator.navigator
import web.url.URL
import world.hachimi.app.logging.Logger
import world.hachimi.app.player.PlayerEngine.Companion.mixVolume
import kotlin.js.*
import kotlin.time.measureTime

@OptIn(ExperimentalWasmJsInterop::class, ExperimentalUnsignedTypes::class)
class WebPlayerEngine : AbstractPlatformPlayerEngine() {
    private var howl: Howl? = null
    private val mutex = Mutex()
    private var isReady = false
    private var isReadyMutex = Mutex()
    private var rgDb = 0f
    private var userVolume = 1f
    private var replayGainEnabled: Boolean = true

    override val supportRemotePlay: Boolean
        get() = true

    override suspend fun isPlaying(): Boolean {
        return howl?.playing()?.toBoolean() ?: false
    }

    override suspend fun isEnd(): Boolean {
        return howl?.let {
            it.seek().toDouble() >= it.duration().toDouble()
        } ?: true
    }

    override suspend fun currentPosition(): Long {
        val seconds = howl?.seek()?.toDouble() ?: 0.0
        return (seconds * 1000L).toLong()
    }

    override suspend fun bufferedProgress(): Float {
        return 1f
    }

    override suspend fun play() {
        howl?.play()
    }

    override suspend fun pause() {
        howl?.pause()
    }

    override suspend fun stop() {
        try {
            howl?.stop()
        } catch (e: Throwable) {
            Logger.e("player", "Failed to stop player", e)
        }
    }

    override suspend fun seek(position: Long, autoStart: Boolean) {
        val seconds = position.toDouble() / 1000
        Logger.d("player", "seek to $seconds s")
        howl?.seek(seconds)
    }

    override suspend fun getVolume(): Float {
        return userVolume
    }

    override suspend fun setVolume(value: Float) {
        this.userVolume = value
        val rg = if (replayGainEnabled) rgDb else 0f
        val volume = mixVolume(replayGain = rg, volume = value)
        howl?.volume(volume.toDouble().toJsNumber())
    }

    override suspend fun setReplayGainEnabled(enabled: Boolean) {
        replayGainEnabled = enabled
        // Re-apply effective output volume immediately
        setVolume(userVolume)
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun prepare(item: SongItem, autoPlay: Boolean) {
        mutex.withLock {
            val time = measureTime {
                howl?.let {
                    it.unload()
                    howl = null
                }

                val url: String
                val coverUrl: String?

                when (item) {
                    is SongItem.Local -> {
                        val blob = Blob(listOf(item.audioBytes.toArrayBuffer()).toBrowserArray())
                        url = URL.createObjectURL(blob)

                        val coverBlob = item.coverBytes?.let { Blob(listOf(it.toArrayBuffer()).toBrowserArray()) }
                        coverUrl = coverBlob?.let { URL.createObjectURL(it) }
                    }
                    is SongItem.Remote -> {
                        url = item.audioUrl
                        coverUrl = item.coverUrl
                    }
                }


                Logger.d("player", "URL: $url")
                val options = HowlOptions(
                    src = listOf(url.toJsString()).toJsArray(),
                    format = listOf("mp3".toJsString(), "flac".toJsString()).toJsArray(),
                    html5 = true.toJsBoolean(), // Set this to true to enable the Media Session API
                    onplay = {
                        Logger.d("player", "onplay")
                        notifyEvent(PlayEvent.Play)
                    },
                    onpause = {
                        Logger.d("player", "onpause")
                        notifyEvent(PlayEvent.Pause)
                    },
                    onend = {
                        Logger.d("player", "onend")
                        notifyEvent(PlayEvent.End)
                    }
                )
                val howl = buildHowl(options)
                this.howl = howl
                rgDb = item.replayGainDB
                setVolume(userVolume)
                isReadyMutex.withLock {
                    this.isReady = true
                }

                try {
                    val metadata = MediaMetadata(
                        unsafeJso<MediaMetadataInit>().apply {
                            title = item.title
                            artist = item.artist
                            artwork = (coverUrl?.let { cover ->
                                listOf(unsafeJso<MediaImage>().apply { src = cover })
                            } ?: emptyList()).toBrowserArray()
                        }
                    )
                    val mediaSession: MediaSession? = navigator.mediaSession
                    mediaSession?.let {
                        Logger.d("player", "set metadata: $metadata")
                        it.metadata = metadata
                    }
                } catch (e: Throwable) {
                    Logger.e("player", "Failed to set metadata", e)
                }

                if (autoPlay) howl.play()
            }

            Logger.d("player", "build howl succeed in ${time.inWholeMilliseconds}ms")
        }
    }

    override suspend fun isReady(): Boolean {
        isReadyMutex.withLock {
            return isReady
        }
    }

    override suspend fun release() {
        howl?.unload()
        howl = null
    }

    override suspend fun initialize() {
        // Do nothing because the WASM player does not need to be initialized
    }
}