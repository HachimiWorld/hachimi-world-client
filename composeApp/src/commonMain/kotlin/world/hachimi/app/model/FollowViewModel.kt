package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

enum class FollowListType { FOLLOWING, FOLLOWERS }

@KoinViewModel
class FollowViewModel(
    @InjectedParam
    val listType: FollowListType,
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {

    val followingItems = mutableStateListOf<UserModule.FollowingItem>()
    val followerItems = mutableStateListOf<UserModule.FollowerItem>()

    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    /** First load or silent refresh. */
    var loading by mutableStateOf(false)
        private set
    var loadingMore by mutableStateOf(false)
        private set
    var hasMore by mutableStateOf(true)
        private set
    /** Last init/refresh failure message (load-more uses alert). */
    var errorData by mutableStateOf<String?>(null)
        private set

    private var nextCursor: String? = null
    private var loadJob: Job? = null
    private var loadMoreJob: Job? = null

    // Unfollow action (list)
    var actionTargetUid by mutableStateOf<Long?>(null)
        private set
    var actionLoading by mutableStateOf(false)
        private set

    data class UnfollowTarget(val uid: Long, val username: String)
    var unfollowDialogTarget: UnfollowTarget? by mutableStateOf(null)
        private set

    /**
     * Call when the screen is shown.
     * - INIT: first load
     * - LOADED: silent refresh (keep list + [RefreshingIndicator])
     * - FAILED: wait for [retry] (or remount after process death → INIT again)
     */
    fun mounted() {
        when (initializeStatus) {
            InitializeStatus.INIT -> init()
            InitializeStatus.LOADED -> refresh()
            InitializeStatus.FAILED -> Unit
        }
    }

    fun dispose() {
        // store-owned; jobs cancel in onCleared
    }

    fun retry() {
        if (initializeStatus == InitializeStatus.FAILED) {
            init()
        }
    }

    /** Silent refresh after data is already shown. */
    fun refresh() {
        if (initializeStatus != InitializeStatus.LOADED) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                load(reset = true)
                errorData = null
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to refresh ${listType.name}", e)
                errorData = e.message
                global.alert(e.message)
            }
        }
    }

    fun loadMore() {
        if (initializeStatus != InitializeStatus.LOADED) return
        if (!hasMore || loadingMore || loading) return
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            loadingMore = true
            try {
                load(reset = false)
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load more ${listType.name}", e)
                global.alert(e.message ?: "加载更多失败")
            } finally {
                loadingMore = false
            }
        }
    }

    private fun init() {
        loadJob?.cancel()
        loadMoreJob?.cancel()
        loadJob = viewModelScope.launch {
            initializeStatus = InitializeStatus.INIT
            errorData = null
            try {
                load(reset = true)
                initializeStatus = InitializeStatus.LOADED
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to init ${listType.name}", e)
                errorData = e.message
                initializeStatus = InitializeStatus.FAILED
            }
        }
    }

    /**
     * Fetch a page. On [reset], clears list and cursor (first page / refresh).
     * Propagates errors for [init]/[refresh]/[loadMore] to handle.
     * [loading] is only toggled for reset; load-more uses [loadingMore] outside.
     */
    private suspend fun load(reset: Boolean) {
        if (reset) {
            loading = true
            nextCursor = null
            hasMore = true
        }
        try {
            fetchPage(reset)
        } finally {
            if (reset) loading = false
        }
    }

    private suspend fun fetchPage(reset: Boolean) {
        when (listType) {
            FollowListType.FOLLOWING -> {
                val resp = api.userModule.following(
                    UserModule.FollowingReq(after = nextCursor, limit = 20)
                )
                if (resp.ok) {
                    val data = resp.ok()
                    if (reset) followingItems.clear()
                    followingItems.addAll(data.items)
                    nextCursor = data.nextCursor
                    hasMore = data.nextCursor != null
                } else {
                    throw RuntimeException(resp.err().msg)
                }
            }
            FollowListType.FOLLOWERS -> {
                val resp = api.userModule.followers(
                    UserModule.FollowersReq(after = nextCursor, limit = 20)
                )
                if (resp.ok) {
                    val data = resp.ok()
                    if (reset) followerItems.clear()
                    followerItems.addAll(data.items)
                    nextCursor = data.nextCursor
                    hasMore = data.nextCursor != null
                } else {
                    throw RuntimeException(resp.err().msg)
                }
            }
        }
    }

    fun showUnfollowDialog(item: UserModule.FollowingItem) {
        unfollowDialogTarget = UnfollowTarget(item.user.uid, item.user.username)
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
                followingItems.removeAll { it.user.uid == target.uid }
                for (i in followerItems.indices) {
                    if (followerItems[i].user.uid == target.uid) {
                        followerItems[i] = followerItems[i].copy(isMutual = false)
                    }
                }
                dismissUnfollowDialog()
            } else {
                global.alert(resp.err().msg)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to unfollow user ${target.uid}", e)
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

    override fun onCleared() {
        loadJob?.cancel()
        loadMoreJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val TAG = "follow"
    }
}
