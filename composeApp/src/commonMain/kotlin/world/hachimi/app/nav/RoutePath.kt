package world.hachimi.app.nav

import io.ktor.http.encodeURLPath
import io.ktor.http.encodeURLQueryComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
annotation class RoutePath(val path: String)

@ExperimentalSerializationApi
class RoutePathEncoder(
    val polymorphic: Boolean,
    val classDiscriminator: String = "type",
    private val _path: String
) : AbstractEncoder() {
    override val serializersModule: SerializersModule = EmptySerializersModule()

    private var elementIndex = -1
    private var elementName = ""

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

            /*val annotations = descriptor.getElementAnnotations(index)
            val routePathAnnotation = annotations.find { it is RoutePath }

            if (routePathAnnotation != null) {
                val path = (routePathAnnotation as RoutePath).path
                this.path = path
            }*/

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
}

@OptIn(InternalSerializationApi::class)
@ExperimentalSerializationApi
fun <T : Route> encodeToRoutePath(
    serializer: SerializationStrategy<T>,
    value: T,
    path: String
): Pair<String, Map<String, String?>> {
    val isPolymorphic = serializer is AbstractPolymorphicSerializer
    val encoder = RoutePathEncoder(isPolymorphic, "type", path)
    encoder.encodeSerializableValue(serializer, value)
    return encoder.getPath() to encoder.getParameters()
}

@OptIn(InternalSerializationApi::class)
@ExperimentalSerializationApi
inline fun <reified T : Route> encodeToRoutePath(value: T): Pair<String, Map<String, String?>> {
    // Get annotation of T
    val serializer = serializer<T>()
    val annotations = T::class.serializer().descriptor.annotations // T::class.serializer() is based on reflection
    val routePathAnnotation = annotations.filterIsInstance<RoutePath>().firstOrNull()
    return encodeToRoutePath(serializer, value, path = routePathAnnotation?.path ?: "")
}

fun buildBrowserPath(path: String, parameters: Map<String, String?>): String {
    val notNullParams = parameters.entries.filter {
        it.value != null
    }
    if (notNullParams.isEmpty()) return path.encodeURLPath()
    val query = notNullParams.joinToString("&") {
        "${it.key.encodeURLQueryComponent()}=${it.value!!.encodeURLQueryComponent()}"
    }
    return "${path.encodeURLPath()}?$query"
}