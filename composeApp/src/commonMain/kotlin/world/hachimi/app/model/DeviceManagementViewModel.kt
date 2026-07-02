package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.AuthModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

/**
 * Manages the list of logged-in devices for the current user.
 * @since 260630
 */
@KoinViewModel
class DeviceManagementViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val devices = mutableStateListOf<AuthModule.DeviceItem>()

    /** The device matching the current refresh token's jti, or null if not found. */
    var currentDevice by mutableStateOf<AuthModule.DeviceItem?>(null)
        private set

    /** All devices except the current one. */
    val otherDevices: List<AuthModule.DeviceItem>
        get() = currentDevice?.let { cur -> devices.filter { it.id != cur.id } } ?: devices.toList()


    /** Set of device IDs currently being logged out. */
    val logoutLoading = mutableStateListOf<Long>()

    /** The device pending logout confirmation in the dialog. */
    var logoutTarget by mutableStateOf<AuthModule.DeviceItem?>(null)
        private set

    fun mounted() = viewModelScope.launch {
        if (initializeStatus == InitializeStatus.INIT) {
            init()
        } else {
            refresh()
        }
    }

    fun retry() = viewModelScope.launch {
        if (initializeStatus == InitializeStatus.FAILED) {
            init()
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            load()
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to refresh devices", e)
        }
    }

    private suspend fun init() {
        if (initializeStatus == InitializeStatus.INIT) {
            try {
                load()
                initializeStatus = InitializeStatus.LOADED
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load devices", e)
                error = e.message
                initializeStatus = InitializeStatus.FAILED
            }
        }
    }

    private suspend fun load() {
        loading = true
        try {
            error = null
            val resp = api.authModule.deviceList()
            if (resp.ok) {
                val list = resp.ok().devices
                devices.clear()
                devices.addAll(list)
                currentDevice = list.find { it.tokenId == global.currentJti }
            } else {
                val err = resp.err()
                error = err.msg
                global.alert(err.msg)
            }
        } finally {
            loading = false
        }
    }

    fun logout(deviceId: Long) {
        if (deviceId in logoutLoading) return
        logoutLoading.add(deviceId)
        viewModelScope.launch {
            try {
                val resp = api.authModule.deviceLogout(AuthModule.DeviceLogoutReq(deviceId))
                if (resp.ok) {
                    if (currentDevice?.id == deviceId) currentDevice = null
                    devices.removeAll { it.id == deviceId }
                } else {
                    global.alert(resp.err().msg)
                }
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to logout device $deviceId", e)
                global.alert(e.message)
            } finally {
                logoutLoading.remove(deviceId)
            }
        }
    }

    fun showLogoutConfirm(device: AuthModule.DeviceItem) {
        logoutTarget = device
    }

    fun dismissLogoutConfirm() {
        logoutTarget = null
    }

    fun confirmLogout() {
        val device = logoutTarget ?: return
        dismissLogoutConfirm()
        logout(device.id)
    }

    companion object {
        private const val TAG = "device"
    }
}
