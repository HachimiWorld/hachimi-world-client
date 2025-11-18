@file:OptIn(ExperimentalWasmJsInterop::class, ExperimentalUnsignedTypes::class)

package world.hachimi.app.player

import howler.Howl
import howler.buildHowl
import howler.buildHowlerOptions
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.khronos.webgl.toUint8Array
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import world.hachimi.app.logging.Logger
import world.hachimi.app.player.Player.Companion.mixVolume
import kotlin.time.measureTime

class WasmPlayer : Player {
    private var howl: Howl? = null
    private val mutex = Mutex()
    private var isReady = false
    private var isReadyMutex = Mutex()
    private val listeners: MutableSet<Player.Listener> = mutableSetOf()
    private var rgDb = 0f
    private var userVolume = 1f

    override suspend fun isPlaying(): Boolean {
        return if (isReady()) {
            howl!!.playing().toBoolean()
        } else {
            false
        }
    }

    override suspend fun isEnd(): Boolean {
        if (isReady()) {
            return howl!!.seek().toDouble() >= howl!!.duration().toDouble()
        } else {
            return true
        }
    }

    override suspend fun currentPosition(): Long {
        val seconds = if (isReady()) {
            howl!!.seek().toDouble()
        } else 0.0
        return (seconds * 1000L).toLong()
    }

    override suspend fun play() {
        if (isReady()) {
            howl!!.play()
        }
    }

    override suspend fun pause() {
        if (isReady()) {
            howl!!.pause()
        }
    }

    override suspend fun seek(position: Long, autoStart: Boolean) {
        if (isReady()) {
            val seconds = position.toDouble() / 1000
            Logger.d("player", "seek to $seconds s")
            howl!!.seek(seconds)
        }
    }

    override suspend fun getVolume(): Float {
        return userVolume
    }

    override suspend fun setVolume(value: Float) {
        this.userVolume = value
        val volume = mixVolume(replayGain = rgDb, volume = value)
        howl?.volume(volume.toDouble().toJsNumber())
    }

    override suspend fun prepare(item: SongItem, autoPlay: Boolean) {
        mutex.withLock {
            val time = measureTime {
                val previousVolume = getVolume()
                howl?.let {
                    it.unload()
                    howl = null
                }

                val uint8array = item.audioBytes.toUByteArray().toUint8Array()
                val blob = Blob(arrayOf(uint8array as JsAny?).toJsArray(), BlobPropertyBag())
                val url = URL.createObjectURL(blob)

                val coverUint8Array = item.coverBytes?.toUByteArray()?.toUint8Array()
                val coverBlob = coverUint8Array?.let { Blob(arrayOf(it as JsAny?).toJsArray(), BlobPropertyBag()) }
                val coverUrl = coverBlob?.let { URL.createObjectURL(it) }

                Logger.d("player", "URL: $url")
                val options = buildHowlerOptions(
                    src = listOf(url.toJsString()).toJsArray(),
                    format = listOf("mp3".toJsString(), "flac".toJsString()).toJsArray(),
                    html5 = true.toJsBoolean(), // Set this to true to enable the Media Session API
                    onplay = {
                        Logger.d("player", "onplay")
                        listeners.forEach { listener -> listener.onEvent(PlayEvent.Play) }
                    },
                    onpause = {
                        Logger.d("player", "onpause")
                        listeners.forEach { listener -> listener.onEvent(PlayEvent.Pause) }
                    },
                    onend = {
                        Logger.d("player", "onend")
                        listeners.forEach { listener -> listener.onEvent(PlayEvent.End) }
                    }
                )
                val howl = buildHowl(options)
                this.howl = howl
                rgDb = item.replayGainDB
                setVolume(previousVolume)
                isReadyMutex.withLock {
                    this.isReady = true
                }

                try {
                    val metadata = MediaMetadata(
                        MediaMetadataInit(
                            title = item.title,
                        artist = item.artist,
                        artwork = (coverUrl?.let {
                            arrayOf(MediaImage(src = it))
                        } ?: emptyArray()).toJsArray()
                    ))
                    navigator.mediaSession?.let {
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

    override fun addListener(listener: Player.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)
    }

    override suspend fun initialize() {
        // Do nothing because the WASM player does not need to be initialized
    }
}