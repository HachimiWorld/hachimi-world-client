package world.hachimi.app.player

/**
 * A player can work without UI
 */
interface Player {
    suspend fun isPlaying(): Boolean
    suspend fun isEnd(): Boolean
    suspend fun currentPosition(): Long

    suspend fun play()
    suspend fun pause()
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

data class SongItem(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val audioBytes: ByteArray,
    val coverBytes: ByteArray? = null,
    val format: String,
    val replayGainDB: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SongItem

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