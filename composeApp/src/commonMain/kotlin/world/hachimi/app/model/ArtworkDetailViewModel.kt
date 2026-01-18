package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.artwork_jmid_already_used
import hachimiworld.composeapp.generated.resources.artwork_jmid_prefix_not_set
import hachimiworld.composeapp.generated.resources.publish_change_success
import hachimiworld.composeapp.generated.resources.publish_jmid_number_format
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import world.hachimi.app.util.singleLined

private const val TAG = "artwork_detail"

class ArtworkDetailViewModel(
    private val global: GlobalStore,
    private val api: ApiClient
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var songId by mutableStateOf(-1L)
        private set
    var detail by mutableStateOf<SongModule.PublicSongDetail?>(null)
        private set
    var loading by mutableStateOf(false)
        private set

    var changeOperating by mutableStateOf(false)
        private set
    var showChangeJmidDialog by mutableStateOf(false)
        private set
    var changeJmidAvailable by mutableStateOf(false)
        private set

    var jmidPrefix by mutableStateOf<String?>(null)
        private set
    var jmidNumber by mutableStateOf<String?>(null)
        private set
    var jmidValid by mutableStateOf<Boolean?>(null)
        private set
    var jmidSupportText by mutableStateOf<String?>(null)
        private set

    fun mounted(songId: Long) {
        if (this.songId != songId) {
            this.songId = songId
            initializeStatus = InitializeStatus.INIT
            load()
        }
    }

    fun dispose() {

    }

    fun load() {
        loading = true
        viewModelScope.launch {
            try {
                val resp = api.songModule.detailById(songId)
                if (resp.ok) {
                    detail = resp.ok()
                    if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.LOADED
                } else {
                    val err = resp.err()
                    global.alert(err.msg)
                    if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.FAILED
                }
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to fetch detail", e)
                global.alert(e.message)
                if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.FAILED
            } finally {
                loading = false
            }
        }
    }

    fun retry() {
        if (initializeStatus == InitializeStatus.FAILED) {
            initializeStatus = InitializeStatus.INIT
            load()
        }
    }

    fun startChangeJmid() {
        changeOperating = true
        viewModelScope.launch {
            try {
                val resp = api.publishModule.jmidMine()
                if (resp.ok) {
                    val prefix = resp.ok().jmidPrefix
                    if (prefix != null) {
                        jmidPrefix = prefix
                        changeJmidAvailable = true
                        showChangeJmidDialog = true
                    } else {
                        jmidPrefix = null
                        changeJmidAvailable = false
                        showChangeJmidDialog = true
                    }
                } else {
                    val err = resp.err()
                    global.alert(err.msg)
                    changeJmidAvailable = false
                }
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to fetch detail", e)
                global.alert(e.message)
            } finally {
                changeOperating = false
            }
        }
    }

    private var checkJmidJob: Job? = null
    private val checkJmidMutex = Mutex()

    fun updateJmidNumber(number: String) {
        jmidValid = null
        jmidSupportText = null
        val mappedInput = number.singleLined()
        jmidNumber = mappedInput
        val prefix = jmidPrefix ?: run {
            // unreachable — set localized message asynchronously
            jmidValid = false
            viewModelScope.launch {
                jmidSupportText = org.jetbrains.compose.resources.getString(Res.string.artwork_jmid_prefix_not_set)
            }
            return
        }

        val jmidFull = "JM-$prefix-$mappedInput"

        if (Regex("\\d{3}").matches(mappedInput)) {
            viewModelScope.launch {
                checkJmidMutex.withLock {
                    checkJmidJob?.cancelAndJoin()
                    checkJmidJob = launch {
                        delay(500)
                        try {
                            val resp = api.publishModule.jmidCheck(PublishModule.JmidCheckReq(jmidFull))
                            if (jmidNumber == mappedInput) {
                                if (resp.ok) {
                                    if (resp.ok().result) {
                                        jmidValid = true
                                        jmidSupportText = null
                                    } else {
                                        jmidValid = false
                                        viewModelScope.launch {
                                            jmidSupportText = org.jetbrains.compose.resources.getString(Res.string.artwork_jmid_already_used)
                                        }
                                    }
                                } else {
                                    jmidValid = false
                                    jmidSupportText = resp.err().msg
                                }
                            }
                        } catch (e: Throwable) {
                            Logger.e("publish", "Failed to check jmid", e)
                            jmidValid = false
                            jmidSupportText = e.message
                        }
                    }
                }
            }
        } else {
            jmidValid = false
            viewModelScope.launch {
                jmidSupportText = org.jetbrains.compose.resources.getString(Res.string.publish_jmid_number_format)
            }
        }
    }

    fun confirmChangeJmid() {
        changeOperating = true

        viewModelScope.launch {
            try {
                val jmid = "JM-${jmidPrefix!!}-${jmidNumber!!}"

                val resp = api.publishModule.changeJmid(PublishModule.ChangeJmidReq(
                    songId = songId,
                    oldJmid = detail!!.displayId,
                    newJmid = jmid
                ))
                if (resp.ok) {
                    showChangeJmidDialog = false
                    global.alert(Res.string.publish_change_success)
                    load()
                } else {
                    val err = resp.err()
                    global.alert(err.msg)
                }
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to change jmid", e)
                global.alert(e.message)
            } finally {
                changeOperating = false
            }
        }
    }

    fun cancelChangeJmid() {
        showChangeJmidDialog = false
    }
}