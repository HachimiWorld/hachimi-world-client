package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.*
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.*
import kotlinx.io.Buffer
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

class EditProfileViewModel(
    private val api: ApiClient,
    private val global: GlobalStore
) : ViewModel(CoroutineScope(Dispatchers.Default)) {

    var loadingProfile by mutableStateOf(true)
        private set
    var profile by mutableStateOf<UserModule.PublicUserProfile?>(null)
        private set

    // Inline edit fields (initialized from profile once loaded)
    var editUsernameValue by mutableStateOf("")
    var editBioValue by mutableStateOf("")
    var editGenderValue by mutableStateOf<Int?>(null)

    var operating by mutableStateOf(false)
        private set

    var avatarUploading by mutableStateOf(false)
    var avatarUploadProgress by mutableStateOf(0f)

    // Connections
    val connections = mutableStateListOf<UserModule.ConnectionItem>()
    var loadingConnections by mutableStateOf(false)
        private set

    // Bind dialog state
    var showBindDialog by mutableStateOf(false)
        private set
    var bindBilibiliUid by mutableStateOf("")
    var bindChallengeId by mutableStateOf<String?>(null)
        private set
    var bindChallenge by mutableStateOf<String?>(null)
        private set
    var bindGenerating by mutableStateOf(false)
        private set
    var bindVerifying by mutableStateOf(false)
        private set

    // Unlink confirm dialog state
    var showUnlinkConfirm by mutableStateOf(false)
        private set
    var unlinkTargetType by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            val userInfo = global.userInfo
            if (userInfo == null) {
                global.alert(Res.string.auth_not_logged_in)
                return@launch
            }
            awaitAll(
                async { refreshProfile() },
                async { refreshConnections() }
            )
        }
    }

    // ---- Avatar ----

    fun editAvatar() {
        viewModelScope.launch {
            val image = FileKit.openFilePicker(type = FileKitType.Image)
            if (image != null) {
                val size = image.size()
                if (size > 4 * 1024 * 1024) {
                    global.alert(Res.string.publish_image_too_large)
                    return@launch
                }
                val buffer = Buffer().apply { write(image.readBytes()) }
                try {
                    avatarUploading = true
                    val resp = api.userModule.setAvatar(
                        filename = image.name,
                        source = buffer,
                        listener = { sent, _ ->
                            avatarUploadProgress = (sent.toDouble() / size).toFloat().coerceIn(0f, 1f)
                        }
                    )
                    if (resp.ok) {
                        refreshProfile()
                    } else {
                        global.alert(resp.err().msg)
                    }
                } catch (e: Throwable) {
                    Logger.e("edit_profile", "Failed to upload avatar", e)
                    global.alert(e.message)
                } finally {
                    avatarUploading = false
                }
            }
        }
    }

    // ---- Save all profile fields at once ----

    fun saveProfile() {
        viewModelScope.launch {
            operating = true
            try {
                val resp = api.userModule.updateProfile(
                    UserModule.UpdateProfileReq(
                        username = editUsernameValue,
                        bio = editBioValue.takeIf { it.isNotBlank() },
                        gender = editGenderValue,
                    )
                )
                if (resp.ok) {
                    refreshProfile()
                    global.alert(Res.string.user_profile_saved)
                } else {
                    global.alert(resp.err().msg)
                }
            } catch (e: Throwable) {
                Logger.e("edit_profile", "Failed to save profile", e)
                global.alert(e.message)
            } finally {
                operating = false
            }
        }
    }

    // ---- Internal profile refresh ----

    private suspend fun refreshProfile() {
        val uid = global.userInfo?.uid ?: return
        loadingProfile = true
        try {
            val resp = api.userModule.profile(uid)
            if (resp.ok) {
                val data = resp.ok()
                profile = data
                editUsernameValue = data.username
                editBioValue = data.bio ?: ""
                editGenderValue = data.gender
                global.setLoginUser(data.uid, data.username, data.avatarUrl, true)
            } else {
                global.alert(resp.err().msg)
            }
        } catch (e: Throwable) {
            Logger.e("edit_profile", "Failed to fetch profile", e)
            global.alert(e.message)
        } finally {
            loadingProfile = false
        }
    }

    // ---- Connections ----

    private suspend fun refreshConnections() {
        loadingConnections = true
        try {
            val resp = api.userModule.connectionList()
            if (resp.ok) {
                val data = resp.ok()
                connections.clear()
                connections.addAll(data.items)
            } else {
                global.alert(resp.err().msg)
            }
        } catch (e: Throwable) {
            Logger.e("edit_profile", "Failed to load connections", e)
            global.alert(e.message)
        } finally {
            loadingConnections = false
        }
    }

    fun startBind() {
        bindBilibiliUid = ""
        bindChallengeId = null
        bindChallenge = null
        bindGenerating = false
        bindVerifying = false
        showBindDialog = true
    }

    fun dismissBind() {
        showBindDialog = false
    }

    fun generateChallenge() {
        viewModelScope.launch {
            bindGenerating = true
            try {
                val resp = api.userModule.connectionGenerateChallenge(
                    UserModule.ConnectionGenerateChallengeReq(
                        type = UserModule.CONNECTION_TYPE_BILIBILI,
                        providerAccountId = bindBilibiliUid.trim()
                    )
                )
                if (resp.ok) {
                    val data = resp.ok()
                    bindChallengeId = data.challengeId
                    bindChallenge = data.challenge
                } else {
                    global.alert(resp.err().msg)
                }
            } catch (e: Throwable) {
                Logger.e("edit_profile", "Failed to generate challenge", e)
                global.alert(e.message)
            } finally {
                bindGenerating = false
            }
        }
    }

    fun verifyChallenge() {
        val challengeId = bindChallengeId ?: return
        viewModelScope.launch {
            bindVerifying = true
            try {
                val resp = api.userModule.connectionVerifyChallenge(
                    UserModule.ConnectionVerifyChallengeReq(challengeId = challengeId)
                )
                if (resp.ok) {
                    showBindDialog = false
                    refreshConnections()
                    global.alert(Res.string.user_connections_linked_success)
                } else {
                    global.alert(resp.err().msg)
                }
            } catch (e: Throwable) {
                Logger.e("edit_profile", "Failed to verify challenge", e)
                global.alert(e.message)
            } finally {
                bindVerifying = false
            }
        }
    }

    fun requestUnlink(type: String) {
        unlinkTargetType = type
        showUnlinkConfirm = true
    }

    fun dismissUnlink() {
        showUnlinkConfirm = false
        unlinkTargetType = null
    }

    fun confirmUnlink() {
        val type = unlinkTargetType ?: return
        viewModelScope.launch {
            operating = true
            try {
                val resp = api.userModule.connectionUnlink(
                    UserModule.ConnectionUnlinkReq(type = type)
                )
                if (resp.ok) {
                    showUnlinkConfirm = false
                    unlinkTargetType = null
                    refreshConnections()
                } else {
                    global.alert(resp.err().msg)
                }
            } catch (e: Throwable) {
                Logger.e("edit_profile", "Failed to unlink account", e)
                global.alert(e.message)
            } finally {
                operating = false
            }
        }
    }
}
