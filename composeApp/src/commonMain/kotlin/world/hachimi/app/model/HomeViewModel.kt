package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty0

class HomeViewModel(
    private val api: ApiClient,
    private val global: GlobalStore
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var recentStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var recentSongs by mutableStateOf<List<SongModule.PublicSongDetail>>(emptyList())
    var recentLoading by mutableStateOf(false)
        private set
    var recommendStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var recommendSongs by mutableStateOf<List<SongModule.PublicSongDetail>>(emptyList())
    var recommendLoading by mutableStateOf(false)
        private set

    fun mounted() {
        if (recentStatus == InitializeStatus.INIT) {
            init()
        }
    }

    fun unmount() {

    }

    private fun init() = viewModelScope.launch {
        awaitAll(async {
            loadRecommend()
        }, async {
            loadRecent()
        })
    }

    fun fakeRefresh() {

    }

    fun retryRecommend() {
        if (recommendStatus == InitializeStatus.FAILED) {
            recommendStatus = InitializeStatus.INIT

        }
    }

    fun retryRecent() {
        if (recentStatus == InitializeStatus.FAILED) {
        }
    }

    suspend fun loadRecent() {
        recentLoading = true
        try {
            val resp = api.songModule.recentV2()
            if (resp.ok) {
                recentSongs = resp.ok().songs.take(12)
                recentStatus = InitializeStatus.LOADED
            } else {
                global.alert(resp.err().msg)
                recentStatus = InitializeStatus.FAILED
            }
        } catch (e: Throwable) {
            Logger.e("home", "Failed to fetch recent songs", e)
            recentStatus = InitializeStatus.FAILED
        } finally {
            recentLoading = false
        }
    }

    suspend fun loadRecommend() {
        /*try {
            recommendLoading = true
            val resp = if (global.isLoggedIn) api.songModule.recommend() else api.songModule.recommendAnonymous()
            if (resp.ok) {
                recommendSongs = resp.ok().songs
                recommendStatus = InitializeStatus.LOADED
            } else {
                global.alert(resp.err().msg)
                recommendStatus = InitializeStatus.FAILED
            }
        } catch (e: Throwable) {
            Logger.e("home", "Failed to fetch recommend songs", e)
            recommendStatus = InitializeStatus.FAILED
        } finally {
            recommendLoading = false
        }
*/

        withStatus(::recommendLoading, ::recommendStatus) {
            val resp = if (global.isLoggedIn) api.songModule.recommend() else api.songModule.recommendAnonymous()
            if (resp.ok) {
                recommendSongs = resp.ok().songs.take(12)
                true
            } else {
                global.alert(resp.err().msg)
                false
            }
        }
    }

    private suspend fun withStatus(
        loadingStatus: KMutableProperty0<Boolean>,
        initializeStatus: KMutableProperty0<InitializeStatus>,
        block: suspend () -> Boolean
    ) {
        loadingStatus.set(true)
        try {
            val r = block()
            if (initializeStatus.get() == InitializeStatus.INIT) {
                initializeStatus.set(if (r) InitializeStatus.LOADED else InitializeStatus.FAILED)
            }
        } catch (e: Throwable) {
            Logger.e("home", "Failed to fetch recommend songs", e)
            initializeStatus.set(InitializeStatus.FAILED)
        } finally {
            loadingStatus.set(false)
        }
    }
}