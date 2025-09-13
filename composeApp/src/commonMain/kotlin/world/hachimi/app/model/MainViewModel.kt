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

class MainViewModel(
    private val apiClient: ApiClient,
    private val global: GlobalStore
): ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var songs by mutableStateOf(emptyList<SongModule.DetailResp>())
        private set

    fun mounted() {
        if (initializeStatus == InitializeStatus.INIT) {
            getRecommendSongs()
        }
    }

    fun unmount() {

    }

    fun retry() {
        initializeStatus = InitializeStatus.INIT
        getRecommendSongs()
    }

    private fun getRecommendSongs() {
        viewModelScope.launch(Dispatchers.Default) {
            isLoading = true

            try {
                val resp = apiClient.songModule.recentV2();
                if (resp.ok) {
                    val data = resp.ok()
                    songs = data.songs
                    if (initializeStatus == InitializeStatus.INIT) {
                        initializeStatus = InitializeStatus.LOADED
                    }
                } else {
                    global.alert(resp.err().msg)
                    if (initializeStatus == InitializeStatus.INIT) {
                        initializeStatus = InitializeStatus.FAILED
                    }
                }
            } catch (e: Exception) {
                Logger.e("home", "Failed to get recommend songs", e)
                global.alert("获取推荐音乐失败：${e.message}")
                if (initializeStatus == InitializeStatus.INIT) {
                    initializeStatus = InitializeStatus.FAILED
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun refresh() {
        getRecommendSongs()
    }
}