package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

private const val TAG = "song_comments"

/**
 * The song a comments page is on. The comments are in [CommentsStore].
 *
 * @since 261031
 */
@KoinViewModel
class SongCommentsViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    private var songDisplayId: String? = null
    private var loadJob: Job? = null

    /** Null while it loads. */
    var song by mutableStateOf<SongModule.PublicSongDetail?>(null)
        private set
    var status by mutableStateOf(InitializeStatus.INIT)
        private set

    fun mounted(songDisplayId: String) {
        if (this.songDisplayId == songDisplayId && status != InitializeStatus.FAILED) return
        this.songDisplayId = songDisplayId
        load()
    }

    fun retry() = load()

    private fun load() {
        val songDisplayId = songDisplayId ?: return
        loadJob?.cancel()
        song = null
        status = InitializeStatus.INIT
        loadJob = viewModelScope.launch {
            try {
                val resp = api.songModule.detail(songDisplayId)
                if (!resp.ok) error(resp.err().msg)
                song = resp.ok()
                status = InitializeStatus.LOADED
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load the song", e)
                status = InitializeStatus.FAILED
            }
        }
    }

    /** Plays the song (unless it's already playing) in the full player. */
    fun playSong() {
        val songDisplayId = songDisplayId ?: return
        if (global.player.playerState.songInfo?.displayId != songDisplayId) {
            global.player.insertToQueueWithFetch(songDisplayId, instantPlay = true, append = false)
        }
        global.expandPlayer()
    }
}
