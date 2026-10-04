package world.hachimi.app.player

import androidx.compose.runtime.snapshotFlow
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.hachimi.MediaControlEvent
import uniffi.hachimi.MediaControls
import uniffi.hachimi.MediaControlsListener
import uniffi.hachimi.MediaInfo
import uniffi.hachimi.PlaybackState
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.ApiClient
import world.hachimi.app.logging.Logger
import world.hachimi.app.model.PlayerService
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeBytes
import kotlin.math.abs

private const val TAG = "media_controls"

/**
 * Bridges the player to the system media controls: SMTC on Windows, Now Playing on macOS and MPRIS on Linux.
 *
 * @since 261005
 */
class DesktopMediaControls(
    private val player: PlayerService,
    private val api: ApiClient,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var controls: MediaControls? = null

    /**
     * Called when the system asks to bring the app to the front (MPRIS only).
     */
    var onRaise: (() -> Unit)? = null

    private data class Metadata(
        val title: String,
        val author: String,
        val coverUrl: String?,
        val durationMillis: Long,
    )

    private data class SentPlayback(val playing: Boolean, val positionMillis: Long, val sentAtNanos: Long)

    // Forces the next playback sync, e.g. after the metadata was replaced
    @Volatile
    private var lastPlayback: SentPlayback? = null
    private var coverFile: Path? = null

    fun initialize() {
        if (controls != null) return
        controls = try {
            MediaControls(displayName = BuildKonfig.APP_NAME, dbusName = "hachimi_world", listener = Listener())
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to initialize system media controls", e)
            return
        }
        scope.launch { syncMetadata() }
        scope.launch { syncPlayback() }
    }

    private suspend fun syncMetadata() {
        snapshotFlow {
            val state = player.playerState
            if (state.hasSong) Metadata(
                title = state.displayedTitle,
                author = state.displayedAuthor,
                coverUrl = state.displayedCover,
                durationMillis = state.displayedDurationMillis,
            ) else null
        }.distinctUntilChanged().collectLatest { metadata ->
            val controls = controls ?: return@collectLatest
            if (metadata == null) {
                controls.setMetadata(null)
                return@collectLatest
            }
            controls.setMetadata(metadata.toMediaInfo(coverPath = null))
            lastPlayback = null
            // The system cannot always load our cover URLs, so hand it a local file
            val coverPath = metadata.coverUrl?.let { downloadCover(it) } ?: return@collectLatest
            controls.setMetadata(metadata.toMediaInfo(coverPath = coverPath))
            lastPlayback = null
        }
    }

    private fun Metadata.toMediaInfo(coverPath: String?) = MediaInfo(
        title = title,
        artist = author,
        coverPath = coverPath,
        duration = durationMillis.takeIf { it > 0 }?.let { Duration.ofMillis(it) },
    )

    private suspend fun downloadCover(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val bytes = api.httpClient.get(url).bodyAsBytes()
            val extension = url.substringBefore('?').substringAfterLast('.', "").takeIf { it.length in 3..4 } ?: "jpg"
            val file = Files.createTempFile("hachimi-cover-", ".$extension")
            file.toFile().deleteOnExit()
            file.writeBytes(bytes)
            // The previous file may still be loading on macOS, where the cover is read asynchronously,
            // so only the one before it is removed
            coverFile?.deleteIfExists()
            coverFile = file
            file.absolutePathString()
        } catch (e: Throwable) {
            Logger.w(TAG, "Failed to download cover: $url", e)
            null
        }
    }

    /**
     * Systems extrapolate the position while playing, so it is only sent when the state changes or drifts, e.g. after a seek.
     */
    private suspend fun syncPlayback() {
        while (scope.isActive) {
            val controls = controls ?: return
            val state = player.playerState
            val now = System.nanoTime()
            val position = state.displayedCurrentMillis
            val last = lastPlayback

            if (!state.hasSong) {
                if (last != null) {
                    controls.setPlayback(PlaybackState.Stopped)
                    lastPlayback = null
                }
            } else {
                val playing = state.isPlaying
                val expected = last?.let {
                    if (it.playing) it.positionMillis + (now - it.sentAtNanos) / 1_000_000 else it.positionMillis
                }
                if (last == null || last.playing != playing || abs(position - expected!!) > 1500) {
                    val duration = Duration.ofMillis(position.coerceAtLeast(0))
                    controls.setPlayback(
                        if (playing) PlaybackState.Playing(duration) else PlaybackState.Paused(duration)
                    )
                    lastPlayback = SentPlayback(playing, position, now)
                }
            }
            delay(250)
        }
    }

    private fun seekTo(millis: Long) {
        val duration = player.playerState.displayedDurationMillis
        if (duration <= 0) return
        player.setSongProgress((millis.toFloat() / duration).coerceIn(0f, 1f))
    }

    private inner class Listener : MediaControlsListener {
        override fun onEvent(event: MediaControlEvent) {
            Logger.d(TAG, "Event: $event")
            val state = player.playerState
            when (event) {
                MediaControlEvent.Play -> if (!state.isPlaying) player.playOrPause()
                MediaControlEvent.Pause, MediaControlEvent.Stop -> if (state.isPlaying) player.playOrPause()
                MediaControlEvent.Toggle -> player.playOrPause()
                MediaControlEvent.Next -> player.next()
                MediaControlEvent.Previous -> player.previous()
                is MediaControlEvent.SetPosition -> seekTo(event.position.toMillis())
                is MediaControlEvent.SeekBy -> {
                    val offset = event.offset?.toMillis() ?: 10_000L
                    seekTo(state.displayedCurrentMillis + if (event.forward) offset else -offset)
                }
                MediaControlEvent.Raise -> onRaise?.invoke()
            }
        }
    }
}
