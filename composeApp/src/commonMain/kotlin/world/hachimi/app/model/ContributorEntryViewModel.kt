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
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

private const val TAG = "ContributorEntryVM"

class ContributorEntryViewModel(
    private val api: ApiClient,
    private val global: GlobalStore
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initStat by mutableStateOf(InitializeStatus.INIT)
        private set

    var isContributor by mutableStateOf(false)
        private set

    fun mounted() {
        if (initStat == InitializeStatus.INIT) {
            checkContributor()
        }
    }

    fun dispose() {
        // no-op for now
    }

    fun retry() {
        if (initStat == InitializeStatus.FAILED) {
            initStat = InitializeStatus.INIT
            checkContributor()
        }
    }

    private fun checkContributor() = viewModelScope.launch {
        initStat = InitializeStatus.INIT
        try {
            val resp = api.contributorModule.check()
            if (resp.ok) {
                val data = resp.ok()
                isContributor = data.isContributor
                initStat = InitializeStatus.LOADED
            } else {
                val err = resp.err()
                Logger.w(TAG, "checkContributor: backend returned error: ${err.code} ${err.msg}")
                global.alert(err.msg)
                initStat = InitializeStatus.FAILED
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "checkContributor failed", e)
            global.alert("检查贡献者身份失败: ${e.message}")
            initStat = InitializeStatus.FAILED
        }
    }
}
