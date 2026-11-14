package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.change_email_cancelled
import hachimiworld.composeapp.generated.resources.change_email_failed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.WebResult
import world.hachimi.app.api.err
import world.hachimi.app.api.module.AuthModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

private const val TAG = "change_email"

/**
 * Changing the account email, either at once with a code from the old email, or after a wait with
 * the password for users who can't read the old email.
 * @since 261105
 */
@KoinViewModel
class ChangeEmailViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var loading by mutableStateOf(false)
        private set
    var info by mutableStateOf<AuthModule.EmailInfoResp?>(null)
        private set
    var operating by mutableStateOf(false)
        private set

    /** How the user verifies the change, or null while choosing. */
    var method by mutableStateOf<EmailChangeMethod?>(null)
        private set
    /** Whether the user verifies with a code from the old email, else with the password and a wait. */
    val withOldEmail: Boolean get() = method == EmailChangeMethod.OLD_EMAIL
    var newEmail by mutableStateOf("")
    var oldCode by mutableStateOf("")
    var newCode by mutableStateOf("")
    var password by mutableStateOf("")

    var oldCodeRemainSecs by mutableStateOf(-1)
        private set
    var newCodeRemainSecs by mutableStateOf(-1)
        private set

    /** The new email after a change that applied at once, for the success dialog. */
    var changedTo by mutableStateOf<String?>(null)
        private set
    var showCancelConfirm by mutableStateOf(false)

    private var countdownJob: Job? = null

    fun mounted() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                if (oldCodeRemainSecs >= 0) oldCodeRemainSecs -= 1
                if (newCodeRemainSecs >= 0) newCodeRemainSecs -= 1
                delay(1000)
            }
        }
        viewModelScope.launch { load() }
    }

    fun dispose() {
        countdownJob?.cancel()
        countdownJob = null
    }

    fun retry() = viewModelScope.launch { load() }

    private suspend fun load() {
        loading = true
        try {
            val resp = api.authModule.emailInfo()
            if (resp.ok) {
                info = resp.ok()
                initializeStatus = InitializeStatus.LOADED
            } else {
                showError(resp.err())
                if (initializeStatus != InitializeStatus.LOADED) initializeStatus = InitializeStatus.FAILED
            }
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            Logger.e(TAG, "Failed to load email info", e)
            if (initializeStatus != InitializeStatus.LOADED) initializeStatus = InitializeStatus.FAILED
        } finally {
            loading = false
        }
    }

    fun sendOldCode() = run("send old code") {
        api.authModule.sendOldEmailCode().onOk { oldCodeRemainSecs = 60 }
    }

    fun sendNewCode() = run("send new code") {
        api.authModule.sendNewEmailCode(AuthModule.SendNewEmailCodeReq(newEmail.trim()))
            .onOk { newCodeRemainSecs = 60 }
    }

    fun submit() = run("change email") {
        if (withOldEmail) {
            val target = newEmail.trim()
            api.authModule.changeEmail(
                AuthModule.ChangeEmailReq(newEmail = target, oldCode = oldCode.trim(), newCode = newCode.trim())
            ).onOk {
                changedTo = target
                chooseMethod(null)
                load()
            }
        } else {
            api.authModule.requestEmailChange(
                AuthModule.RequestEmailChangeReq(newEmail = newEmail.trim(), newCode = newCode.trim(), password = password)
            ).onOk {
                chooseMethod(null)
                load()
            }
        }
    }

    fun cancelPending() = run("cancel email change") {
        showCancelConfirm = false
        api.authModule.cancelEmailChange().onOk {
            global.alert(Res.string.change_email_cancelled)
            load()
        }
    }

    /** Opens the form for [method], or goes back to choosing with null. */
    fun chooseMethod(method: EmailChangeMethod?) {
        if (method == null) clearForm()
        this.method = method
    }

    fun closeSuccessDialog() {
        changedTo = null
    }

    private fun clearForm() {
        newEmail = ""
        oldCode = ""
        newCode = ""
        password = ""
    }

    private inline fun <reified T> WebResult<T>.onOk(block: (T) -> Unit) {
        if (ok) block(ok()) else showError(err())
    }

    private fun showError(err: CommonError) {
        val message = emailChangeErrorMessage(err.code)
        if (message != null) global.alert(message) else global.alert(err.msg)
    }

    private fun run(action: String, block: suspend () -> Unit) = viewModelScope.launch {
        if (operating) return@launch
        operating = true
        try {
            block()
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            Logger.e(TAG, "Failed to $action", e)
            global.alert(Res.string.change_email_failed)
        } finally {
            operating = false
        }
    }
}

/** @since 261105 */
enum class EmailChangeMethod { OLD_EMAIL, PASSWORD }
