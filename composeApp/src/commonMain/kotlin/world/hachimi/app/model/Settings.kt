package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferenceKey

/**
 * A place to manage all the settings.
 *
 * TODO: Actually it's not decoupled at all.
 */
class Settings(
    private val dataStore: MyDataStore,
    // This is used to apply the loudness normalization setting immediately.
    // Maybe we can use observer pattern or something to decouple them in the future
    private val player: PlayerService
) {
    private object PreferencesKeys {
        val SETTINGS_DARK_MODE: PreferenceKey<Boolean> = PreferenceKey("settings_dark_mode", Boolean::class)
        val SETTINGS_LOUDNESS_NORMALIZATION: PreferenceKey<Boolean> = PreferenceKey("settings_loudness_normalization", Boolean::class)
        val SETTINGS_KIDS_MODE: PreferenceKey<Boolean> = PreferenceKey("settings_kids_mode", Boolean::class)
        val SETTINGS_LOCALE: PreferenceKey<String> = PreferenceKey("settings_locale", String::class)
        val SETTINGS_CLOSE_BEHAVIOR: PreferenceKey<String> = PreferenceKey("settings_close_behavior", String::class)
    }

    // New locale setting: null = follow system, otherwise locale string like "en" or "zh" or "zh_CN"
    var locale by mutableStateOf<String?>(null)
        private set
    var darkMode by mutableStateOf<Boolean?>(null)
        private set
    var enableLoudnessNormalization by mutableStateOf(true)
        private set
    var kidsMode by mutableStateOf(false)
        private set
    enum class CloseBehavior {
        ASK, MINIMIZE_TO_TRAY, EXIT
    }

    var closeBehavior by mutableStateOf(CloseBehavior.ASK)
        private set
    private val scope = CoroutineScope(Dispatchers.Default)

    suspend fun loadSettings() {
        this.darkMode = dataStore.get(PreferencesKeys.SETTINGS_DARK_MODE)
        this.enableLoudnessNormalization =
            dataStore.get(PreferencesKeys.SETTINGS_LOUDNESS_NORMALIZATION) ?: true
        this.locale = dataStore.get(PreferencesKeys.SETTINGS_LOCALE)
        this.kidsMode = dataStore.get(PreferencesKeys.SETTINGS_KIDS_MODE) ?: false

        val rawCloseBehavior = dataStore.get(PreferencesKeys.SETTINGS_CLOSE_BEHAVIOR)
        this.closeBehavior = if (rawCloseBehavior != null) {
            runCatching { CloseBehavior.valueOf(rawCloseBehavior) }.getOrDefault(CloseBehavior.ASK)
        } else {
            CloseBehavior.ASK
        }
    }

    fun updateCloseBehavior(value: CloseBehavior) = scope.launch {
        this@Settings.closeBehavior = value
        dataStore.set(PreferencesKeys.SETTINGS_CLOSE_BEHAVIOR, value.name)
    }

    fun updateDarkMode(darkMode: Boolean?) = scope.launch {
        this@Settings.darkMode = darkMode
        if (darkMode == null) {
            dataStore.delete(PreferencesKeys.SETTINGS_DARK_MODE)
        } else {
            dataStore.set(PreferencesKeys.SETTINGS_DARK_MODE, darkMode)
        }
    }

    fun updateLoudnessNormalization(enabled: Boolean) = scope.launch {
        this@Settings.enableLoudnessNormalization = enabled
        player.setReplayGainEnabled(enabled)
        dataStore.set(PreferencesKeys.SETTINGS_LOUDNESS_NORMALIZATION, enabled)
    }

    // Persist and update locale. null => follow system (delete stored key)
    fun updateLocale(locale: String?) = scope.launch {
        this@Settings.locale = locale
        if (locale == null) {
            dataStore.delete(PreferencesKeys.SETTINGS_LOCALE)
        } else {
            dataStore.set(PreferencesKeys.SETTINGS_LOCALE, locale)
        }
    }

    fun updateKidsMode(it: Boolean) {
        kidsMode = it
        scope.launch {
            dataStore.set(PreferencesKeys.SETTINGS_KIDS_MODE, it)
        }
    }
}