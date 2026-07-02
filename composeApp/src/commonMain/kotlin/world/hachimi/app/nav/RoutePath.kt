package world.hachimi.app.nav

import io.ktor.http.encodeURLPath
import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.parseUrlEncodedParameters
import io.ktor.util.toMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@ExperimentalSerializationApi
@SerialInfo
annotation class RoutePath(val path: String)

private const val ClassDiscriminator = "@type"

private val RouteJson = Json {
    classDiscriminator = ClassDiscriminator
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun encodeToBrowserPath(route: Route): BrowserPath {
    val jsonElement = RouteJson.encodeToJsonElement(route)
    val json = jsonElement.jsonObject
    val type = json[ClassDiscriminator]?.jsonPrimitive?.content // type is path
    val parameters = json.filterKeys { it != ClassDiscriminator }
        .mapValues {
            (it.value as JsonPrimitive?)?.content ?: Json.encodeToString(it.value)
        }
    return BrowserPath(type ?: "", parameters)
}

fun decodeFromBrowserPath(path: BrowserPath): Route {
    // It's so difficult to implement a decoder, why not just utilize the JSON decoder :p
    val json = buildJsonObject {
        put(ClassDiscriminator, path.path) // The polymorphic discriminator
        path.parameters.forEach {
            put(it.key, it.value)
        }
    }

    return RouteJson.decodeFromJsonElement(json)
}

fun decodeFromBrowserPathString(path: String): Route {
    val browserPath = BrowserPath.decodeFromString(path)
    return decodeFromBrowserPath(browserPath)
}

data class BrowserPath(
    val path: String,
    val parameters: Map<String, String?>,
) {
    fun encodeToString(): String {
        val notNullParams = parameters.entries.filter {
            it.value != null
        }
        if (notNullParams.isEmpty()) return path.encodeURLPath()
        val query = notNullParams.joinToString("&") {
            "${it.key.encodeURLQueryComponent(encodeFull = true)}=${
                it.value!!.encodeURLQueryComponent(
                    encodeFull = true
                )
            }"
        }
        return "${path.encodeURLPath()}?$query"
    }

    companion object {
        fun decodeFromString(encoded: String): BrowserPath {
            val parts = encoded.split('?', limit = 2)
            val path = parts[0]
            val parameters = parts.getOrNull(1)?.parseUrlEncodedParameters()
            val map = parameters?.toMap()
                ?.map { it.key to it.value.joinToString(",") as String? }
                ?.associate { it }
            return BrowserPath(path, map ?: emptyMap())
        }
    }
}


/*
@OptIn(InternalSerializationApi::class)
@ExperimentalSerializationApi
fun <T : Route> encodeToBrowserPath(
    serializer: SerializationStrategy<T>,
    value: T,
    path: String
): BrowserPath {
    val isPolymorphic = serializer is AbstractPolymorphicSerializer
    val encoder = RoutePathEncoder(isPolymorphic, "type", path)
    encoder.encodeSerializableValue(serializer, value)
    return BrowserPath(
        encoder.getPath(),
        encoder.getParameters()
    )
}

@OptIn(InternalSerializationApi::class)
@ExperimentalSerializationApi
inline fun <reified T : Route> encodeToBrowserPath(value: T): BrowserPath {
    // Get annotation of T
    val serializer = serializer<T>()
    val annotations = T::class.serializer().descriptor.annotations // T::class.serializer() is based on reflection
    val routePathAnnotation = annotations.filterIsInstance<RoutePath>().firstOrNull()
    return encodeToBrowserPath(serializer, value, path = routePathAnnotation?.path ?: "")
}
*/

/*
@ExperimentalSerializationApi
class RoutePathEncoder(
    val polymorphic: Boolean,
    val classDiscriminator: String = "type",
    private val _path: String
) : AbstractEncoder() {
    override val serializersModule: SerializersModule = EmptySerializersModule()

    private var elementIndex = -1
    private var elementName = ""

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        super.encodeSerializableElement(descriptor, index, serializer, value)
    }

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        elementIndex = index
        elementName = descriptor.getElementName(index)

        if (polymorphic && elementName == classDiscriminator && elementIndex == 0) {
            // Hope this must be the class discriminator

            // FIXME:
            //  We should get the route path from the `RoutePath` annotation, but unfortunately,
            //  for some reason, we can't get the annotation from this method.
            //  So I have to get the path from the serial name of the descriptor, which is not ideal.
            //  It might be the restriction of kotlinx.serialization, or maybe I just don't know how to get the annotation in this case.
            //  https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md

            */
/*val annotations = descriptor.getElementAnnotations(index)
            val routePathAnnotation = annotations.find { it is RoutePath }

            if (routePathAnnotation != null) {
                val path = (routePathAnnotation as RoutePath).path
                this.path = path
            }*//*


            // Skip serializing the class discriminator
            return false
        }

        return true
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        // Use serial name as path
        this.path = descriptor.serialName
        return super.beginStructure(descriptor)
    }

    override fun encodeValue(value: Any) {
        if (elementIndex != -1) {
            queryParameters[elementName] = value.toString()
        }
    }

    override fun encodeNull() {
        // For null, just ignore it and don't add it to the query part
    }

    private var path: String = ""

    fun getPath(): String {
        return path
    }

    private val queryParameters = mutableMapOf<String, String?>()
    fun getParameters(): Map<String, String?> {
        return queryParameters
    }
}*/
