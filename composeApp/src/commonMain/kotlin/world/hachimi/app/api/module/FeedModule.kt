package world.hachimi.app.api.module

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import kotlin.time.Instant

/**
 * Streams that mix kinds of content, such as the square.
 *
 * @since 261106
 */
class FeedModule(
    private val client: ApiClient
) {
    /** @since 261106 */
    @Serializable
    data class PageReq(
        /** [PageResp.nextCursor] of the previous page; null for the first page. */
        val cursor: String? = null,
        /** 1..=50, defaults to 20. */
        val limit: Int? = null,
    )

    /** @since 261106 */
    @Serializable
    data class PageResp(
        /** Skip [FeedItem.Unknown] ones. */
        val items: List<FeedItem>,
        /** Null when there's no more. */
        val nextCursor: String? = null,
    )

    /** The square: everyone's moments, newest first. Pass [auth] when logged in. */
    suspend fun square(req: PageReq, auth: Boolean = false): WebResult<PageResp> =
        client.get("/feed/square", req, auth = auth)
}

/**
 * One entry of a feed, sent as `{ time, kind, data }`. Kinds this version doesn't know are
 * [Unknown], to skip.
 *
 * @since 261106
 */
@Serializable(with = FeedItem.Serializer::class)
sealed interface FeedItem {
    val time: Instant?

    data class Moment(override val time: Instant?, val moment: MomentModule.MomentItem) : FeedItem

    data class Unknown(override val time: Instant?, val kind: String) : FeedItem

    object Serializer : KSerializer<FeedItem> {
        override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

        override fun deserialize(decoder: Decoder): FeedItem {
            val input = decoder as? JsonDecoder ?: throw SerializationException("Only JSON is supported")
            val element = input.decodeJsonElement().jsonObject
            val time = element["time"]?.jsonPrimitive?.contentOrNull?.let { runCatching { Instant.parse(it) }.getOrNull() }
            val kind = element["kind"]?.jsonPrimitive?.contentOrNull.orEmpty()
            val data = element["data"]
            if (data == null || data is JsonNull) return Unknown(time, kind)
            return when (kind) {
                "moment" -> Moment(time, input.json.decodeFromJsonElement(MomentModule.MomentItem.serializer(), data))
                else -> Unknown(time, kind)
            }
        }

        override fun serialize(encoder: Encoder, value: FeedItem) {
            val output = encoder as? JsonEncoder ?: throw SerializationException("Only JSON is supported")
            val (kind, data) = when (value) {
                is Moment -> "moment" to output.json.encodeToJsonElement(MomentModule.MomentItem.serializer(), value.moment)
                is Unknown -> value.kind to JsonNull
            }
            output.encodeJsonElement(JsonObject(buildMap {
                value.time?.let { put("time", JsonPrimitive(it.toString())) }
                put("kind", JsonPrimitive(kind))
                put("data", data)
            }))
        }
    }
}
