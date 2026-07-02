package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

enum class FollowListType { FOLLOWING, FOLLOWERS }

@KoinViewModel
class FollowViewModel(
    private val api: ApiClient,
    private val global: GlobalStore
) : ViewModel(CoroutineScope(Dispatchers.Default)) {

    var listType by mutableStateOf(FollowListType.FOLLOWING)
        private set

    // Items — can hold either FollowingItem or FollowerItem
    val followingItems = mutableStateListOf<UserModule.FollowingItem>()
    val followerItems = mutableStateListOf<UserModule.FollowerItem>()

    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var loading by mutableStateOf(false)
        private set
    var loadingMore by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var hasMore by mutableStateOf(true)
        private set
    private var nextCursor: String? = null

    // Follow/unfollow action state
    var actionTargetUid by mutableStateOf<Long?>(null)
        private set
    var actionLoading by mutableStateOf(false)
        private set

    // Unfollow dialog state — supports both list item and profile page contexts
    data class UnfollowTarget(val uid: Long, val username: String)
    var unfollowDialogTarget: UnfollowTarget? by mutableStateOf(null)
        private set

    // Track which uid we're operating on (for UserSpaceScreen loading state)
    var uid by mutableStateOf<Long?>(null)
        private set

    // Result of last follow/unfollow action — consumed by screens to update local profile state
    data class LastActionResult(
        val uid: Long,
        val isFollowing: Boolean,
        val followerCount: Long,
    )
    var lastActionResult by mutableStateOf<LastActionResult?>(null)
        private set

    fun consumeLastActionResult(): LastActionResult? {
        val r = lastActionResult
        lastActionResult = null
        return r
    }

    fun initForProfile(uid: Long?) {
        this.uid = uid
    }

    fun mounted(listType: FollowListType) {
        if (this.listType != listType) {
            this.listType = listType
            followingItems.clear()
            followerItems.clear()
            nextCursor = null
            hasMore = true
            error = null
        }
        loadFirstPage()
    }

    fun dispose() {
        // no-op for now
    }

    fun loadFirstPage() {
        loading = true
        viewModelScope.launch {
            try {
                error = null
                nextCursor = null
                hasMore = true
                when (listType) {
                    FollowListType.FOLLOWING -> followingItems.clear()
                    FollowListType.FOLLOWERS -> followerItems.clear()
                }
                loadPage()
                if (initializeStatus == InitializeStatus.INIT) {
                    initializeStatus = InitializeStatus.LOADED
                }
            } catch (e: Throwable) {
                if (initializeStatus == InitializeStatus.INIT) {
                    initializeStatus = InitializeStatus.FAILED
                }
            } finally {
                loading = false
            }
        }
    }

    fun loadNextPage() {
        if (!hasMore || loadingMore || loading) return
        loadingMore = true
        viewModelScope.launch {
            try {
                loadPage()
            } finally {
                loadingMore = false
            }
        }
    }

    private suspend fun loadPage() {
        try {
            when (listType) {
                FollowListType.FOLLOWING -> {
                    val resp = api.userModule.following(UserModule.FollowingReq(after = nextCursor, limit = 20))
                    if (resp.ok) {
                        val data = resp.ok()
                        followingItems.addAll(data.items)
                        nextCursor = data.nextCursor
                        hasMore = data.nextCursor != null
                    } else {
                        val err = resp.err()
                        error = err.msg
                    }
                }
                FollowListType.FOLLOWERS -> {
                    val resp = api.userModule.followers(UserModule.FollowersReq(after = nextCursor, limit = 20))
                    if (resp.ok) {
                        val data = resp.ok()
                        followerItems.addAll(data.items)
                        nextCursor = data.nextCursor
                        hasMore = data.nextCursor != null
                    } else {
                        val err = resp.err()
                        error = err.msg
                    }
                }
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to load ${listType.name}", e)
            error = e.message
        }
    }

    fun follow(uid: Long) = viewModelScope.launch {
        actionTargetUid = uid
        actionLoading = true
        try {
            val resp = api.userModule.follow(UserModule.FollowReq(targetUid = uid))
            if (resp.ok) {
                // Update local state: remove from following list if in followers view,
                // or update is_following state.
                // The caller should refresh profile stats.
            } else {
                val err = resp.err()
                global.alert(err.msg)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to follow user $uid", e)
            global.alert(e.message)
        } finally {
            actionLoading = false
            actionTargetUid = null
        }
    }

    fun showUnfollowDialog(item: UserModule.FollowingItem) {
        unfollowDialogTarget = UnfollowTarget(item.user.uid, item.user.username)
    }

    fun showUnfollowDialog(uid: Long, username: String) {
        unfollowDialogTarget = UnfollowTarget(uid, username)
    }

    fun dismissUnfollowDialog() {
        unfollowDialogTarget = null
    }

    fun confirmUnfollow() = viewModelScope.launch {
        val target = unfollowDialogTarget ?: return@launch
        actionTargetUid = target.uid
        actionLoading = true
        try {
            val resp = api.userModule.unfollow(UserModule.FollowReq(targetUid = target.uid))
            if (resp.ok) {
                val data = resp.ok()
                followingItems.removeAll { it.user.uid == target.uid }
                // Also remove from follower items' mutual state
                for (i in followerItems.indices) {
                    if (followerItems[i].user.uid == target.uid) {
                        followerItems[i] = followerItems[i].copy(isMutual = false)
                    }
                }
                lastActionResult = LastActionResult(uid = target.uid, isFollowing = false, followerCount = data.followerCount)
                dismissUnfollowDialog()
            } else {
                val err = resp.err()
                global.alert(err.msg)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to unfollow user ${target.uid}", e)
            global.alert(e.message)
        } finally {
            actionLoading = false
            actionTargetUid = null
        }
    }

    fun followUser(uid: Long) = viewModelScope.launch {
        this@FollowViewModel.uid = uid
        actionTargetUid = uid
        actionLoading = true
        try {
            val resp = api.userModule.follow(UserModule.FollowReq(targetUid = uid))
            if (resp.ok) {
                val data = resp.ok()
                // Signal that profile should update — the consuming screen will update UserSpaceViewModel.profile
                lastActionResult = LastActionResult(uid = uid, isFollowing = true, followerCount = data.followerCount)
            } else {
                val err = resp.err()
                global.alert(err.msg)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to follow user $uid", e)
            global.alert(e.message)
        } finally {
            actionLoading = false
            actionTargetUid = null
        }
    }

    fun navigateToSpace(uid: Long) {
        global.requestAppNavigation(
            world.hachimi.app.nav.NavigationRequest.Push(
                world.hachimi.app.nav.Route.Root.PublicUserSpace(uid)
            )
        )
    }

    companion object {
        private const val TAG = "follow"
    }
}
