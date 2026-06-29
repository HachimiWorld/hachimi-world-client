package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.auth_not_logged_in
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.PlaylistModule
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import kotlin.time.Duration.Companion.seconds

@KoinViewModel
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
    val privateConnections = mutableStateListOf<UserModule.ConnectionItem>()

    var loadingPrivateConnections by mutableStateOf(false)
        private set
    val publicPlaylists = mutableStateListOf<PlaylistModule.PlaylistMetadata>()
    var loadingPlaylists by mutableStateOf(false)
        private set
    val songs = mutableStateListOf<SongModule.PublicSongDetail>()
    var pageIndex by mutableStateOf(0L)
        private set
    var pageSize by mutableStateOf(30L)
        private set
    var total by mutableStateOf(0L)
        private set
    private var uid: Long? = null

    fun mounted(uid: Long?) {
        if (this.uid != uid) {
            initialize(uid)
        }
    }

    fun dispose() {

    }

    private fun initialize(uid: Long?) {
        initializeStatus = InitializeStatus.INIT
        profile = null
        songs.clear()
        privateConnections.clear()
        publicPlaylists.clear()

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
            val deferred = mutableListOf(
                async { refreshProfile() },
                async {
                    pageIndex = 0
                    pageSize = 30
                    loadSongs()
                },
                async { loadPublicPlaylists() }
            )
            if (myself) {
                deferred.add(async { refreshConnections() })
            }
            deferred.awaitAll()
            initializeStatus = InitializeStatus.LOADED
        }
    }

    /**
     * Update follow state on the currently loaded profile after a follow/unfollow action
     * succeeds. Called from the screen in response to FollowViewModel.lastActionResult.
     */
    fun updateFollowState(isFollowing: Boolean, followerCount: Long) {
        val p = profile ?: return
        profile = p.copy(
            isFollowing = if (isFollowing) true else null,
            followerCount = followerCount,
        )
    }

    fun updateSongPage(pageIndex: Long, pageSize: Long) = viewModelScope.launch {
        this@UserSpaceViewModel.pageIndex = pageIndex
        this@UserSpaceViewModel.pageSize = pageSize
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
            Logger.e(TAG, "Failed to fetch profile", e)
            global.alert(e.message)
        } finally {
            loadingProfile = false
        }
    }

    private suspend fun loadSongs() {
        loadingSongs = true
        try {
            val resp = api.songModule.pageByUser(SongModule.PageByUserReq(
                userId = uid!!,
                page = pageIndex,
                size = pageSize
            ))
            if (resp.ok) {
                val data = resp.ok()
                songs.clear()
                songs.addAll(data.songs)
                total = data.total
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

    private suspend fun loadPublicPlaylists() {
        loadingPlaylists = true
        try {
            val resp = api.playlistModule.listPublicByUser(PlaylistModule.ListPublicByUserReq(userId = uid!!))
            if (resp.ok) {
                val data = resp.ok()
                publicPlaylists.clear()
                publicPlaylists.addAll(data.playlists)
            } else {
                val err = resp.err()
                Logger.e(TAG, "Failed to load public playlists: ${err.msg}")
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to load public playlists", e)
        } finally {
            loadingPlaylists = false
        }
    }

    private suspend fun refreshConnections() {
        loadingPrivateConnections = true
        try {
            val resp = api.userModule.connectionList()
            if (resp.ok) {
                val data = resp.ok()
                privateConnections.clear()
                privateConnections.addAll(data.items)
            } else {
                val err = resp.err()
                global.alert(err.msg)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to load connections", e)
            global.alert(e.message)
        } finally {
            loadingPrivateConnections = false
        }
    }

    companion object {
        private val TAG = "userspace"
    }
}