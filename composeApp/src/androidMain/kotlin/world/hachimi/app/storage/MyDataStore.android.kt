package world.hachimi.app.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

actual class PreferenceKey<T> {
    actual val name: String
    val key: androidx.datastore.preferences.core.Preferences.Key<T>

    constructor(name: String, key: androidx.datastore.preferences.core.Preferences.Key<T>) {
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
