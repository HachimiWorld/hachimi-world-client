package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.logging.Logger

class UserSpaceViewModel(
    private val api: ApiClient,
    private val global: GlobalStore
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var loading by mutableStateOf(false)
        private set
    var profile by mutableStateOf<UserModule.ProfileResp?>(null)
        private set
    var showEditBio by mutableStateOf(false)
        private set
    var editBioValue by mutableStateOf<String>("")

    var showEditUsername by mutableStateOf(false)
        private set
    var editUsernameValue by mutableStateOf<String>("")

    var operating by mutableStateOf(false)
        private set

    var avatarUploading by mutableStateOf(false)

    fun mounted() {
        refreshProfile()
    }

    fun dispose() {

    }

    fun editAvatar() {
        viewModelScope.launch {
            val image = FileKit.openFilePicker(
                type = FileKitType.Image
            )
            if (image != null) {
                // 1. Validate image
                val size = image.size()
                if (size > 4 * 1024 * 1024) {
                    global.alert("Image too large")
                    return@launch
                }
                val buffer = Buffer().apply { write(image.readBytes()) }

                // 2. Upload
                try {
                    avatarUploading = true

                    val resp = api.userModule.setAvatar(filename = image.name, source = buffer, listener = { sent, total ->
                        val progress = (sent.toDouble() / size).toFloat()
                    })
                    if (resp.ok) {
                        refreshProfile()
                    } else {
                        val error = resp.errData<CommonError>()
                        global.alert(error.msg)
                        return@launch
                    }
                } catch (e: Exception) {
                    Logger.e("creation", "Failed to upload image image", e)
                    global.alert(e.message)
                    return@launch
                } finally {
                    avatarUploading = false
                }
            }
        }
    }

    fun editUsername() {
        editUsernameValue = profile?.username ?: ""
        showEditUsername = true
    }

    fun confirmEditUsername() {
        viewModelScope.launch {
            operating = true
            try {
                val resp = api.userModule.updateProfile(
                    UserModule.UpdateProfileReq(
                        username = editUsernameValue,
                        bio = profile!!.bio,
                        gender = profile!!.gender,
                    )
                )
                if (resp.ok) {
                    showEditUsername = false
                    refreshProfile()
                } else {
                    val err = resp.errData<CommonError>()
                    global.alert(err.msg)
                }
            } catch (e: Exception) {
                Logger.e("userspace", "Failed to update profile", e)
                global.alert(e.message)
            } finally {
                operating = false
            }
        }
    }

    fun editBio() {
        editBioValue = profile?.bio ?: ""
        showEditBio = true
    }

    fun cancelEdit() {
        showEditBio = false
        showEditUsername = false
    }

    fun confirmEditBio() {
        viewModelScope.launch {
            operating = true
            try {
                val resp = api.userModule.updateProfile(
                    UserModule.UpdateProfileReq(
                        username = profile!!.username,
                        bio = editBioValue,
                        gender = profile!!.gender,
                    )
                )
                if (resp.ok) {
                    showEditBio = false
                    refreshProfile()
                } else {
                    val err = resp.errData<CommonError>()
                    global.alert(err.msg)
                }
            } catch (e: Exception) {
                Logger.e("userspace", "Failed to update profile", e)
                global.alert(e.message)
            } finally {
                operating = false
            }
        }
    }

    private fun refreshProfile() {
        val userInfo = global.userInfo
        if (userInfo == null) {
            global.alert("Not logged in")
            return
        }

        viewModelScope.launch {
            loading = true
            try {
                val resp = api.userModule.profile(userInfo.uid)
                if (resp.ok) {
                    val data = resp.okData<UserModule.ProfileResp>()
                    profile = data
                    global.setLoginUser(data.uid, data.username, data.avatarUrl)
                } else {
                    val err = resp.errData<CommonError>()
                    global.alert(err.msg)
                }
            } catch (e: Exception) {
                Logger.e("userspace", "Failed to fetch profile", e)
                global.alert(e.message)
            } finally {
                loading = false
            }
        }
    }
}