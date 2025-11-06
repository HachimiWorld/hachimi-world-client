@file:Suppress("UNCHECKED_CAST")

package world.hachimi.app.storage

import kotlinx.browser.localStorage
import kotlin.reflect.KClass

class MyDataStoreImpl(): MyDataStore {
    override suspend fun <T : Any> get(key: PreferenceKey<T>): T? {
        // Use local storage
        localStorage.getItem(key.name)?.let {
            return when (key.clazz) {
                Long::class -> it.toLong() as T
                Int::class -> it.toInt() as T
                Short::class -> it.toShort() as T
                Char::class -> it.first() as T
                Byte::class -> it.toByte() as T
                Float::class -> it.toFloat() as T
                Double::class -> it.toDouble() as T
                String::class -> it as T
                Boolean::class -> it.toBooleanStrict() as T
                else -> error("Unsupported preference type ${key.clazz}")
            }
        }
        return null
    }

    override suspend fun <T : Any> set(key: PreferenceKey<T>, value: T) {
        localStorage.setItem(key.name, value.toString())
    }

    override suspend fun <T : Any> delete(key: PreferenceKey<T>) {
        localStorage.removeItem(key.name)
    }
}

actual class PreferenceKey<T : Any> {
    actual val name: String
    val clazz: KClass<T>

    constructor(name: String, clazz: KClass<T>) {
        this.name = name
        this.clazz = clazz
    }
}

actual object PreferencesKeys {
    actual val USER_UID: PreferenceKey<Long> = PreferenceKey("user_uid", Long::class)
    actual val USER_NAME: PreferenceKey<String> = PreferenceKey("user_name", String::class)
    actual val USER_AVATAR: PreferenceKey<String> = PreferenceKey("user_avatar", String::class)
    actual val AUTH_ACCESS_TOKEN: PreferenceKey<String> = PreferenceKey("auth_access_token", String::class)
    actual val AUTH_REFRESH_TOKEN: PreferenceKey<String> = PreferenceKey("auth_refresh_token", String::class)
    actual val SETTINGS_DARK_MODE: PreferenceKey<Boolean> = PreferenceKey("settings_dark_mode", Boolean::class)
    actual val SETTINGS_LOUDNESS_NORMALIZATION: PreferenceKey<Boolean> = PreferenceKey("settings_loudness_normalization", Boolean::class)
    actual val PLAYER_VOLUME: PreferenceKey<Float> = PreferenceKey("player_volume", Float::class)
    actual val PLAYER_MUSIC_QUEUE: PreferenceKey<String> = PreferenceKey("player_music_queue", String::class)
}