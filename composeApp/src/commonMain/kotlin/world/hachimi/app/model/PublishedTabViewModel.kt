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
import world.hachimi.app.api.err
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

class PublishedTabViewModel(
    private val api: ApiClient,
    private val global: GlobalStore
): ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var loading by mutableStateOf(false)
        private set
    var total by mutableStateOf(0L)
        private set
    var currentPage by mutableStateOf(0)
        private set
    var pageSize by mutableStateOf(30)
        private set
    var songs by mutableStateOf<List<SongModule.PublicSongDetail>>(emptyList())
        private set

    fun mounted() {
        if (initializeStatus == InitializeStatus.INIT) {
            // Init
            load()
        } else {
            // Refresh
            load()
        }
    }

    fun dispose() {

    }

    fun retry() {
        if (initializeStatus == InitializeStatus.FAILED) {
            initializeStatus = InitializeStatus.INIT
            load()
        }
    }

    fun load() {
        loading = true
        viewModelScope.launch {
            try {
                val resp = api.songModule.pageByUser(
                    SongModule.PageByUserReq(
                        global.userInfo!!.uid,
                        currentPage.toLong(),
                        pageSize.toLong()
                    )
                )
                if (resp.ok) {
                    val data = resp.ok()
                    total = data.total
                    songs = data.songs
                    if (initializeStatus == InitializeStatus.INIT) {
                        initializeStatus = InitializeStatus.LOADED
                    }
                } else {
                    val err = resp.err()
                    global.alert(err.msg)
                    if (initializeStatus == InitializeStatus.INIT) {
                        initializeStatus = InitializeStatus.FAILED
                    }
                }
            } catch (e: Throwable) {
                Logger.e("published", "Failed to fetch published songs", e)
                global.alert(e.message)
                if (initializeStatus == InitializeStatus.INIT) {
                    initializeStatus = InitializeStatus.FAILED
                }
            } finally {
                loading = false
            }
        }
    }

    fun setPage(pageSize: Int, page: Int) {
        this.pageSize = pageSize
        this.currentPage = page
        load()
    }
}