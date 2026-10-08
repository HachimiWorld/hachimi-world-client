package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.CommitteeModule
import world.hachimi.app.api.module.ReportModule
import world.hachimi.app.api.module.ReportModule.CaseItem
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

/** The committee center: what the user may do, and the report queue. */
@KoinViewModel
class CommitteeViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {

    var access by mutableStateOf<CommitteeModule.MeResp?>(null)
        private set
    /** [ReportModule.STATUS_PENDING] or [ReportModule.STATUS_RESOLVED] */
    var status by mutableStateOf(ReportModule.STATUS_PENDING)
        private set
    /** Most recently reported first. */
    val items = mutableStateListOf<CaseItem>()
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var hasMore by mutableStateOf(false)
        private set
    var loadingMore by mutableStateOf(false)
        private set
    var loadMoreFailed by mutableStateOf(false)
        private set

    private var loadJob: Job? = null

    /** Reloads on every visit, so decisions made in a case show up when coming back. */
    fun mounted() {
        if (!global.isLoggedIn) return
        load()
    }

    fun retry() = load()

    fun selectStatus(value: String) {
        if (value == status) return
        status = value
        load()
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (items.isEmpty()) initializeStatus = InitializeStatus.INIT
            try {
                val me = api.committeeModule.me()
                if (!me.ok) error(me.err().msg)
                access = me.ok()
                if (access?.canView == true) {
                    val (page, more) = fetch(null)
                    items.clear()
                    items.addAll(page)
                    hasMore = more
                }
                initializeStatus = InitializeStatus.LOADED
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load the committee center", e)
                initializeStatus = InitializeStatus.FAILED
            }
        }
    }

    fun loadMore() {
        if (initializeStatus != InitializeStatus.LOADED || !hasMore || loadingMore) return
        val last = items.lastOrNull() ?: return
        viewModelScope.launch {
            loadingMore = true
            loadMoreFailed = false
            try {
                val (page, more) = fetch(last)
                val known = items.mapTo(HashSet()) { it.caseId }
                items.addAll(page.filter { it.caseId !in known })
                hasMore = more
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load more cases", e)
                loadMoreFailed = true
            } finally {
                loadingMore = false
            }
        }
    }

    private suspend fun fetch(after: CaseItem?): Pair<List<CaseItem>, Boolean> {
        val resp = api.reportModule.queue(
            ReportModule.QueueReq(
                status = status,
                beforeTime = after?.lastReportTime,
                beforeId = after?.caseId,
                limit = PAGE_SIZE,
            )
        )
        if (!resp.ok) error(resp.err().msg)
        val data = resp.ok()
        return data.items to data.hasMore
    }

    companion object {
        private const val TAG = "committee"
        private const val PAGE_SIZE = 20
    }
}
