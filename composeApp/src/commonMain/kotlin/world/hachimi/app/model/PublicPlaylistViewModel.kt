package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.PlaylistModule
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import world.hachimi.app.model.GlobalStore.MusicQueueItem
import kotlin.time.Duration.Companion.seconds

private const val TAG = "PublicPlaylistViewModel"

class PublicPlaylistViewModel(
    private val global: GlobalStore,
    private val api: ApiClient
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var loading by mutableStateOf(true)
        private set
    var playlistId by mutableStateOf(-1L)
        private set
    var playlistInfo by mutableStateOf<PlaylistModule.PlaylistItem?>(null)
        private set
    var creatorProfile by mutableStateOf<UserModule.PublicUserProfile?>(null)
        private set
    var songs by mutableStateOf<List<PlaylistModule.SongItem>>(emptyList())
        private set
    var isFavorite by mutableStateOf<Boolean?>(null)
        private set
    var operating by mutableStateOf(false)
        private set

    fun mounted(playlistId: Long) {
        if (initStatus == InitializeStatus.INIT || initStatus == InitializeStatus.FAILED) {
            initStatus = InitializeStatus.INIT
            this.playlistId = playlistId
            load()
        } else if (this.playlistId != playlistId) {
            initStatus = InitializeStatus.INIT
            this.playlistId = playlistId
            load()
        }
    }

    fun dispose() {

    }

    fun retry() {
        if (initStatus == InitializeStatus.FAILED) {
            initStatus = InitializeStatus.INIT
            load()
        }
    }

    fun refresh() {
        if (initStatus == InitializeStatus.LOADED) {
            load()
        }
    }

    fun play(song: PlaylistModule.SongItem) {
        global.player.insertToQueue(song.toMusicQueueItem(), true, false)
    }

    fun playAll() {
        global.player.playAll(songs.map { it.toMusicQueueItem() })
    }

    fun favorite(value: Boolean) = viewModelScope.launch {
        operating = true
        try {
            if (value) {
                val resp = api.playlistModule.addFavorite(PlaylistModule.AddFavoriteReq(playlistId))
                if (resp.ok) {
                    isFavorite = true
                } else {
                    global.alert(resp.err().msg)
                }
            } else {
                val resp = api.playlistModule.removeFavorite(PlaylistModule.RemoveFavoriteReq(playlistId))
                if (resp.ok) {
                    isFavorite = false
                } else {
                    global.alert(resp.err().msg)
                }
            }
        } finally {
            operating = false
        }
    }

    private fun load() = viewModelScope.launch {
        loading = true
        try {
            val checkAsync = async {
                api.playlistModule.checkFavorite(PlaylistModule.CheckFavoriteReq(playlistId))
            }

            val resp = api.playlistModule.detail(PlaylistModule.PlaylistIdReq(id = playlistId))
            if (resp.ok) {
                val data = resp.ok()
                playlistInfo = data.playlistInfo
                creatorProfile = data.creatorProfile
                songs = data.songs
            } else {
                val err = resp.err()
                global.alert(err.msg)
                if (initStatus == InitializeStatus.INIT) {
                    initStatus = InitializeStatus.FAILED
                }
            }

            val checkResp = checkAsync.await()
            if (checkResp.ok) {
                val data = checkResp.ok()
                isFavorite = data.isFavorite
            } else {
                val err = checkResp.err()
                global.alert(err.msg)
            }

            if (initStatus == InitializeStatus.INIT) {
                initStatus = InitializeStatus.LOADED
            }

        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to load playlist", e)
            global.alert(e.message)
            if (initStatus == InitializeStatus.INIT) {
                initStatus = InitializeStatus.FAILED
            }
        } finally {
            loading = false
        }
    }
}

private fun PlaylistModule.SongItem.toMusicQueueItem(): MusicQueueItem {
    return MusicQueueItem(
        id = songId,
        displayId = songDisplayId,
        name = title,
        artist = uploaderName,
        duration = durationSeconds.seconds,
        coverUrl = coverUrl,
        explicit = null // TODO
    )
}