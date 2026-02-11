package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.auth_not_logged_in
import hachimiworld.composeapp.generated.resources.publish_image_too_large
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import kotlin.time.Duration.Companion.seconds

class UserSpaceViewModel(
    private val api: ApiClient,
    private val global: GlobalStore
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var loadingProfile by mutableStateOf(false)
        private set
    var loadingSongs by mutableStateOf(false)
        private set
    var myself by mutableStateOf(false)
        private set
    var profile by mutableStateOf<UserModule.PublicUserProfile?>(null)
        private set
    var showEditBio by mutableStateOf(false)
        private set
    var editBioValue by mutableStateOf<String>("")

    var showEditUsername by mutableStateOf(false)
        private set
    var editUsernameValue by mutableStateOf<String>("")

    var operating by mutableStateOf(false)
        private set

    var avatarUploading by mutableStateOf(false)
    var avatarUploadProgress by mutableStateOf(0f)

    val songs = mutableStateListOf<SongModule.PublicSongDetail>()
    var songPage by mutableStateOf(0)
        private set
    var songPageSize by mutableStateOf(20)
        private set
    private var uid: Long? = null

    fun mounted(uid: Long?) {
        when (initializeStatus) {
            InitializeStatus.INIT, InitializeStatus.FAILED -> {
                initialize(uid)
            }

            InitializeStatus.LOADED -> {
                // Just refresh?
                if (this.uid != uid) {
                    initialize(uid)
                } else {
                    refresh()
                }
            }
        }
    }

    fun dispose() {

    }

    private fun initialize(uid: Long?) {
        initializeStatus = InitializeStatus.INIT
        profile = null
        songs.clear()

        // Initialize
        if (uid == null) {
            val userInfo = global.userInfo
            if (userInfo == null) {
                global.alert(Res.string.auth_not_logged_in)
                return
            }
            this.uid = userInfo.uid
        } else {
            this.uid = uid
        }
        myself = this.uid == global.userInfo?.uid

        viewModelScope.launch {
            awaitAll(
                async {
                    refreshProfile()
                },
                async {
                    loadSongs()
                }
            )
            initializeStatus = InitializeStatus.LOADED
        }
    }

    private fun refresh() {

    }

    fun editAvatar() {
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
                val buffer = Buffer().apply { write(image.readBytes()) }

                // 2. Upload
                try {
                    avatarUploading = true

                    val resp =
                        api.userModule.setAvatar(filename = image.name, source = buffer, listener = { sent, total ->
                            val progress = (sent.toDouble() / size).toFloat()
                            avatarUploadProgress = progress.coerceIn(0f, 1f)
                        })
                    if (resp.ok) {
                        refreshProfile()
                    } else {
                        val error = resp.err()
                        global.alert(error.msg)
                        return@launch
                    }
                } catch (e: Throwable) {
                    Logger.e("creation", "Failed to upload image image", e)
                    global.alert(e.message)
                    return@launch
                } finally {
                    avatarUploading = false
                }
            }
        }
    }

    fun editUsername() {
        editUsernameValue = profile?.username ?: ""
        showEditUsername = true
    }

    fun confirmEditUsername() {
        viewModelScope.launch {
            operating = true
            try {
                val resp = api.userModule.updateProfile(
                    UserModule.UpdateProfileReq(
                        username = editUsernameValue,
                        bio = profile!!.bio,
                        gender = profile!!.gender,
                    )
                )
                if (resp.ok) {
                    showEditUsername = false
                    refreshProfile()
                } else {
                    val err = resp.err()
                    global.alert(err.msg)
                }
            } catch (e: Throwable) {
                Logger.e("userspace", "Failed to update profile", e)
                global.alert(e.message)
            } finally {
                operating = false
            }
        }
    }

    fun editBio() {
        editBioValue = profile?.bio ?: ""
        showEditBio = true
    }

    fun cancelEdit() {
        showEditBio = false
        showEditUsername = false
    }

    fun confirmEditBio() {
        viewModelScope.launch {
            operating = true
            try {
                val resp = api.userModule.updateProfile(
                    UserModule.UpdateProfileReq(
                        username = profile!!.username,
                        bio = editBioValue,
                        gender = profile!!.gender,
                    )
                )
                if (resp.ok) {
                    showEditBio = false
                    refreshProfile()
                } else {
                    val err = resp.err()
                    global.alert(err.msg)
                }
            } catch (e: Throwable) {
                Logger.e("userspace", "Failed to update profile", e)
                global.alert(e.message)
            } finally {
                operating = false
            }
        }
    }

    fun updateSongPage(page: Int, pageSize: Int) = viewModelScope.launch {
        songPage = page
        songPageSize = pageSize
        loadSongs()
    }

    private suspend fun refreshProfile() {
        loadingProfile = true
        try {
            val resp = api.userModule.profile(uid!!)
            if (resp.ok) {
                val data = resp.ok()
                profile = data
                if (myself) {
                    // Update self profile
                    global.setLoginUser(data.uid, data.username, data.avatarUrl, true)
                }
            } else {
                val err = resp.err()
                global.alert(err.msg)
            }
        } catch (e: Throwable) {
            Logger.e("userspace", "Failed to fetch profile", e)
            global.alert(e.message)
        } finally {
            loadingProfile = false
        }
    }

    private suspend fun loadSongs() {
        loadingSongs = true
        try {
            val resp = api.songModule.pageByUser(SongModule.PageByUserReq(uid!!, null, null))
            if (resp.ok) {
                val data = resp.ok()
                songs.clear()
                songs.addAll(data.songs)
            } else {
                val err = resp.err()
                global.alert(err.msg)
            }
        } catch (e: Throwable) {
            Logger.e("userspace", "Failed to fetch songs", e)
            global.alert(e.message)
        } finally {
            loadingSongs = false
        }
    }

    fun playAll() {
        if (songs.isEmpty()) return
        viewModelScope.launch {
            global.player.playAll(songs.map {
                GlobalStore.MusicQueueItem(
                    id = it.id,
                    displayId = it.displayId,
                    name = it.title,
                    artist = it.uploaderName,
                    duration = it.durationSeconds.seconds,
                    coverUrl = it.coverUrl,
                    explicit = it.explicit,
                )
            })
        }
    }
}