package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.MomentModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

/** One moment, above its comments. @since 261106 */
@KoinViewModel
class MomentDetailViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
    private val store: MomentStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var status by mutableStateOf(InitializeStatus.INIT)
        private set
    var moment by mutableStateOf<MomentModule.MomentItem?>(null)
        private set
    /** It was deleted, or never existed. */
    var notFound by mutableStateOf(false)
        private set
    private var momentId: Long? = null

    init {
        viewModelScope.launch {
            store.changes.collect { change ->
                when (change) {
                    is MomentChange.Updated -> if (change.item.id == moment?.id) moment = change.item
                    is MomentChange.Deleted -> if (change.id == moment?.id) {
                        moment = null
                        notFound = true
                    }
                    is MomentChange.Posted -> {}
                }
            }
        }
    }

    fun mounted(momentId: Long) {
        if (this.momentId == momentId && status == InitializeStatus.LOADED) return
        this.momentId = momentId
        status = InitializeStatus.INIT
        moment = null
        notFound = false
        load()
    }

    fun retry() {
        status = InitializeStatus.INIT
        load()
    }

    private fun load() {
        val id = momentId ?: return
        viewModelScope.launch {
            try {
                val resp = api.momentModule.detail(MomentModule.MomentIdReq(id), auth = global.isLoggedIn)
                if (resp.ok) {
                    val item = resp.ok<MomentModule.MomentItem>()
                    moment = item
                    store.refreshed(item)
                    status = InitializeStatus.LOADED
                } else {
                    val err = resp.err()
                    if (err.code == MomentModule.ERR_MOMENT_NOT_FOUND) {
                        notFound = true
                        status = InitializeStatus.LOADED
                    } else {
                        global.alert(err.msg)
                        status = InitializeStatus.FAILED
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e("moment_detail", "Failed to load a moment", e)
                global.alert(e.message)
                status = InitializeStatus.FAILED
            }
        }
    }
}
