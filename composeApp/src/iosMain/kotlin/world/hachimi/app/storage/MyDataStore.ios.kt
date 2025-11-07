
package world.hachimi.app.storage
import platform.Foundation.NSUserDefaults
import kotlin.reflect.KClass

actual class PreferenceKey<T : Any> {
    actual val name: String
    val clazz: KClass<T>

    actual constructor(name: String, clazz: KClass<T>) {
        this.name = name
        this.clazz = clazz
    }
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
