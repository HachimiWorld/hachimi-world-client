package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import kotlin.random.Random
import kotlin.reflect.KMutableProperty0
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class HomeViewModel(
    private val api: ApiClient,
    private val global: GlobalStore
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var recentStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var recentSongs by mutableStateOf<List<SongModule.PublicSongDetail>>(emptyList())
        private set
    var recentLoading by mutableStateOf(false)
        private set
    var recommendStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var recommendSongs by mutableStateOf<List<SongModule.PublicSongDetail>>(emptyList())
        private set
    var recommendLoading by mutableStateOf(false)
        private set
    var hotStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var hotSongs by mutableStateOf<List<SongModule.PublicSongDetail>>(emptyList())
        private set
    var hotLoading by mutableStateOf(false)
        private set
    var pureStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var pureSongs by mutableStateOf<List<SongModule.SearchSongItem>>(emptyList())
        private set
    var pureLoading by mutableStateOf(false)
        private set

    private var lastRefreshTime: Instant = Clock.System.now()

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
        }, async {
            loadHot()
        }, async {
            loadCategories()
        })
    }

    fun fakeRefresh() {
        // This is used to treat obsessive-compulsive disorder (OCD).
        val now = Clock.System.now()
        if (now - lastRefreshTime > 10.minutes) {
            viewModelScope.launch {
                init()
                lastRefreshTime = now
            }
        } else {
            viewModelScope.launch {
                refreshing = true
                delay(Random.nextLong(1000, 5000))
                refreshing = false
            }
        }
    }

    fun retryRecommend() {
        if (recommendStatus == InitializeStatus.FAILED) {
            recommendStatus = InitializeStatus.INIT
            viewModelScope.launch {
                loadRecommend()
            }
        }
    }

    fun retryRecent() {
        if (recentStatus == InitializeStatus.FAILED) {
            recentStatus = InitializeStatus.INIT
            viewModelScope.launch {
                loadRecent()
            }
        }
    }

    fun retryHot() {
        if (hotStatus == InitializeStatus.FAILED) {
            hotStatus = InitializeStatus.INIT
            viewModelScope.launch {
                loadHot()
            }
        }
    }

    fun retryPure() {
        if (pureStatus == InitializeStatus.FAILED) {
            pureStatus = InitializeStatus.INIT
            viewModelScope.launch {
                loadCategories()
            }
        }
    }

    suspend fun loadRecent() {
        recentLoading = true
        try {
            val resp = api.songModule.recentV2(SongModule.RecentReq(null, 12, false))
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

    suspend fun loadHot() {
        withStatus(::hotLoading, ::hotStatus) {
            val resp = api.songModule.hotWeekly()
            if (resp.ok) {
                hotSongs = resp.ok().songs.take(12)
                true
            } else {
                global.alert(resp.err().msg)
                false
            }
        }
    }

    suspend fun loadCategories() {
        // Category
        withStatus(::pureLoading, ::pureStatus) {
            val resp = api.songModule.search(
                SongModule.SearchReq(
                    q = "",
                    limit = 12,
                    offset = null,
                    filter = "tags = \"纯净哈基米\""
                )
            )
            if (resp.ok) {
                pureSongs = resp.ok().hits.take(12)
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