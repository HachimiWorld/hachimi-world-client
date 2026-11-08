package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferenceKey

/**
 * The card on the home page that points to the square, so people learn it's there.
 *
 * It goes away for good once the user has opened the square, from the card or any other way,
 * or closed the card.
 *
 * @since 261106
 */
@Singleton
class SquareIntro(private val dataStore: MyDataStore) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** False until it's known the user hasn't seen the square, so the card doesn't flash. */
    var visible by mutableStateOf(false)
        private set
    private var seen = false

    init {
        scope.launch {
            val done = dataStore.get(KEY_SEEN) == true
            if (!seen) visible = !done
        }
    }

    /** The user opened the square, or closed the card. */
    fun markSeen() {
        visible = false
        if (seen) return
        seen = true
        scope.launch { dataStore.set(KEY_SEEN, true) }
    }

    private companion object {
        val KEY_SEEN = PreferenceKey("square_intro_seen", Boolean::class)
    }
}
