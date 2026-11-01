package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import world.hachimi.app.getPlatform
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferenceKey
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

const val GITHUB_REPO_URL = "https://github.com/HachimiWorld/hachimi-world-client"

/**
 * Asks the user to star the project on GitHub. The app is not in any app store, so a star stands in for a rating.
 *
 * When it shows:
 * - The app was first opened at least [MIN_AGE] ago, and has been opened [MIN_LAUNCHES] times
 *   or has played [MIN_COMPLETED_SONGS] songs to the end.
 * - Only right after something good happens (liking a song, adding one to a playlist, favoriting a playlist,
 *   posting a comment), after a short pause. Never at launch, and never over another dialog.
 * - At most once per session, [SNOOZE] apart, [MAX_PROMPTS] times in total.
 * - Starring (from the dialog or from settings) or "Don't ask again" turns it off for good.
 *
 * It only listens to [AppEvent]s, so the code that likes songs or plays them doesn't know about it.
 */
class StarPrompt(
    private val dataStore: MyDataStore,
    events: Flow<AppEvent>,
    /** True while another dialog is open, so the prompt does not stack on top of it */
    private val isBusy: () -> Boolean,
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val storeMutex = Mutex()
    private val promptMutex = Mutex()
    private var shownThisSession = false

    var visible by mutableStateOf(false)
        private set

    init {
        // Undispatched, so it is already listening when the app sends AppLaunched
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            events.collect { event ->
                when (event) {
                    AppEvent.AppLaunched -> onAppLaunch()
                    is AppEvent.SongCompleted -> onSongCompleted()
                    is AppEvent.SongLiked,
                    is AppEvent.SongAddedToPlaylist,
                    is AppEvent.PlaylistFavorited,
                    AppEvent.CommentPosted -> onPositiveAction()
                }
            }
        }
    }

    private fun onAppLaunch() {
        scope.launch {
            storeMutex.withLock {
                if (dataStore.get(KEY_FIRST_LAUNCH_AT) == null) {
                    dataStore.set(KEY_FIRST_LAUNCH_AT, now())
                }
                dataStore.set(KEY_LAUNCH_COUNT, (dataStore.get(KEY_LAUNCH_COUNT) ?: 0L) + 1)
            }
        }
    }

    private fun onSongCompleted() {
        scope.launch {
            storeMutex.withLock {
                dataStore.set(KEY_COMPLETED_SONGS, (dataStore.get(KEY_COMPLETED_SONGS) ?: 0L) + 1)
            }
        }
    }

    private fun onPositiveAction() {
        if (shownThisSession || visible) return
        scope.launch {
            // Ignore taps that arrive while one is already waiting to show
            if (!promptMutex.tryLock()) return@launch
            try {
                if (!isEligible()) return@launch
                // Let the like or the closing dialog finish before asking
                delay(PROMPT_DELAY)
                if (shownThisSession || isBusy()) return@launch
                shownThisSession = true
                visible = true
                storeMutex.withLock {
                    dataStore.set(KEY_SHOWN_COUNT, (dataStore.get(KEY_SHOWN_COUNT) ?: 0L) + 1)
                    dataStore.set(KEY_LAST_SHOWN_AT, now())
                }
            } finally {
                promptMutex.unlock()
            }
        }
    }

    /** "Star on GitHub" in the dialog */
    fun star() {
        visible = false
        openRepo()
    }

    /** "Later", or closing the dialog. It may come back after [SNOOZE]. */
    fun later() {
        visible = false
    }

    /** "Don't ask again" */
    fun never() {
        visible = false
        scope.launch { dataStore.set(KEY_DONE, true) }
    }

    /** The fixed entry in settings. Someone who stars from there will not be asked again. */
    fun openRepo() {
        getPlatform().openUrl(GITHUB_REPO_URL)
        scope.launch { dataStore.set(KEY_DONE, true) }
    }

    private suspend fun isEligible(): Boolean = storeMutex.withLock {
        if (dataStore.get(KEY_DONE) == true) return false
        if ((dataStore.get(KEY_SHOWN_COUNT) ?: 0L) >= MAX_PROMPTS) return false

        val now = now()
        val lastShownAt = dataStore.get(KEY_LAST_SHOWN_AT)
        if (lastShownAt != null && now - lastShownAt < SNOOZE.inWholeMilliseconds) return false

        val firstLaunchAt = dataStore.get(KEY_FIRST_LAUNCH_AT) ?: return false
        if (now - firstLaunchAt < MIN_AGE.inWholeMilliseconds) return false

        val launches = dataStore.get(KEY_LAUNCH_COUNT) ?: 0L
        val completed = dataStore.get(KEY_COMPLETED_SONGS) ?: 0L
        launches >= MIN_LAUNCHES || completed >= MIN_COMPLETED_SONGS
    }

    private fun now() = Clock.System.now().toEpochMilliseconds()

    private companion object {
        val MIN_AGE = 3.days
        const val MIN_LAUNCHES = 5L
        const val MIN_COMPLETED_SONGS = 20L
        val SNOOZE = 30.days
        const val MAX_PROMPTS = 3L
        val PROMPT_DELAY = 1.5.seconds

        val KEY_FIRST_LAUNCH_AT = PreferenceKey("star_prompt_first_launch_at", Long::class)
        val KEY_LAUNCH_COUNT = PreferenceKey("star_prompt_launch_count", Long::class)
        val KEY_COMPLETED_SONGS = PreferenceKey("star_prompt_completed_songs", Long::class)
        val KEY_SHOWN_COUNT = PreferenceKey("star_prompt_shown_count", Long::class)
        val KEY_LAST_SHOWN_AT = PreferenceKey("star_prompt_last_shown_at", Long::class)
        val KEY_DONE = PreferenceKey("star_prompt_done", Boolean::class)
    }
}
