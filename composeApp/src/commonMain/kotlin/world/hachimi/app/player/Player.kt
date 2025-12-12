package world.hachimi.app.player

import world.hachimi.app.logging.Logger
import kotlin.math.log10
import kotlin.math.pow

/**
 * A player can work without UI
 */
interface Player {
    val supportRemotePlay: Boolean

    suspend fun isPlaying(): Boolean
    suspend fun isEnd(): Boolean
    suspend fun currentPosition(): Long
    suspend fun bufferedProgress(): Float

    suspend fun play()
    suspend fun pause()
    suspend fun stop()
    suspend fun seek(position: Long, autoStart: Boolean = false)

    suspend fun getVolume(): Float
    suspend fun setVolume(value: Float)

    /**
     * Download from URL and prepare to play
     * Might throw Exception
     */
    suspend fun prepare(item: SongItem, autoPlay: Boolean = false)
    suspend fun isReady(): Boolean

    suspend fun release()
    suspend fun initialize()
    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)

    interface Listener {
        fun onEvent(event: PlayEvent)
    }

    companion object {
        fun mixVolume(replayGain: Float, volume: Float): Float {
            val volumeDb = 20f * log10(volume)
            val totalDb = replayGain + volumeDb
            val mixedVolume = (if (totalDb.isInfinite()) 0f else gainToMultiplier(totalDb)).coerceIn(0f, 1f)
            Logger.d("player", "volume: $mixedVolume, gain: $totalDb, replay gain: $replayGain, user gain: $volumeDb")
            return mixedVolume
        }

        fun gainToMultiplier(db: Float) = 10f.pow(db / 20f)
    }
}

sealed class PlayEvent {
    object Play : PlayEvent()
    object Pause : PlayEvent()
    object End : PlayEvent()

    /**
     * Might happen during playing while downloading
     */
    data class Error(val e: Throwable) : PlayEvent()
    data class Seek(val position: Long) : PlayEvent()
}

sealed class SongItem {
    abstract val id: String
    abstract val title: String
    abstract val artist: String
    abstract val durationSeconds: Int
    abstract val format: String
    abstract val replayGainDB: Float

    data class Local(
        override val id: String,
        override val title: String,
        override val artist: String,
        override val durationSeconds: Int,
        val audioBytes: ByteArray,
        val coverBytes: ByteArray? = null,
        override val format: String,
        override val replayGainDB: Float
    ): SongItem() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Local

            if (id != other.id) return false
            if (title != other.title) return false
            if (artist != other.artist) return false
            if (!audioBytes.contentEquals(other.audioBytes)) return false
            if (!coverBytes.contentEquals(other.coverBytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + title.hashCode()
            result = 31 * result + artist.hashCode()
            result = 31 * result + audioBytes.contentHashCode()
            result = 31 * result + (coverBytes?.contentHashCode() ?: 0)
            return result
        }
    }

    data class Remote(
        override val id: String,
        override val title: String,
        override val artist: String,
        override val durationSeconds: Int,
        val audioUrl: String,
        val coverUrl: String? = null,
        override val format: String,
        override val replayGainDB: Float
    ): SongItem()
}
