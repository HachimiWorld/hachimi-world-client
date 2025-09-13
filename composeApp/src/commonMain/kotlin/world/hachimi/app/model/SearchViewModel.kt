package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.logging.Logger

class SearchViewModel(
    private val global: GlobalStore,
    private val api: ApiClient
): ViewModel(
    CoroutineScope(Dispatchers.Default)
) {
    var query by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
    val data = mutableStateListOf<SongModule.SearchSongItem>()
    var searchProcessingTimeMs by mutableStateOf(0L)
        private set

    fun mounted(query: String) {
        this.query = query
        search()
    }

    fun dispose() {

    }


    fun search() {
        viewModelScope.launch {
            loading = true
            try {
                val resp = api.songModule.search(SongModule.SearchReq(
                    q = query,
                    limit = null,
                    offset = null,
                    filter = null
                ))
                if (resp.ok) {
                    val data = resp.okData<SongModule.SearchResp>()
                    Snapshot.withMutableSnapshot {
                        this@SearchViewModel.data.clear()
                        this@SearchViewModel.data.addAll(data.hits)
                        searchProcessingTimeMs = data.processingTimeMs
                    }
                } else {
                    val err = resp.errData<CommonError>()
                    global.alert(err.msg)
                }
            } catch (e: Exception) {
                Logger.e("search", "Failed to search", e)
                global.alert(e.message)
            } finally {
                loading = false
            }
        }
    }

}