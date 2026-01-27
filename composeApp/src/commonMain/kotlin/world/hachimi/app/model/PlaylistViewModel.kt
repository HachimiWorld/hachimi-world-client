package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.playlist_login_required
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.PlaylistModule
import world.hachimi.app.api.ok
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
    var containingPlaylist by mutableStateOf(setOf<Long>())
        private set
    var favoritePlaylists by mutableStateOf<List<PlaylistModule.FavoritePlaylistItem>>(emptyList())
        private set
    var favoritePlaylistsLoading by mutableStateOf(false)
        private set

    fun mounted() {
        viewModelScope.launch {
            if (initializeStatus == InitializeStatus.INIT) {
                launch { refreshPlaylist() }
                launch { refreshFavoritePlaylist() }
            } else {
                launch { refreshPlaylist() }
                launch { refreshFavoritePlaylist() }
            }
        }
    }

    fun dispose() {

    }

    fun retry() {
        initializeStatus = InitializeStatus.INIT

        viewModelScope.launch {
            launch { refreshPlaylist() }
            launch { refreshFavoritePlaylist() }
        }
    }

    fun addToPlaylist(songId: Long) {
        if (!global.isLoggedIn) {
            global.alert(Res.string.playlist_login_required)
            return
        }

        viewModelScope.launch {
            toBeAddedSongId = songId
            selectedPlaylistId = null
            containingPlaylist = emptySet()
            showPlaylistDialog = true
            launch { refreshPlaylist() }
            launch { getContaining(songId) }
        }
    }

    private suspend fun getContaining(songId: Long) {
        try {
            val resp = api.playlistModule.listContaining(PlaylistModule.ListContainingReq(songId))
            if (resp.ok) {
                val data = resp.ok()
                containingPlaylist = data.playlistIds.toSet()
                if (containingPlaylist.contains(selectedPlaylistId)) {
                    selectedPlaylistId = null
                }
            } else {
                global.alert(resp.err().msg)
            }
        } catch (e: Throwable) {
            Logger.e("player", "Failed to list containing playlist", e)
        }
    }

    private suspend fun refreshPlaylist() {
        playlistIsLoading = true

        try {
            val resp = api.playlistModule.list()
            if (resp.ok) {
                val data = resp.ok()
                playlists = data.playlists
                if (initializeStatus == InitializeStatus.INIT) initializeStatus =
                    InitializeStatus.LOADED
            } else {
                val data = resp.err()
                global.alert(data.msg)
                if (initializeStatus == InitializeStatus.INIT) initializeStatus =
                    InitializeStatus.FAILED
            }
        } catch (e: Throwable) {
            Logger.e("player", "Failed to play playlist", e)
            global.alert(e.message)
            if (initializeStatus == InitializeStatus.INIT) initializeStatus =
                InitializeStatus.FAILED
        } finally {
            playlistIsLoading = false
        }
    }

    private suspend fun refreshFavoritePlaylist() {
        favoritePlaylistsLoading = true
        try {
            val resp = api.playlistModule.pageFavorite(
                PlaylistModule.PageFavoritesReq(
                    pageIndex = 0,
                    pageSize = 50
                )
            )
            if (resp.ok) {
                val data = resp.ok()
                favoritePlaylists = data.data
            } else {
                val data = resp.err()
                global.alert(data.msg)
            }
        } catch (e: Throwable) {
            Logger.e("player", "Failed to refresh favorite playlist", e)
        } finally {
            favoritePlaylistsLoading = false
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
                    global.alert(resp.err().msg)
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
                val resp = api.playlistModule.create(
                    PlaylistModule.CreatePlaylistReq(
                        name = createPlaylistName,
                        description = createPlaylistDescription.takeIf { it.isNotBlank() },
                        isPublic = !createPlaylistPrivate
                    )
                )
                if (resp.ok) {
                    showCreatePlaylistDialog = false
                    refreshPlaylist()
                } else {
                    global.alert(resp.err().msg)
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