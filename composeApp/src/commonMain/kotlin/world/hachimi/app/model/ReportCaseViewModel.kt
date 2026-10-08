package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.report_resolve_failed
import hachimiworld.composeapp.generated.resources.report_resolved
import hachimiworld.composeapp.generated.resources.report_resolved_with_new
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.ReportModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

/** One report case: the target, its pending reports and past decisions, and the decision form. */
@KoinViewModel
class ReportCaseViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {

    var case by mutableStateOf<ReportModule.CaseResp?>(null)
        private set
    var canResolve by mutableStateOf(false)
        private set
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set

    var verdict by mutableStateOf<String?>(null)
        private set
    /** Content actions chosen for [verdict]. */
    var contentActions by mutableStateOf<Set<String>>(emptySet())
        private set
    /** Shown to the owner; required for penalties. */
    var authorReason by mutableStateOf("")
    var note by mutableStateOf("")
    var ignoreReports by mutableStateOf(false)
    var resolving by mutableStateOf(false)
        private set

    /** Content actions that go with the chosen verdict. */
    val actionsForVerdict: List<String>
        get() = case?.contentActions.orEmpty().filter { it.verdict == verdict }.map { it.action }

    val needsAuthorReason: Boolean
        get() = contentActions.any { it != ReportModule.ACTION_RESTORE }

    val canSubmit: Boolean
        get() {
            val v = verdict ?: return false
            if (resolving) return false
            // Upholding must do something, unless there is nothing left to do (already hidden)
            if (v == ReportModule.VERDICT_AGREE && contentActions.isEmpty() && actionsForVerdict.isNotEmpty()) return false
            if (needsAuthorReason && authorReason.isBlank()) return false
            return true
        }

    fun selectVerdict(value: String) {
        if (value == verdict) return
        verdict = value
        contentActions = emptySet()
    }

    fun toggleAction(action: String) {
        contentActions = if (action in contentActions) contentActions - action else contentActions + action
    }

    private var targetType = ""
    private var targetId = 0L

    fun mounted(targetType: String, targetId: Long) {
        if (this.targetType == targetType && this.targetId == targetId && initializeStatus != InitializeStatus.FAILED) return
        this.targetType = targetType
        this.targetId = targetId
        load()
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            initializeStatus = InitializeStatus.INIT
            try {
                fetch()
                val me = api.committeeModule.me()
                if (!me.ok) error(me.err().msg)
                canResolve = me.ok().canResolve
                initializeStatus = InitializeStatus.LOADED
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load the report case", e)
                initializeStatus = InitializeStatus.FAILED
            }
        }
    }

    private suspend fun fetch() {
        val resp = api.reportModule.case(ReportModule.CaseReq(targetType, targetId))
        if (!resp.ok) error(resp.err().msg)
        case = resp.ok()
        verdict = null
        contentActions = emptySet()
        authorReason = ""
        note = ""
        ignoreReports = false
    }

    fun resolve() {
        val current = case ?: return
        val verdict = verdict ?: return
        if (!canSubmit) return
        resolving = true
        viewModelScope.launch {
            try {
                val resp = api.reportModule.resolve(
                    ReportModule.ResolveReq(
                        targetType = targetType,
                        targetId = targetId,
                        verdict = verdict,
                        note = note.trim().ifEmpty { null },
                        contentActions = contentActions.toList(),
                        authorReason = authorReason.trim().ifEmpty { null },
                        ignoreReports = ignoreReports,
                        upToReportId = current.pendingReports.maxOfOrNull { it.reportId } ?: 0,
                    )
                )
                if (!resp.ok) {
                    global.alert(resp.err().msg)
                    return@launch
                }
                val pending = resp.ok().pendingCount
                if (pending > 0) global.alert(Res.string.report_resolved_with_new, pending)
                else global.alert(Res.string.report_resolved)
                fetch()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to resolve the report case", e)
                global.alert(Res.string.report_resolve_failed)
            } finally {
                resolving = false
            }
        }
    }

    companion object {
        private const val TAG = "report_case"
    }
}
