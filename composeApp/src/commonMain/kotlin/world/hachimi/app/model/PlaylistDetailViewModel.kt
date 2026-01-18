package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.publish_image_too_large
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.module.PlaylistModule
import world.hachimi.app.logging.Logger
import kotlin.time.Duration.Companion.seconds

class PlaylistDetailViewModel(
    private val api: ApiClient,
    private val global: GlobalStore
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var loading by mutableStateOf(false)
        private set
    var playlistId by mutableStateOf<Long?>(null)
    var playlistInfo by mutableStateOf<PlaylistModule.PlaylistItem?>(null)
    var songs by mutableStateOf<List<PlaylistModule.SongItem>>(emptyList())

    var showEditDialog by mutableStateOf(false)
    var editName by mutableStateOf("")
    var editDescription by mutableStateOf("")
    var editOperating by mutableStateOf(false)
    var editPrivate by mutableStateOf(true)

    var coverUploading by mutableStateOf(false)
    var coverUploadingProgress by mutableStateOf(0f)

    fun mounted(playlistId: Long) {
        if (this.playlistId != playlistId) {
            init()
        } else {
            viewModelScope.launch {
                refresh()
            }
        }
        this.playlistId = playlistId
    }

    fun dispose() {

    }

    fun init() = viewModelScope.launch {
        initStatus = InitializeStatus.INIT
        refresh()
    }

    fun retry() = viewModelScope.launch {
        initStatus = InitializeStatus.INIT
        refresh()
    }

    fun edit() {
        playlistInfo?.let {
            editName = it.name
            editDescription = it.description ?: ""
            editPrivate = !it.isPublic
            showEditDialog = true
        }
    }

    fun confirmEdit() {
        viewModelScope.launch {
            playlistId?.let { id ->
                editOperating = false
                try {
                    val resp = api.playlistModule.update(
                        PlaylistModule.UpdatePlaylistReq(
                            id = id,
                            name = editName,
                            description = editDescription.takeIf { it.isNotBlank() },
                            isPublic = !editPrivate
                        )
                    )
                    if (resp.ok) {
                        showEditDialog = false
                        refresh()
                    } else {
                        val data = resp.errData<CommonError>()
                        global.alert(data.msg)
                    }
                } catch (e: Throwable) {
                    Logger.e("playlist", "Failed to edit playlist", e)
                    global.alert(e.message)
                } finally {
                    editOperating = false
                }
            }
        }
    }

    fun cancelEdit() {
        showEditDialog = false
    }

    fun playAll() {
        viewModelScope.launch {
            val items = songs.map {
                GlobalStore.MusicQueueItem(
                    id = it.songId,
                    displayId = it.songDisplayId,
                    name = it.title,
                    artist = it.uploaderName,
                    duration = it.durationSeconds.seconds,
                    coverUrl = it.coverUrl,
                    explicit = null // TODO: Get explicit from server
                )
            }
            global.player.playAll(items)
        }
    }

    private suspend fun refresh() {
        loading = true
        try {
            val resp = api.playlistModule.detailPrivate(PlaylistModule.PlaylistIdReq(playlistId!!))
            if (resp.ok) {
                val data = resp.okData<PlaylistModule.DetailResp>()
                playlistInfo = data.playlistInfo
                songs = data.songs
                if (initStatus == InitializeStatus.INIT) {
                    initStatus = InitializeStatus.LOADED
                }
            } else {
                global.alert(resp.errData<CommonError>().msg)
                if (initStatus == InitializeStatus.INIT) {
                    initStatus = InitializeStatus.FAILED
                }
            }
        } catch (e: Throwable) {
            Logger.e("playlist detail", "Failed to refresh playlist detail", e)
            global.alert(e.message)
        } finally {
            loading = false
        }
    }

    fun editCover() {
        viewModelScope.launch {
            val image = FileKit.openFilePicker(
                type = FileKitType.Image
            )
            if (image != null) {
                // 1. Validate image
                val size = image.size()
                if (size > 4 * 1024 * 1024) {
                    global.alert(Res.string.publish_image_too_large)
                    return@launch
                }
                // TODO[opt]: Here might be a memory copy
                val buffer = Buffer().apply { write(image.readBytes()) }
                // 2. Upload
                try {
                    coverUploading = true
                    val resp = api.playlistModule.setCover(
                        req = PlaylistModule.SetCoverReq(playlistId = playlistId!!),
                        filename = image.name, source = buffer, listener = { sent, total ->
                            val progress = (sent.toDouble() / size).toFloat()
                            coverUploadingProgress = progress.coerceIn(0f, 1f)
                        }
                    )
                    if (resp.ok) {
                        refresh()
                    } else {
                        val error = resp.errData<CommonError>()
                        global.alert(error.msg)
                        return@launch
                    }
                } catch (e: Throwable) {
                    Logger.e("playlist", "Failed to set cover image", e)
                    global.alert(e.message)
                    return@launch
                } finally {
                    coverUploading = false
                }
            }
        }
    }

    fun removeFromPlaylist(songId: Long) {
        playlistId?.let { playlistId ->
            viewModelScope.launch {
                api.playlistModule.removeSong(
                    PlaylistModule.AddSongReq(
                        playlistId = playlistId,
                        songId = songId
                    )
                )
                refresh()
            }
        }
    }
}