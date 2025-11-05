package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.logging.Logger
import world.hachimi.app.util.LrcParser
import kotlin.time.Duration

typealias SongDetailInfo = SongModule.PublicSongDetail

/**
 * UI states, should attach a player.
 *
 * TODO(opt): Reduce extra complexity
 */
class PlayerUIState() {
    // Status
    // TODO: Remove this status because it has unclear definition
    var hasSong by mutableStateOf(false)

    /**
     * Getting audio metadata.
     */
    var fetchingMetadata by mutableStateOf(false)
    var fetchingSongId by mutableStateOf<Long?>(null)
        private set
    // If preview metadata is available during fetching process, we can use preview metadata at first
    var previewMetadata by mutableStateOf<PreviewMetadata?>(null)
        private set

    /**
     * Downloading audio data
     */
    var buffering by mutableStateOf(false)
    var downloadProgress by mutableStateOf(0f)

    // Controller-related state
    /**
     * The player should always not be playing during fetching/buffering phase
     */
    var isPlaying by mutableStateOf(false)
    /**
     * Only meaningful when music data loaded
     */
    var currentMillis by mutableStateOf(0L)
        private set

    // Lyrics state
    var currentLyricsLine by mutableStateOf(-1)
        private set
    var timedLyricsEnabled by mutableStateOf(false)
        private set
    var lyricsLines by mutableStateOf<List<String>>(emptyList())
        private set

    // The current playing music info
    var songInfo by mutableStateOf<SongDetailInfo?>(null)
        private set

    var volume by mutableStateOf<Float>(1f)

    private var lrcSegments: List<TimedLyricsSegment> = emptyList()

    data class TimedLyricsSegment(
        val startTimeMs: Long,
        val endTimeMs: Long,
        val spans: List<TimedLyricsSpan>
    )

    data class TimedLyricsSpan(
        val startTimeMs: Long,
        val endTimeMs: Long,
        val text: String
    )

    data class PreviewMetadata(
        val id: Long,
        val displayId: String,
        val title: String,
        val author: String,
        val coverUrl: String,
        val duration: Duration,
        val explicit: Boolean?
    )

    fun updateCurrentMillis(milliseconds: Long) {
        currentMillis = milliseconds

        if (timedLyricsEnabled) {
            val currentLineIndex = lrcSegments.indexOfFirst {
                it.startTimeMs <= milliseconds && milliseconds <= it.endTimeMs
            }
            currentLyricsLine = currentLineIndex
        }
    }

    fun setLyrics(content: String) {
        currentLyricsLine = -1
        
        if (content.isBlank()) {
            lyricsLines = emptyList()
            timedLyricsEnabled = false
            return
        }

        try {
            val lrcLines = LrcParser.parse(content)
            if (lrcLines.isNotEmpty()) {
                val result = mutableListOf<TimedLyricsSegment>()
                for ((index, line) in lrcLines.withIndex()) {
                    val startTime = line.timestampMs

                    val next = lrcLines.getOrNull(index + 1)
                    val endTime = next?.timestampMs ?: Long.MAX_VALUE

                    val segment = TimedLyricsSegment(
                        startTimeMs = startTime,
                        endTimeMs = endTime,
                        // TODO: Support enhanced lrc later
                        spans = listOf(TimedLyricsSpan(startTime, endTime, line.content))
                    )
                    result.add(segment)
                }
//                result.sortBy { it.startTimeMs }
                lrcSegments = result
                lyricsLines = lrcSegments.map { it.spans.first().text }
                timedLyricsEnabled = true
            } else {
                lyricsLines = emptyList()
                timedLyricsEnabled = false
            }
        } catch (e: Throwable) {
            Logger.e("player", "Failed to parse lyrics", e)
            lyricsLines = content.lines()
            timedLyricsEnabled = false
        }
    }

    fun updateSongInfo(data: SongDetailInfo) {
        hasSong = true
        songInfo = data
        setLyrics(data.lyrics)
    }

    fun updatePreviewMetadata(data: PreviewMetadata) {
        previewMetadata = data
    }

    fun clear() {
        songInfo = null
        hasSong = false
        setLyrics("")
    }
}