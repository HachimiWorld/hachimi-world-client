package world.hachimi.app.model

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Things the user did, for parts of the app that react to them without the sender knowing.
 * Only successful actions are sent.
 */
sealed interface AppEvent {
    data object AppLaunched : AppEvent
    data class SongCompleted(val songId: Long) : AppEvent
    data class SongLiked(val songId: Long) : AppEvent
    data class SongAddedToPlaylist(val songId: Long, val playlistId: Long) : AppEvent
    data class PlaylistFavorited(val playlistId: Long) : AppEvent
    data object CommentPosted : AppEvent
}

/**
 * App-wide event bus. Senders call [send], listeners collect [events].
 *
 * Events are not replayed, so a listener only sees what is sent after it starts collecting.
 */
class AppEvents {
    private val _events = MutableSharedFlow<AppEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    fun send(event: AppEvent) {
        _events.tryEmit(event)
    }
}
