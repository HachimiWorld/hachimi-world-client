package world.hachimi.app.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

actual class PreferenceKey<T: Any> {
    actual val name: String
    val key: Preferences.Key<T>

    constructor(name: String, key: Preferences.Key<T>) {
        this.name = name
        this.key = key
    }

    @Suppress("UNCHECKED_CAST")
    actual constructor(name: String, clazz: KClass<T>) : this(
        name = name,
        key = when (clazz) {
            Byte::class -> intPreferencesKey(name)
            Short::class -> intPreferencesKey(name)
            Int::class -> intPreferencesKey(name)
            Long::class -> longPreferencesKey(name)
            Boolean::class -> booleanPreferencesKey(name)
            String::class -> stringPreferencesKey(name)
            Float::class -> floatPreferencesKey(name)
            Double::class -> doublePreferencesKey(name)
            else -> error("Unsupported preference type $clazz")
        } as Preferences.Key<T>
    )
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
