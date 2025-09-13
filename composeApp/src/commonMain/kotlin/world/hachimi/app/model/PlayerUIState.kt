package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import world.hachimi.app.api.module.PlaylistModule
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.logging.Logger
import world.hachimi.app.player.SongItem
import world.hachimi.app.util.LrcParser

typealias SongDetailInfo = SongModule.DetailResp

/**
 * UI states, should attach a player
 */
class PlayerUIState() {
    // Status
    var hasSong by mutableStateOf(false)
    var isFetching by mutableStateOf(false)
    var isPlaying by mutableStateOf(false)
    var isBuffering by mutableStateOf(false)
    var downloadProgress by mutableStateOf(0f)
    var currentMillis by mutableStateOf(0L)
        private set

    // Info
    var songId by mutableStateOf<Long?>(null)
        private set
    var songDisplayId by mutableStateOf("")
        private set
    var songTitle by mutableStateOf("")
        private set
    var songAuthor by mutableStateOf("")
        private set
    var songCoverUrl by mutableStateOf<String?>(null)
        private set
    var songDurationSecs by mutableStateOf(-1)
        private set
    var staff by mutableStateOf<List<Pair<String, String>>>(emptyList())
        private set
    // Lyrics status
    var currentLyricsLine by mutableStateOf(-1)
        private set
    var timedLyricsEnabled by mutableStateOf(false)
        private set
    var lyricsLines by mutableStateOf<List<String>>(emptyList())
        private set

    var songInfo by mutableStateOf<SongDetailInfo?>(null)

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
        try {
            val lrcLines = LrcParser.parse(content)
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
            this.lrcSegments = result
            lyricsLines = lrcSegments.map { it.spans.first().text }
            timedLyricsEnabled = true
        } catch (e: Exception) {
            Logger.e("player", "Failed to parse lyrics", e)
            lyricsLines = content.lines()
            timedLyricsEnabled = false
        }
    }

    fun updateSongInfo(data: SongDetailInfo) {
        songId = data.id
        songDisplayId = data.displayId
        hasSong = true
        songCoverUrl = data.coverUrl
        songTitle = data.title
        songAuthor = data.uploaderName
        songDurationSecs = data.durationSeconds
        staff = data.productionCrew.map {
            it.role to (it.personName ?: it.uid?.toString() ?: "Unknown")
        }
        setLyrics(data.lyrics)
    }
}