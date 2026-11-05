package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.auth_revert_email_failed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.AuthModule
import world.hachimi.app.logging.Logger
import world.hachimi.app.nav.NavigationRequest
import world.hachimi.app.nav.Route

private const val TAG = "revert_email_change"

/**
 * Follows the link sent to the old email when the account email changes: puts the old email back
 * and sets a new password. Every device is logged out, this one included.
 * @since 261105
 */
@KoinViewModel
class RevertEmailChangeViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    private val _navigationRequests = MutableSharedFlow<NavigationRequest>(
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navigationRequests = _navigationRequests.asSharedFlow()

    var operating by mutableStateOf(false)
        private set
    var password by mutableStateOf("")
    var passwordRepeat by mutableStateOf("")
    var showSuccessDialog by mutableStateOf(false)
        private set

    fun submit(token: String) = viewModelScope.launch {
        if (operating) return@launch
        operating = true
        try {
            val resp = api.authModule.revertEmailChange(AuthModule.RevertEmailChangeReq(token = token, newPassword = password))
            if (resp.ok) {
                // The server logged out every device
                if (global.isLoggedIn) global.logout(navigateHome = false).join()
                showSuccessDialog = true
            } else {
                val err = resp.err()
                val message = emailChangeErrorMessage(err.code)
                if (message != null) global.alert(message) else global.alert(err.msg)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to revert email change", e)
            global.alert(Res.string.auth_revert_email_failed)
        } finally {
            operating = false
        }
    }

    fun closeDialog() {
        showSuccessDialog = false
        _navigationRequests.tryEmit(NavigationRequest.Replace(listOf(Route.Auth(true))))
    }
}
