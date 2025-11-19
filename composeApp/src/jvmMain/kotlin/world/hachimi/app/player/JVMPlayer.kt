package world.hachimi.app.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import world.hachimi.app.logging.Logger
import java.io.ByteArrayInputStream
import javax.sound.sampled.*
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.exp


import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferencesKeys

class JVMPlayer(
    private val dataStore: MyDataStore
) : Player {
    private var clip: Clip = AudioSystem.getLine(DataLine.Info(Clip::class.java, null)) as Clip
    private var volumeControl: FloatControl? = null
    private var masterGainControl: FloatControl? = null

    private lateinit var stream: AudioInputStream
    private var ready = false
    private val listeners: MutableSet<Player.Listener> = mutableSetOf()
    private val mutex = Mutex()
    private var volume: Float = 1f
    private var replayGainDB: Float = 0f

    private var fadeMultiplier = 1f
    private var fadeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun applyVolume() {
        applyVolumeToControls(volumeControl, masterGainControl, replayGainDB, fadeMultiplier)
    }

    private fun applyVolumeToControls(
        volControl: FloatControl?,
        gainControl: FloatControl?,
        replayGain: Float,
        multiplier: Float
    ) {
        val effectiveVolume = volume * multiplier
        if (volControl != null) {
            volControl.value = effectiveVolume
            gainControl?.value = replayGain
        } else {
            gainControl?.let { control ->
                if (effectiveVolume == 0f) {
                    control.value = control.minimum
                } else {
                    val min: Float = control.minimum
                    val max: Float = 0f

                    // Convert volume (0.0 to 1.0) to dB gain (logarithmic)
                    val gain = linearToDb(effectiveVolume)
                    val finalDB = (gain + replayGain).coerceIn(min, max)
                    control.value = finalDB
                }
            }
        }
    }

    private fun startFadeOut(
        clip: Clip,
        volControl: FloatControl?,
        gainControl: FloatControl?,
        replayGain: Float,
        startMultiplier: Float
    ) {
        scope.launch {
            val duration = dataStore.get(PreferencesKeys.SETTINGS_FADE_DURATION) ?: 3000L
            val startTime = System.currentTimeMillis()
            val start = startMultiplier

            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed >= duration) {
                    break
                }
                val t = elapsed.toFloat() / duration
                val m = 12f
                val easing = 1f / (1f + exp(-m * (t - 0.5f)))
                val multiplier = start * (1f - easing)

                applyVolumeToControls(volControl, gainControl, replayGain, multiplier)
                delay(16)
            }
            clip.stop()
            clip.close()
        }
    }

    private fun startFade(target: Float, onFinish: (() -> Unit)? = null) {
        fadeJob?.cancel()
        fadeJob = scope.launch {
            val enabled = dataStore.get(PreferencesKeys.SETTINGS_FADE_IN_FADE_OUT) ?: false
            if (!enabled) {
                fadeMultiplier = target
                applyVolume()
                onFinish?.invoke()
                return@launch
            }

            val duration = dataStore.get(PreferencesKeys.SETTINGS_FADE_DURATION) ?: 3000L
            val start = fadeMultiplier
            val startTime = System.currentTimeMillis()
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed >= duration) {
                    fadeMultiplier = target
                    applyVolume()
                    onFinish?.invoke()
                    break
                }
                val t = elapsed.toFloat() / duration
                val m = 12f
                val easing = 1f / (1f + exp(-m * (t - 0.5f)))
                fadeMultiplier = start + (target - start) * easing
                applyVolume()
                delay(16)
            }
        }
    }

    suspend fun prepare(uri: String, autoPlay: Boolean) {
        /*val bytes = withContext(Dispatchers.IO) {
            val uri = URI.create(uri).toURL()
            uri.readBytes()
        }
        prepare(bytes, autoPlay)*/
    }

    override suspend fun prepare(item: SongItem, autoPlay: Boolean, fade: Boolean): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            // If there is a current clip playing, fade it out and close it later
            if (ready && clip.isRunning) {
                val isFadeEnabled = dataStore.get(PreferencesKeys.SETTINGS_FADE_IN_FADE_OUT) ?: false
                if (isFadeEnabled && fade) {
                    startFadeOut(clip, volumeControl, masterGainControl, replayGainDB, fadeMultiplier)
                } else {
                    clip.stop()
                    clip.close()
                }
            } else {
                clip.close()
            }

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

            fadeJob?.cancel()
            replayGainDB = item.replayGainDB
            
            val isFadeEnabled = dataStore.get(PreferencesKeys.SETTINGS_FADE_IN_FADE_OUT) ?: false
            fadeMultiplier = if (isFadeEnabled && autoPlay && fade) 0f else 1f
            
            setVolume(volume)
            Logger.i("player", "volumeControl = $volumeControl")
            Logger.i("player", "masterGainControl = $masterGainControl")
            Logger.i("player", "replayGain = $replayGainDB")

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
            startFade(1f)
        }
    }

    override suspend fun pause(fade: Boolean) {
        if (ready) {
            if (fade) {
                startFade(0f) {
                    clip.stop()
                }
            } else {
                fadeJob?.cancel()
                clip.stop()
            }
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
                // Convert dB back to linear volume (reverse of linearToDb)
                val actualGain = it.value - replayGainDB
                val volume = 10.0.pow(actualGain / 20.0).toFloat()
                volume.coerceIn(0f, 1f)
            } ?: 1f
        } else {
            1f
        }
    }

    override suspend fun setVolume(value: Float) {
        volume = value
        applyVolume()
    }

    private fun linearToDb(volume: Float): Float = 20f * log10(volume)

    override suspend fun release() {
        scope.cancel()
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