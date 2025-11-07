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

class RecommendViewModel(
    private val api: ApiClient,
    private val global: GlobalStore
): ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
    var loading by mutableStateOf(false)
    var songs by mutableStateOf<List<SongModule.PublicSongDetail>>(emptyList())
    fun mounted() {
        if (initializeStatus == InitializeStatus.INIT) {
            init()
        }
    }

    fun unmount() {

    }

    fun init() = viewModelScope.launch {
        loading = true
        try {
            val resp = if (global.isLoggedIn) api.songModule.recommend() else api.songModule.recommendAnonymous()
            if (resp.ok) {
                val data = resp.ok().songs
                songs = data
                initializeStatus = InitializeStatus.LOADED
            } else {
                val err = resp.err()
                global.alert(err.msg)
                initializeStatus = InitializeStatus.FAILED
            }
        } catch (e: Throwable) {
            Logger.e("recommend", "Failed to get recommend song list", e)
            initializeStatus = InitializeStatus.FAILED
            global.alert(e.message)
        } finally {
            loading = false
        }
    }

    fun retry() {
        if (initializeStatus == InitializeStatus.FAILED) {
            initializeStatus = InitializeStatus.INIT
            init()
        }
    }

}