package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.module.PlaylistModule
import world.hachimi.app.logging.Logger

class PlaylistViewModel(
    private val api: ApiClient,
    private val global: GlobalStore
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    // Playlist related states
    // Store at here because the footer player is shared across multiple screens
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var toBeAddedSongId by mutableStateOf<Long?>(null)
    var showPlaylistDialog by mutableStateOf(false)
    var playlists by mutableStateOf<List<PlaylistModule.PlaylistItem>>(emptyList())
    var playlistIsLoading by mutableStateOf(false)
    var selectedPlaylistId by mutableStateOf<Long?>(null)
    var addingToPlaylistOperating by mutableStateOf(false)

    fun mounted() {
        viewModelScope.launch {
            if (initializeStatus == InitializeStatus.INIT) {
                refreshPlaylist()
            } else {
                refreshPlaylist()
            }
        }
    }

    fun dispose() {

    }

    fun retry() {
        initializeStatus = InitializeStatus.INIT
        viewModelScope.launch {
            refreshPlaylist()
        }
    }

    fun addToPlaylist() {
        if (!global.player.playerState.hasSong) return
        if (!global.isLoggedIn) {
            global.alert("歌单功能登录后可用")
            return
        }

        viewModelScope.launch {
            toBeAddedSongId = global.player.playerState.songId
            selectedPlaylistId = null
            showPlaylistDialog = true
            refreshPlaylist()
        }
    }

    private suspend fun refreshPlaylist() {
        playlistIsLoading = true

        try {
            val resp = api.playlistModule.list()
            if (resp.ok) {
                val data = resp.okData<PlaylistModule.ListResp>()
                playlists = data.playlists
                if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.LOADED
            } else {
                val data = resp.errData<CommonError>()
                global.alert(data.msg)
                if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.FAILED
            }
        } catch (e: Throwable) {
            Logger.e("player", "Failed to play playlist", e)
            global.alert(e.message)
            if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.FAILED
        } finally {
            playlistIsLoading = false
        }
    }

    fun confirmAddToPlaylist() {
        viewModelScope.launch {
            addingToPlaylistOperating = true
            try {
                val resp = api.playlistModule.addSong(
                    PlaylistModule.AddSongReq(
                        playlistId = selectedPlaylistId ?: return@launch,
                        songId = toBeAddedSongId ?: return@launch
                    )
                )
                if (resp.ok) {
                    showPlaylistDialog = false
                } else {
                    global.alert(resp.errData<CommonError>().msg)
                }
            } catch (e: Throwable) {
                Logger.e("player", "Failed to add playlist", e)
                global.alert(e.message)
            } finally {
                addingToPlaylistOperating = false
            }
        }
    }

    fun cancelAddToPlaylist() {
        showPlaylistDialog = false
    }

    var showCreatePlaylistDialog by mutableStateOf(false)
    var createPlaylistName by mutableStateOf("")
    var createPlaylistDescription by mutableStateOf("")
    var createPlaylistPrivate by mutableStateOf(false)
    var createPlaylistOperating by mutableStateOf(false)

    fun createPlaylist() {
        createPlaylistName = ""
        createPlaylistDescription = ""
        showCreatePlaylistDialog = true
    }

    fun confirmCreatePlaylist() {
        viewModelScope.launch {
            createPlaylistOperating = true
            // Do something
            try {
                val resp = api.playlistModule.create(PlaylistModule.CreatePlaylistReq(
                    name = createPlaylistName,
                    description = createPlaylistDescription.takeIf { it.isNotBlank() },
                    isPublic = !createPlaylistPrivate
                ))
                if (resp.ok) {
                    showCreatePlaylistDialog = false
                    refreshPlaylist()
                } else {
                    global.alert(resp.errData<CommonError>().msg)
                }
            } catch (e: Throwable) {
                Logger.e("player", "Failed to create playlist", e)
                global.alert(e.message)
            } finally {
                createPlaylistOperating = false
            }
        }
    }

    fun cancelCreatePlaylist() {
        showCreatePlaylistDialog = false
    }
}