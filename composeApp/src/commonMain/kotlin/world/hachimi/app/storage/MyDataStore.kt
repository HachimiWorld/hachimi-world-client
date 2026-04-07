package world.hachimi.app.storage

import kotlin.reflect.KClass


interface MyDataStore {
    suspend fun <T : Any> get(key: PreferenceKey<T>): T?
    suspend fun <T: Any> set(key: PreferenceKey<T>, value: T)
    suspend fun <T: Any> delete(key: PreferenceKey<T>)
}

expect class PreferenceKey<T: Any> {
    val name: String
    constructor(name: String, clazz: KClass<T>)
}

object PreferencesKeys {
    val USER_UID: PreferenceKey<Long> = PreferenceKey("user_uid", Long::class)
    val USER_NAME: PreferenceKey<String> = PreferenceKey("user_name", String::class)
    val USER_AVATAR: PreferenceKey<String> = PreferenceKey("user_avatar", String::class)
    val AUTH_ACCESS_TOKEN: PreferenceKey<String> = PreferenceKey("auth_access_token", String::class)
    val AUTH_REFRESH_TOKEN: PreferenceKey<String> = PreferenceKey("auth_refresh_token", String::class)
    val PLAYER_VOLUME: PreferenceKey<Float> = PreferenceKey("player_volume", Float::class)
    val PLAYER_MUSIC_QUEUE: PreferenceKey<String> = PreferenceKey("player_music_queue", String::class)
}