package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.need_login_message
import hachimiworld.composeapp.generated.resources.report_already_reported
import hachimiworld.composeapp.generated.resources.report_already_reviewed
import hachimiworld.composeapp.generated.resources.report_rate_limited
import hachimiworld.composeapp.generated.resources.report_submit_failed
import hachimiworld.composeapp.generated.resources.report_submitted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.ReportModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

/**
 * The report dialog, opened from any song, playlist or user and drawn once by the root screen.
 */
@Singleton
class ReportStore(
    private val api: ApiClient,
    private val global: GlobalStore,
) {
    data class Target(val type: String, val id: Long)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Non-null while the dialog is shown. */
    var target by mutableStateOf<Target?>(null)
        private set
    var reason by mutableStateOf<String?>(null)
    var detail by mutableStateOf("")
    var submitting by mutableStateOf(false)
        private set

    val canSubmit: Boolean
        get() = !submitting && reason != null &&
            (reason != ReportModule.REASON_OTHER || detail.isNotBlank())

    fun open(type: String, id: Long) {
        if (!global.isLoggedIn) {
            global.alert(Res.string.need_login_message)
            return
        }
        reason = null
        detail = ""
        target = Target(type, id)
    }

    fun dismiss() {
        if (!submitting) target = null
    }

    fun submit() {
        val target = target ?: return
        val reason = reason ?: return
        if (!canSubmit) return
        submitting = true
        scope.launch {
            try {
                val resp = api.reportModule.submit(
                    ReportModule.SubmitReq(target.type, target.id, reason, detail.trim().ifEmpty { null })
                )
                if (resp.ok) {
                    global.alert(if (resp.ok().alreadyReviewed) Res.string.report_already_reviewed else Res.string.report_submitted)
                    this@ReportStore.target = null
                } else {
                    when (resp.err().code) {
                        "already_reported" -> {
                            global.alert(Res.string.report_already_reported)
                            this@ReportStore.target = null
                        }
                        "rate_limited" -> global.alert(Res.string.report_rate_limited)
                        else -> global.alert(resp.err().msg)
                    }
                }
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to submit report", e)
                global.alert(Res.string.report_submit_failed)
            } finally {
                submitting = false
            }
        }
    }

    companion object {
        private const val TAG = "ReportStore"
    }
}
