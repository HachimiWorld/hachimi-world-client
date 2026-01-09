package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.module.AuthModule
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger
import world.hachimi.app.nav.Route

private const val TAG = "forget_password"

class ForgetPasswordViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var operating by mutableStateOf(false)
        private set
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordRepeat by mutableStateOf("")
    var verifyCode by mutableStateOf("")
    var codeRemainSecs by mutableStateOf(-1)
        private set
    var showSuccessDialog by mutableStateOf(false)
        private set
    var showCaptchaDialog by mutableStateOf(false)
        private set
    private var captchaKey: String? = null

    fun mounted() {
        startCountdownJob()
    }

    fun dispose() {
        countdownJob?.cancel()
        countdownJob = null
    }

    fun sendVerifyCode() = viewModelScope.launch {
        try {
            operating = true
            val resp = api.authModule.sendEmailCode(AuthModule.SendEmailCodeReq(email))
            if (resp.ok) {
                codeRemainSecs = 60
            } else {
                val err = resp.errData<CommonError>()
                global.alert(err.msg)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to send verify code", e)
            global.alert("发送验证码失败，请稍后再试")
        } finally {
            operating = false
        }
    }

    fun submit() = viewModelScope.launch {
        generateCaptcha()
    }

    fun captchaContinue() = viewModelScope.launch {
        try {
            operating = true
            val resp = api.authModule.resetPassword(AuthModule.ResetPasswordReq(
                email = email,
                code = verifyCode,
                newPassword = password,
                logoutAllDevices = false,
                captchaKey = captchaKey!!
            ))
            if (resp.ok) {
                showSuccessDialog = true
            } else {
                val err = resp.errData<CommonError>()
                global.alert(err.msg)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to reset password", e)
            global.alert("重置密码失败，请稍后再试")
        } finally {
            showCaptchaDialog = false
            operating = false
        }
    }

    fun closeDialog() {
        global.nav.replace(Route.Auth(true))
    }

    private suspend fun generateCaptcha() {
        operating = true
        captchaKey = null
        try {
            val resp = api.authModule.generateCaptcha()
            if (resp.ok) {
                val data = resp.okData<AuthModule.GenerateCaptchaResp>()
                captchaKey = data.captchaKey
                try {
                    getPlatform().openUrl(data.url)
                    showCaptchaDialog = true
                } catch (e: Throwable) {
                    Logger.e(TAG, "Failed to open captcha url", e)
                    return
                }
            } else {
                val err = resp.errData<CommonError>()
                global.alert(err.msg)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to generate captcha", e)
            global.alert("人机验证失败，请稍后重试")
        } finally {
            operating = false
        }
    }

    private var countdownJob: Job? = null

    private fun startCountdownJob() {
        countdownJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                codeRemainSecs -= 1
                delay(1000)
            }
        }
    }
}