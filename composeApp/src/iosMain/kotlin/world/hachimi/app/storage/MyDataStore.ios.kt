
package world.hachimi.app.storage
import platform.Foundation.NSUserDefaults
import kotlin.reflect.KClass

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
    actual val PLAYER_VOLUME: PreferenceKey<Float> = PreferenceKey("player_volume", Float::class)
    actual val PLAYER_MUSIC_QUEUE: PreferenceKey<String> = PreferenceKey("player_music_queue", String::class)
}


@Suppress("UNCHECKED_CAST")
class MyDataStoreImpl : MyDataStore {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override suspend fun <T : Any> get(key: PreferenceKey<T>): T? {
        val value = userDefaults.objectForKey(key.name) ?: return null
        return when (key.clazz) {
            Long::class, Int::class, Short::class, Byte::class -> value as? T
            Float::class, Double::class -> value as? T
            Boolean::class -> value as? T
            String::class -> value as? T
            else -> error("Unsupported preference type ${key.clazz}")
        }
    }

    override suspend fun <T : Any> set(key: PreferenceKey<T>, value: T) {
        when (value) {
            is Long, is Int, is Short, is Byte -> userDefaults.setInteger(value.toLong(), key.name)
            is Float -> userDefaults.setFloat(value, key.name)
            is Double -> userDefaults.setDouble(value, key.name)
            is String -> userDefaults.setObject(value, key.name)
            is Boolean -> userDefaults.setBool(value, key.name)
            else -> error("Unsupported preference type ${value::class}")
        }
    }

    override suspend fun <T : Any> delete(key: PreferenceKey<T>) {
        userDefaults.removeObjectForKey(key.name)
    }
}
