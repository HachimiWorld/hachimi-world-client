package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.need_login_message
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

class PlayerViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    val uiState: PlayerUIState
        get() = global.player.playerState

    var liked by mutableStateOf(false)
        private set
    var likeLoading by mutableStateOf(false)
        private set
    var likeOperating by mutableStateOf(false)
        private set

    val likeEnabled: Boolean
        get() = currentSongId != null && !likeLoading && !likeOperating

    private var currentSongId: Long? = null
    private var playbackPositionMillis = 0L

    init {
        observeCurrentSong()
        observePlaybackPosition()
    }

    fun toggleLike() {
        val songId = currentSongId ?: return
        if (!global.isLoggedIn) {
            global.alert(Res.string.need_login_message)
            return
        }
        if (likeLoading || likeOperating) {
            return
        }

        viewModelScope.launch {
            likeOperating = true
            val targetLiked = !liked
            try {
                val resp = if (targetLiked) {
                    api.songModule.like(
                        SongModule.LikeReq(
                            songId = songId,
                            playbackPositionSecs = playbackPositionMillis.toPlaybackPositionSecs(),
                        )
                    )
                } else {
                    api.songModule.unlike(SongModule.UnlikeReq(songId))
                }

                if (resp.ok) {
                    if (currentSongId == songId) {
                        liked = targetLiked
                    }
                } else {
                    global.alert(resp.err().msg)
                }
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to toggle like status for song $songId", e)
                global.alert(e.message)
            } finally {
                if (currentSongId == songId) {
                    likeOperating = false
                }
            }
        }
    }

    private fun observeCurrentSong() {
        viewModelScope.launch {
            snapshotFlow {
                global.isLoggedIn to uiState.readySongInfo?.id
            }
                .distinctUntilChanged()
                .collectLatest { (loggedIn, songId) ->
                    currentSongId = songId
                    likeOperating = false

                    if (!loggedIn || songId == null) {
                        liked = false
                        likeLoading = false
                        return@collectLatest
                    }

                    likeLoading = true
                    try {
                        val resp = api.songModule.likeStatus(SongModule.LikeStatusReq(songId))
                        if (currentSongId != songId) {
                            return@collectLatest
                        }
                        if (resp.ok) {
                            liked = resp.ok().liked
                        } else {
                            global.alert(resp.err().msg)
                            liked = false
                        }
                    } catch (_: CancellationException) {
                        Logger.i(TAG, "Cancelled fetching like status for song $songId")
                    } catch (e: Throwable) {
                        if (currentSongId == songId) {
                            Logger.e(TAG, "Failed to fetch like status for song $songId", e)
                            global.alert(e.message)
                            liked = false
                        }
                    } finally {
                        if (currentSongId == songId) {
                            likeLoading = false
                        }
                    }
                }
        }
    }

    private fun observePlaybackPosition() {
        viewModelScope.launch {
            snapshotFlow { uiState.displayedCurrentMillis }
                .distinctUntilChanged()
                .collect { playbackPositionMillis = it.coerceAtLeast(0L) }
        }
    }

    private fun Long.toPlaybackPositionSecs(): Int {
        return (this / 1000L)
            .coerceIn(0L, Int.MAX_VALUE.toLong())
            .toInt()
    }

    private companion object {
        const val TAG = "player-vm"
    }
}

