package world.hachimi.app.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import world.hachimi.app.logging.Logger
import java.io.ByteArrayInputStream
import javax.sound.sampled.*
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min


class JVMPlayer() : Player {
    private var clip: Clip = AudioSystem.getLine(DataLine.Info(Clip::class.java, null)) as Clip
    private var volumeControl: FloatControl? = null
    private var masterGainControl: FloatControl? = null

    private lateinit var stream: AudioInputStream
    private var ready = false
    private val listeners: MutableSet<Player.Listener> = mutableSetOf()
    private val mutex = Mutex()
    private var volume: Float = 1f

    suspend fun prepare(uri: String, autoPlay: Boolean) {
        /*val bytes = withContext(Dispatchers.IO) {
            val uri = URI.create(uri).toURL()
            uri.readBytes()
        }
        prepare(bytes, autoPlay)*/
    }

    override suspend fun prepare(item: SongItem, autoPlay: Boolean): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            clip.close()

            ready = false
            val stream = withContext(Dispatchers.IO) {
                AudioSystem.getAudioInputStream(ByteArrayInputStream(item.audioBytes))
            }
            val originalFormat = stream.format

            Logger.i("player", "originalFormat = $originalFormat")

            // TODO(player)(jvm): Replace javax.sound

            // Decode to PCM
            val sampleBit = originalFormat.sampleSizeInBits.takeIf { it > 0 } ?: 16
            val desiredPcmFormat = AudioFormat( // Signed-int PCM, with original sampleRate and sampleBitSize
                AudioFormat.Encoding.PCM_SIGNED,
                originalFormat.sampleRate,
                sampleBit,
                originalFormat.channels, // Always be 2(stereo)
                originalFormat.channels * (sampleBit / 8),
                originalFormat.sampleRate, // frameRate is the same as sampleRate
                false
            )
            // Decode to PCM
            var decodedStream = withContext(Dispatchers.IO) {
                AudioSystem.getAudioInputStream(desiredPcmFormat, stream)
            }
            Logger.i("player", "decodedFormat = ${decodedStream.format}")

            // Try to get Line with origin pcm format
            var clip = try {
                AudioSystem.getLine(DataLine.Info(Clip::class.java, desiredPcmFormat)) as Clip
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (clip == null) {
                Logger.i("player", "Raw format is unsupported, fallback to PCM 16bit")
                // Target format is not supported, try fallback to PCM 16bit format
                val fallbackFormat = AudioFormat( // 16bit, signed-int PCM, with original sampleRate
                    AudioFormat.Encoding.PCM_SIGNED,
                    originalFormat.sampleRate,
                    16,
                    originalFormat.channels, // Always be 2(stereo)
                    originalFormat.channels * (16 / 8),
                    originalFormat.sampleRate, // frameRate is the same as sampleRate
                    false
                )
                // Transform by the
                decodedStream = withContext(Dispatchers.IO) {
                    AudioSystem.getAudioInputStream(fallbackFormat, decodedStream)
                }
                clip = AudioSystem.getLine(DataLine.Info(Clip::class.java, fallbackFormat)) as Clip
            }


            val defaultFormat = clip.format
            Logger.i("player", "defaultFormat = $defaultFormat")

            clip.addLineListener {
                when (it.type) {
                    LineEvent.Type.START -> listeners.forEach { listener -> listener.onEvent(PlayEvent.Play) }
                    LineEvent.Type.STOP -> {
                        Logger.i("player", "STOP event: ${clip.framePosition} / ${clip.frameLength}")
                        if (clip.framePosition >= clip.frameLength) {
                            Logger.i("player", "End")
                            listeners.forEach { listener -> listener.onEvent(PlayEvent.End) }
                        } else {
                            Logger.i("player", "Pause")
                            listeners.forEach { listener -> listener.onEvent(PlayEvent.Pause) }
                        }
                    }
                }
            }
            clip.open(decodedStream)

            volumeControl = if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
                clip.getControl(FloatControl.Type.VOLUME) as FloatControl
            } else null
            masterGainControl = if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
            } else null

            setVolume(volume)
            Logger.i("player", "volumeControl = $volumeControl")
            Logger.i("player", "masterGainControl = $masterGainControl")

            ready = true
            this@JVMPlayer.clip = clip

            if (autoPlay) {
                play()
            }
        }
    }

    override suspend fun isReady(): Boolean {
        return ready
    }

    override suspend fun isPlaying(): Boolean {
        return clip.isRunning
    }

    override suspend fun isEnd(): Boolean {
        return clip.framePosition >= clip.frameLength - 1
    }

    override suspend fun currentPosition(): Long {
        return clip.microsecondPosition / 1000L
    }

    override suspend fun play() {
        if (ready) {
            clip.start()
        }
    }

    override suspend fun pause() {
        if (ready) {
            clip.stop()
        }
    }

    override suspend fun seek(position: Long, autoStart: Boolean) {
        if (autoStart || isPlaying()) {
            clip.microsecondPosition = position * 1000L
            clip.start()
        } else {
            clip.microsecondPosition = position * 1000L
        }
    }

    override suspend fun getVolume(): Float {
        return if (volumeControl != null) {
            volumeControl?.value ?: 1f
        } else if (masterGainControl != null) {
            masterGainControl?.let {
//                (it.value - it.minimum) / (it.maximum - it.minimum)
                // The maximum gain could be +6 DB, should we make it available to users?
                (it.value - it.minimum) / (0 - it.minimum)
            } ?: 1f
        } else {
            1f
        }
    }

    override suspend fun setVolume(value: Float) {
        volume = value
        if (volumeControl != null) {
            volumeControl?.value = value
        } else if (masterGainControl != null) {
            masterGainControl?.let {
                val min: Float = it.minimum
                val max: Float = 0f

                // Convert volume (0.0 to 1.0) to dB gain (logarithmic)
                var dB = (log10(volume.toDouble()) * 20).toFloat()
                dB = max(min, min(max, dB)) // Clamp to valid range
                masterGainControl?.value = dB
                Logger.d("player", "Set master gain: $dB db")
            }
        }
    }

    override suspend fun release() {
        // Do some cleanup work
        try {
            clip.stop()
            clip.close()
            stream.close()
        } catch (e: Throwable) {
            Logger.e("PlayerImpl", "release: Error closing resources", e)
        }
    }

    override fun addListener(listener: Player.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)
    }

    suspend fun drain() = withContext(Dispatchers.IO) {
        clip.drain()
    }

    override suspend fun initialize() {
        // Do nothing because JVM player does not need to be initialized
    }
}