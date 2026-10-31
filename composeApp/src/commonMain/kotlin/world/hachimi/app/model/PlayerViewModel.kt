package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.CommentModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

@KoinViewModel
class PlayerViewModel(
    private val global: GlobalStore,
    private val api: ApiClient,
) : ViewModel() {
    val uiState: PlayerUIState
        get() = global.player.playerState

    val liked: Boolean
        get() = global.like.liked
    /** @since 261031 */
    val likeCount: Long
        get() = global.like.likeCount
    val likeLoading: Boolean
        get() = global.like.likeLoading
    val likeOperating: Boolean
        get() = global.like.likeOperating
    val likeEnabled: Boolean
        get() = global.like.likeEnabled

    /** Comments and replies on the song in [loadCommentCount]; null until known. @since 261031 */
    var commentCount by mutableStateOf<Long?>(null)
        private set
    private var commentCountSongId: Long? = null
    private var commentCountJob: Job? = null

    fun toggleLike() = global.like.toggleLike()

    /** Refreshes [commentCount], keeping the song's last known count if it fails. @since 261031 */
    fun loadCommentCount(songId: Long) {
        if (commentCountSongId != songId) {
            commentCountSongId = songId
            commentCount = null
        }
        commentCountJob?.cancel()
        commentCountJob = viewModelScope.launch {
            try {
                // The total comes with any page, so the smallest will do
                val resp = api.commentModule.list(
                    CommentModule.ListReq(CommentModule.TARGET_SONG, songId, limit = 1)
                )
                if (resp.ok) commentCount = resp.ok().total
                else Logger.w(TAG, "Failed to load the comment count of song $songId: ${resp.err().msg}")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load the comment count of song $songId", e)
            }
        }
    }

    private companion object {
        const val TAG = "player"
    }
}
