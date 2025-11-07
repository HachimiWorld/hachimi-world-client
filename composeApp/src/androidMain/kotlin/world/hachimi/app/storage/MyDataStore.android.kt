package world.hachimi.app.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

actual class PreferenceKey<T: Any> {
    actual val name: String
    val key: Preferences.Key<T>

    constructor(name: String, key: Preferences.Key<T>) {
        this.name = name
        this.key = key
    }
}

actual object PreferencesKeys {
    actual val USER_UID: PreferenceKey<Long> = PreferenceKey("user_uid", longPreferencesKey("user_uid"))
    actual val USER_NAME: PreferenceKey<String> = PreferenceKey("user_name", stringPreferencesKey("user_name"))
    actual val USER_AVATAR: PreferenceKey<String> = PreferenceKey("user_avatar", stringPreferencesKey("user_avatar"))
    actual val AUTH_ACCESS_TOKEN: PreferenceKey<String> = PreferenceKey("auth_access_token", stringPreferencesKey("auth_access_token"))
    actual val AUTH_REFRESH_TOKEN: PreferenceKey<String> = PreferenceKey("auth_refresh_token", stringPreferencesKey("auth_refresh_token"))
    actual val SETTINGS_DARK_MODE: PreferenceKey<Boolean> = PreferenceKey("settings_dark_mode", booleanPreferencesKey("settings_dark_mode"))
    actual val SETTINGS_LOUDNESS_NORMALIZATION: PreferenceKey<Boolean> = PreferenceKey("settings_loudness_normalization", booleanPreferencesKey("settings_loudness_normalization"))
    actual val SETTINGS_KIDS_MODE: PreferenceKey<Boolean> = PreferenceKey("settings_kids_mode", booleanPreferencesKey("settings_kids_mode"))
    actual val PLAYER_VOLUME: PreferenceKey<Float> = PreferenceKey("player_volume", floatPreferencesKey("player_volume"))
    actual val PLAYER_MUSIC_QUEUE: PreferenceKey<String> = PreferenceKey("player_music_queue", stringPreferencesKey("player_music_queue"))
}

class MyDataStoreImpl(
    private val dataStore: DataStore<Preferences>,
): MyDataStore {
    // I don't need transactions and flow for simple KV storage

    /*suspend inline fun <reified R> get(key: String): R? {
        val preferencesKey = when (R::class) {
            Int::class -> intPreferencesKey(key)
            Long::class -> longPreferencesKey(key)
            Boolean::class -> booleanPreferencesKey(key)
            String::class -> stringPreferencesKey(key)
            Float::class -> floatPreferencesKey(key)
            Double::class -> doublePreferencesKey(key)
            else -> error("Unsupported preference type ${R::class}")
        }
        return get(preferencesKey) as R?
    }*/

    override suspend fun <T : Any> get(key: PreferenceKey<T>): T? {
        return get(key.key)
    }

    override suspend fun <T : Any> set(key: PreferenceKey<T>, value: T) {
        return set(key.key, value)
    }

    override suspend fun <T : Any> delete(key: PreferenceKey<T>) {
        return delete(key.key)
    }

    suspend fun <T> get(key: Preferences.Key<T>): T? {
        return dataStore.data.map { it.get(key) }.first()
    }

    suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences -> preferences[key] = value }
    }

    suspend fun <T> delete(key: Preferences.Key<T>) {
        dataStore.edit { preferences -> preferences.remove(key) }
    }
}
