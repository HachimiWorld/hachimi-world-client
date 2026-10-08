package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.committee_already_member
import hachimiworld.composeapp.generated.resources.committee_operation_failed
import hachimiworld.composeapp.generated.resources.committee_user_not_found
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.CommitteeModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

@KoinViewModel
class CommitteeMembersViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {

    val members = mutableStateListOf<CommitteeModule.MemberItem>()
    var canManage by mutableStateOf(false)
        private set
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    /** The uid typed into the appoint field. */
    var uidInput by mutableStateOf("")
    var working by mutableStateOf(false)
        private set

    fun mounted() {
        if (initializeStatus != InitializeStatus.LOADED) load()
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            initializeStatus = InitializeStatus.INIT
            try {
                val me = api.committeeModule.me()
                if (!me.ok) error(me.err().msg)
                canManage = me.ok().canResolve
                fetch()
                initializeStatus = InitializeStatus.LOADED
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load committee members", e)
                initializeStatus = InitializeStatus.FAILED
            }
        }
    }

    private suspend fun fetch() {
        val resp = api.committeeModule.members()
        if (!resp.ok) error(resp.err().msg)
        members.clear()
        members.addAll(resp.ok().members)
    }

    fun appoint() {
        val uid = uidInput.trim().toLongOrNull() ?: return
        run {
            val resp = api.committeeModule.appoint(CommitteeModule.MemberReq(uid))
            if (resp.ok) {
                uidInput = ""
                fetch()
            } else when (resp.err().code) {
                "user_not_found" -> global.alert(Res.string.committee_user_not_found)
                "already_member" -> global.alert(Res.string.committee_already_member)
                else -> global.alert(resp.err().msg)
            }
        }
    }

    fun revoke(uid: Long) {
        run {
            val resp = api.committeeModule.revoke(CommitteeModule.MemberReq(uid))
            if (resp.ok) fetch() else global.alert(resp.err().msg)
        }
    }

    private fun run(block: suspend () -> Unit) {
        if (working) return
        working = true
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Committee operation failed", e)
                global.alert(Res.string.committee_operation_failed)
            } finally {
                working = false
            }
        }
    }

    companion object {
        private const val TAG = "committee_members"
    }
}
