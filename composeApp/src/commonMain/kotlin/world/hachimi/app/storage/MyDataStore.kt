package world.hachimi.app.storage


interface MyDataStore {
    suspend fun <T : Any> get(key: PreferenceKey<T>): T?
    suspend fun <T: Any> set(key: PreferenceKey<T>, value: T)
    suspend fun <T: Any> delete(key: PreferenceKey<T>)
}

expect class PreferenceKey<T: Any> {
    val name: String
}

expect object PreferencesKeys {
    val USER_UID: PreferenceKey<Long>
    val USER_NAME: PreferenceKey<String>
    val USER_AVATAR: PreferenceKey<String>
    val AUTH_ACCESS_TOKEN: PreferenceKey<String>
    val AUTH_REFRESH_TOKEN: PreferenceKey<String>
    val SETTINGS_DARK_MODE: PreferenceKey<Boolean>
    val SETTINGS_LOUDNESS_NORMALIZATION: PreferenceKey<Boolean>
    val PLAYER_VOLUME: PreferenceKey<Float>
    val PLAYER_MUSIC_QUEUE: PreferenceKey<String>
}
