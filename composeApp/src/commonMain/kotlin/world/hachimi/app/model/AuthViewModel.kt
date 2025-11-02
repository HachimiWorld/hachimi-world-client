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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.module.AuthModule
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.api.ok
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger
import world.hachimi.app.nav.Route
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferencesKeys
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class AuthViewModel(
    private val api: ApiClient,
    private val dataStore: MyDataStore,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var isOperating by mutableStateOf(false)
        private set

    var email by mutableStateOf("")
    var password by mutableStateOf("")

    var regStep by mutableStateOf(0)
    var regEmail by mutableStateOf("")
    var regPassword by mutableStateOf("")
    var regPasswordRepeat by mutableStateOf("")
    var regCode by mutableStateOf("")

    var regCodeRemainSecs by mutableStateOf(-1)
        private set


    var uid by mutableStateOf("")
        private set
    var name by mutableStateOf("")
    var intro by mutableStateOf("")
    var gender by mutableStateOf<Int?>(null)

    var showCaptchaDialog by mutableStateOf(false)

    fun mounted() {
        startCountdownJob()
    }

    fun unmount() {
        clearInput()
        countdownJob?.cancel()
        countdownJob = null
    }

    private fun clearInput() {
        email = ""
        password = ""
        regStep = 0
        regEmail = ""
        regPassword = ""
        regPasswordRepeat = ""
        regCode = ""
    }

    private var countdownJob: Job? = null

    private fun startCountdownJob() {
        countdownJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                regCodeRemainSecs -= 1
                delay(1000)
            }
        }
    }

    fun regSendEmailCode() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                isOperating = true

                val result = api.authModule.sendEmailCode(AuthModule.SendEmailCodeReq(email = regEmail))
                if (!result.ok) {
                    val errData = result.errData<CommonError>()
                    global.alert(errData.msg)
                    return@launch
                }
                regCodeRemainSecs = 60
            } catch (e: Throwable) {
                Logger.e("auth", "Failed to send email code", e)
                global.alert(e.message)
            } finally {
                isOperating = false
            }
        }
    }

    fun regNextStep() {
        if (regStep == 0) {
            // Validate
            if (!regEmail.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))) {
                global.alert("邮箱地址不正确")
                return
            }

            if (regPassword.length < 8) {
                global.alert("密码长度至少为8位")
                return
            }

            regSendEmailCode()
            regStep = 1
        } else if (regStep == 1) {
            register()
        }
    }

    fun finishRegister() {
        viewModelScope.launch {
            isOperating = true
            try {
                val resp = api.userModule.updateProfile(
                    UserModule.UpdateProfileReq(
                        username = name,
                        bio = intro,
                        gender = gender.takeUnless { it == 2 }, // Map 2 to null
                    )
                )
                if (resp.ok) {
                    dataStore.set(PreferencesKeys.USER_NAME, name)
                    global.setLoginUser(uid.toLong(), name, null, false)
                    global.nav.replace(Route.Root.Home.Main)
                } else {
                    global.alert(resp.errData<CommonError>().msg)
                }
            } catch (e: Throwable) {
                global.alert(e.message)
            } finally {
                isOperating = false
            }
        }

    }

    fun skipProfile() {
        global.nav.replace(Route.Root.Home.Main)
    }

    private suspend fun doLogin() {
        try {
            isOperating = true
            val resp = api.authModule.loginEmail(
                AuthModule.LoginReq(
                    email = email,
                    password = password,
                    code = null,
                    deviceInfo = "Desktop Client",
                    captchaKey = captchaKey!!
                )
            )
            if (resp.ok) {
                val data = resp.okData<AuthModule.LoginResp>()

                // Set token
                api.setToken(data.token.accessToken, data.token.refreshToken)
                dataStore.set(PreferencesKeys.AUTH_ACCESS_TOKEN, data.token.accessToken)
                dataStore.set(PreferencesKeys.AUTH_REFRESH_TOKEN, data.token.refreshToken)

                val profile = api.userModule.profile(data.uid)
                if (!profile.ok) {
                    global.alert(profile.errData<CommonError>().msg)
                    return
                }
                val profileData = profile.ok()
                // Save login status
                dataStore.set(PreferencesKeys.USER_UID, profileData.uid)
                dataStore.set(PreferencesKeys.USER_NAME, profileData.username)
                global.setLoginUser(data.uid, data.username, profileData.avatarUrl, false)
                global.nav.replace(Route.Root.Home.Main)
            } else {
                global.alert(resp.errData<CommonError>().msg)
            }
        } catch (e: Throwable) {
            Logger.e("auth", "Failed to login", e)
            global.alert(e.message)
        } finally {
            isOperating = false
        }
    }

    private suspend fun doRegister() = withContext(Dispatchers.Default) {
        try {
            isOperating = true
            val resp = api.authModule.registerEmail(
                AuthModule.RegisterReq(
                    email = regEmail,
                    password = regPassword,
                    code = regCode,
                    deviceInfo = "Desktop Client", // TODO
                    captchaKey = captchaKey!!
                )
            )
            if (resp.ok) {
                val data: AuthModule.RegisterResp = resp.okData()
                uid = data.uid.toString()
                name = data.generatedUsername

                regStep = 2

                // Set token to the api client
                api.setToken(data.token.accessToken, data.token.refreshToken)

                // Save token
                dataStore.set(PreferencesKeys.AUTH_ACCESS_TOKEN, data.token.accessToken)
                dataStore.set(PreferencesKeys.AUTH_REFRESH_TOKEN, data.token.refreshToken)

                // Set login user status
                dataStore.set(PreferencesKeys.USER_UID, data.uid)
                dataStore.set(PreferencesKeys.USER_NAME, data.generatedUsername)
                global.setLoginUser(data.uid, data.generatedUsername, null, false)
            } else {
                global.alert(resp.errData<CommonError>().msg)
            }
        } catch (e: Throwable) {
            Logger.e("auth", "Failed to register", e)
            global.alert(e.message)
        } finally {
            isOperating = false
        }
    }

    private var captchaKey: String? = null

    private suspend fun generateCaptcha() {
        isOperating = true
        captchaKey = null

        try {
            val resp = api.authModule.generateCaptcha()
            if (resp.ok) {
                val data = resp.okData<AuthModule.GenerateCaptchaResp>()
                captchaKey = data.captchaKey
                try {
                    getPlatform().openUrl(data.url)
                } catch (e: Throwable) {
                    Logger.e("auth", "Failed to open captcha url", e)
                    return
                }
            } else {
                val err = resp.errData<CommonError>()
                global.alert(err.msg)
            }
        } catch (e: Throwable) {
            Logger.e("auth", "Failed to generate captcha captcha", e)
            global.alert(e.message)
        } finally {
            isOperating = false
        }
    }

    private var captchaCont: Continuation<Unit>? = null

    private suspend fun waitForCaptcha() {
        suspendCancellableCoroutine<Unit> { cont ->
            showCaptchaDialog = true
            captchaCont = cont
        }
    }

    fun finishCaptcha() {
        showCaptchaDialog = false
        captchaCont?.resume(Unit)
    }

    fun startLogin() {
        viewModelScope.launch {
            generateCaptcha()
            waitForCaptcha()
            doLogin()
        }
    }

    private fun register() {
        viewModelScope.launch {
            generateCaptcha()
            waitForCaptcha()
            doRegister()
        }
    }

    fun forgetPassword() {
        global.nav.push(Route.ForgetPassword)
    }
}