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
import kotlin.time.measureTime

class WasmPlayer : Player {
    private var howl: Howl? = null
    private val mutex = Mutex()
    private var isReady = false
    private var isReadyMutex = Mutex()
    private val listeners: MutableSet<Player.Listener> = mutableSetOf()

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
        if (isReady()) {
            return howl!!.volume().toDouble().toFloat()
        }
        return 1f
    }

    override suspend fun setVolume(value: Float) {
        if (isReady()) {
            howl!!.volume(value.toDouble().toJsNumber())
        }
    }

    override suspend fun prepare(item: SongItem, autoPlay: Boolean) {
        mutex.withLock {
            val time = measureTime {
                howl?.let {
                    it.unload()
                    howl = null
                }

                val uint8array = item.audioBytes.toUByteArray().toUint8Array()
                val blob = Blob(arrayOf(uint8array as JsAny?).toJsArray(), BlobPropertyBag())
                val url = URL.createObjectURL(blob)
                Logger.d("player", "URL: $url")
                val options = buildHowlerOptions(
                    src = listOf(url.toJsString()).toJsArray(),
                    format = listOf("mp3".toJsString(), "flac".toJsString()).toJsArray(),
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
                isReadyMutex.withLock {
                    this.isReady = true
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
}