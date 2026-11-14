package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

/**
 * The Me tab: the signed-in user's own profile, for the counts under their name. The page works
 * without it, so a failure here stays quiet.
 *
 * @since 261110
 */
@KoinViewModel
class MeViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {

    /** Null until loaded, and again after the account changes. */
    var profile by mutableStateOf<UserModule.PublicUserProfile?>(null)
        private set

    private var job: Job? = null

    /** Loads the profile of whoever is signed in now. */
    fun refresh() {
        val uid = global.userInfo?.uid
        if (profile?.uid != uid) profile = null
        job?.cancel()
        if (uid == null) return
        job = viewModelScope.launch {
            try {
                val resp = api.userModule.profile(uid)
                if (resp.ok) {
                    val data = resp.ok()
                    profile = data
                    global.setLoginUser(data.uid, data.username, data.avatarUrl, true)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e("me", "Failed to load own profile", e)
            }
        }
    }
}
