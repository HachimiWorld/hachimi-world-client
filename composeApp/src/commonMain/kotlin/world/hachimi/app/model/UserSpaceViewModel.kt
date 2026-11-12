package world.hachimi.app.model

import kotlinx.coroutines.CancellationException
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
import world.hachimi.app.api.module.MomentModule
import world.hachimi.app.api.module.PlaylistModule
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import kotlin.time.Duration.Companion.seconds

@KoinViewModel
class UserSpaceViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
    momentStore: MomentStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var loadingProfile by mutableStateOf(false)
        private set
    var loadingSongs by mutableStateOf(false)
        private set
    /** The profile didn't load; the page has nothing to show but the way to try again. @since 261112 */
    var profileFailed by mutableStateOf(false)
        private set
    /** The works didn't load. @since 261112 */
    var songsFailed by mutableStateOf(false)
        private set
    /** The public playlists didn't load. @since 261112 */
    var playlistsFailed by mutableStateOf(false)
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

    /** Their moments, loaded when the tab is first opened. @since 261106 */
    val moments = MomentPager(
        scope = viewModelScope,
        global = global,
        store = momentStore,
        acceptsPosted = { it.author.uid == uid },
    ) { cursor ->
        val userId = uid ?: throw MomentLoadException("")
        val resp = api.momentModule.listByUser(
            MomentModule.ListByUserReq(userId = userId, cursor = cursor),
            auth = global.isLoggedIn,
        )
        if (!resp.ok) throw MomentLoadException(resp.err().msg)
        val data = resp.ok<MomentModule.ListResp>()
        MomentPage(data.items, data.nextCursor)
    }

    /** Profile-page follow/unfollow action loading. */
    var followActionLoading by mutableStateOf(false)
        private set

    /** Non-null while unfollow confirmation dialog is shown. */
    var unfollowDialogUsername by mutableStateOf<String?>(null)
        private set

    fun mounted(uid: Long?) {
        if (this.uid != uid || initializeStatus == InitializeStatus.INIT) {
            initialize(uid)
        }
    }

    fun dispose() {

    }

    /** Loads the whole page again, after the profile failed. @since 261112 */
    fun retry() {
        uid?.let { initialize(it) }
    }

    /** @since 261112 */
    fun retrySongs() = viewModelScope.launch { loadSongs() }

    /** @since 261112 */
    fun retryPlaylists() = viewModelScope.launch { loadPublicPlaylists() }

    private fun initialize(uid: Long?) {
        initializeStatus = InitializeStatus.INIT
        profile = null
        profileFailed = false
        songs.clear()
        privateConnections.clear()
        publicPlaylists.clear()
        moments.reset()
        unfollowDialogUsername = null
        followActionLoading = false

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

    fun follow() = viewModelScope.launch {
        val targetUid = profile?.uid ?: return@launch
        if (myself || followActionLoading) return@launch
        followActionLoading = true
        try {
            val resp = api.userModule.follow(UserModule.FollowReq(targetUid = targetUid))
            if (resp.ok) {
                val data = resp.ok()
                applyFollowState(isFollowing = true, followerCount = data.followerCount)
            } else {
                global.alert(resp.err().msg)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to follow user $targetUid", e)
            global.alert(e.message)
        } finally {
            followActionLoading = false
        }
    }

    fun showUnfollowDialog() {
        val p = profile ?: return
        if (myself) return
        unfollowDialogUsername = p.username
    }

    fun dismissUnfollowDialog() {
        unfollowDialogUsername = null
    }

    fun confirmUnfollow() = viewModelScope.launch {
        val targetUid = profile?.uid ?: return@launch
        if (unfollowDialogUsername == null || myself || followActionLoading) return@launch
        followActionLoading = true
        try {
            val resp = api.userModule.unfollow(UserModule.FollowReq(targetUid = targetUid))
            if (resp.ok) {
                val data = resp.ok()
                applyFollowState(isFollowing = false, followerCount = data.followerCount)
                dismissUnfollowDialog()
            } else {
                global.alert(resp.err().msg)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to unfollow user $targetUid", e)
            global.alert(e.message)
        } finally {
            followActionLoading = false
        }
    }

    private fun applyFollowState(isFollowing: Boolean, followerCount: Long) {
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
        profileFailed = false
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
                Logger.w(TAG, "Failed to fetch profile: ${resp.err().msg}")
                profileFailed = true
            }
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            Logger.e(TAG, "Failed to fetch profile", e)
            profileFailed = true
        } finally {
            loadingProfile = false
        }
    }

    private suspend fun loadSongs() {
        loadingSongs = true
        songsFailed = false
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
                Logger.w(TAG, "Failed to fetch songs: ${resp.err().msg}")
                songsFailed = true
            }
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            Logger.e(TAG, "Failed to fetch songs", e)
            songsFailed = true
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
        playlistsFailed = false
        try {
            val resp = api.playlistModule.listPublicByUser(PlaylistModule.ListPublicByUserReq(userId = uid!!))
            if (resp.ok) {
                val data = resp.ok()
                publicPlaylists.clear()
                publicPlaylists.addAll(data.playlists)
            } else {
                val err = resp.err()
                Logger.e(TAG, "Failed to load public playlists: ${err.msg}")
                playlistsFailed = true
            }
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            Logger.e(TAG, "Failed to load public playlists", e)
            playlistsFailed = true
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