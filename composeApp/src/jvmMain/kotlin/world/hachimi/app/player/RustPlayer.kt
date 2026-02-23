package world.hachimi.app.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uniffi.hachimi.MediaItem
import uniffi.hachimi.PlayerEvent
import world.hachimi.app.logging.Logger
import world.hachimi.app.player.Player.Companion.mixVolume
import java.time.Duration
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempFile
import kotlin.io.path.writeBytes

private const val TAG = "player"

class RustPlayer : AbstractPlatformPlayer() {
    private val player: uniffi.hachimi.Player
    private val listeners: MutableSet<Player.Listener> = mutableSetOf()
    private var replayGainDb: Float = 0f
    private var userVolume = 1f
    private var replayGainEnabled: Boolean = true

    init {
//        JniLoader().javaInit()
//        init()
        player = uniffi.hachimi.Player()
        player.setEventListener(object : uniffi.hachimi.PlayerEventListener {
            override fun onEvent(event: PlayerEvent) {
                Logger.d(TAG, "On event: $event")
                when (event) {
                    PlayerEvent.End -> notifyEvent(PlayEvent.End)
                    PlayerEvent.Pause -> notifyEvent(PlayEvent.Pause)
                    PlayerEvent.Play -> notifyEvent(PlayEvent.Play)
                    is PlayerEvent.Seek -> notifyEvent(PlayEvent.Seek(event.v1.toMillis()))
                    PlayerEvent.Stop -> notifyEvent(PlayEvent.Pause)
                }
            }
        })
    }

    override val supportRemotePlay: Boolean get() = true

    override suspend fun isPlaying(): Boolean {
        return !player.isPaused()
    }

    override suspend fun isEnd(): Boolean {
        return player.empty()
    }

    override suspend fun currentPosition(): Long {
        return player.getPos().toMillis()
    }

    override suspend fun bufferedProgress(): Float {
        return player.bufferProgress()
    }

    override suspend fun play() {
        player.play()
    }

    override suspend fun pause() {
        player.pause()
    }

    override suspend fun stop() {
        player.stop()
    }

    override suspend fun seek(position: Long, autoStart: Boolean) {
        try {
            player.seek(Duration.ofMillis(position))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        if (autoStart) {
            player.play()
        }
    }

    override suspend fun getVolume(): Float {
        return userVolume
    }

    override suspend fun setVolume(value: Float) {
        this.userVolume = value
        val rg = if (replayGainEnabled) replayGainDb else 0f
        val volume = mixVolume(replayGain = rg, volume = value)
        player.setVolume(volume)
    }

    override suspend fun setReplayGainEnabled(enabled: Boolean) {
        replayGainEnabled = enabled
        setVolume(userVolume)
    }

    override suspend fun prepare(
        item: SongItem,
        autoPlay: Boolean
    ) {
        player.stop()
        val audioUrl = when (item) {
            is SongItem.Local -> {
                val file = withContext(Dispatchers.IO) {
                    createTempFile().also { it.writeBytes(item.audioBytes) }
                }
                "file://" + file.absolutePathString()
            }

            is SongItem.Remote -> {
                item.audioUrl
            }
        }
        val mediaItem = MediaItem(
            audioUrl,
            format = item.format,
            replayGainDb = 0f, // We process replay gain ourselves
            durationSecs = item.durationSeconds
        )
        player.appendMediaItem(mediaItem)
        replayGainDb = item.replayGainDB
        setVolume(userVolume)
        if (autoPlay) {
            player.play()
        }
    }

    override suspend fun isReady(): Boolean {
        return true
    }

    override suspend fun release() {
        player.stop()
    }

    override suspend fun initialize() {
        // Do nothing
    }

    override fun addListener(listener: Player.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)
    }
}