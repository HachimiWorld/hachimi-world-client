package world.hachimi.app.api.module

import io.ktor.client.content.ProgressListener
import kotlinx.io.Source
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import kotlin.time.Instant

/**
 * Moments, short posts any user can write: text, up to [MAX_IMAGES] images and an attachment.
 * Comments on them go through [CommentModule] with [CommentModule.TARGET_MOMENT]; the square lists
 * them through [FeedModule].
 *
 * @since 261106
 */
class MomentModule(
    private val client: ApiClient
) {
    /** @since 261106 */
    @Serializable
    data class MomentUser(
        val uid: Long,
        val username: String,
        val avatarUrl: String? = null,
    )

    /** @since 261106 */
    @Serializable
    data class MomentImage(
        val url: String,
        /** At most 480 pixels on the long side, for lists. */
        val thumbUrl: String,
        val width: Int,
        val height: Int,
    )

    /** A moment as shown to the current user. @since 261106 */
    @Serializable
    data class MomentItem(
        val id: Long,
        val author: MomentUser,
        /** May be empty. */
        val content: String,
        val images: List<MomentImage>,
        val attachments: List<MomentAttachment>,
        val likeCount: Int,
        /** Whether the current user liked it. */
        val liked: Boolean,
        /** Visible comments and replies. */
        val commentCount: Long,
        /** Hidden by a report decision; only its author sees it then. */
        val isHidden: Boolean,
        val createTime: Instant,
    )

    /** @since 261106 */
    @Serializable
    data class UploadImageResp(
        /** Pass it to [create] within an hour. */
        val imageId: String,
        val url: String,
        val thumbUrl: String,
        val width: Int,
        val height: Int,
    )

    /** What to attach, as `{ type, data }`. @since 261106 */
    @Serializable
    data class AttachmentInput(
        val type: String,
        val data: JsonObject,
    ) {
        companion object {
            fun song(songId: Long) = AttachmentInput(MomentAttachment.TYPE_SONG, buildJsonObject { put("id", songId) })
            fun playlist(playlistId: Long) = AttachmentInput(MomentAttachment.TYPE_PLAYLIST, buildJsonObject { put("id", playlistId) })
        }
    }

    /** @since 261106 */
    @Serializable
    data class CreateReq(
        /** Up to [CONTENT_MAX_CHARS] characters. May be empty when there are images or attachments. */
        val content: String,
        /** From [uploadImage], in order. Up to [MAX_IMAGES]. */
        val imageIds: List<String>,
        /** Up to [MAX_ATTACHMENTS]. */
        val attachments: List<AttachmentInput>,
    )

    /** @since 261106 */
    @Serializable
    data class CreateResp(
        /**
         * Null if it couldn't be checked now and waits to be checked again. No one sees it until it
         * passes; if it's blocked then, the author gets a notification.
         */
        val moment: MomentItem? = null,
    )

    /** @since 261106 */
    @Serializable
    data class MomentIdReq(
        val momentId: Long,
    )

    /** @since 261106 */
    @Serializable
    data class ListByUserReq(
        val userId: Long,
        /** [ListResp.nextCursor] of the previous page; null for the first page. */
        val cursor: String? = null,
        /** 1..=50, defaults to 20. */
        val limit: Int? = null,
    )

    /** @since 261106 */
    @Serializable
    data class ListResp(
        /** Newest first. */
        val items: List<MomentItem>,
        /** Null when there's no more. */
        val nextCursor: String? = null,
    )

    /**
     * Uploads one image: PNG, JPEG, WebP, AVIF or GIF (only its first frame is kept), up to 8 MB.
     * It's checked here, so one that's blocked errors with [ERR_IMAGE_REJECTED] and can be swapped.
     */
    suspend fun uploadImage(filename: String, source: Source, listener: ProgressListener? = null): WebResult<UploadImageResp> =
        client.upload("/moment/upload_image", filename, source, listener)

    /** One the check blocks isn't saved and errors with [ERR_MOMENT_REJECTED]. */
    suspend fun create(req: CreateReq): WebResult<CreateResp> =
        client.post("/moment/create", req)

    /** The user's own. */
    suspend fun delete(req: MomentIdReq): WebResult<Unit> =
        client.post("/moment/delete", req)

    /** Pass [auth] when logged in, to see whether the user liked it, and their own hidden ones. */
    suspend fun detail(req: MomentIdReq, auth: Boolean = false): WebResult<MomentItem> =
        client.get("/moment/detail", req, auth = auth)

    /** A user's moments, newest first. Pass [auth] when logged in. */
    suspend fun listByUser(req: ListByUserReq, auth: Boolean = false): WebResult<ListResp> =
        client.get("/moment/list_by_user", req, auth = auth)

    suspend fun like(req: MomentIdReq): WebResult<Unit> =
        client.post("/moment/like", req)

    suspend fun unlike(req: MomentIdReq): WebResult<Unit> =
        client.post("/moment/unlike", req)

    companion object {
        const val CONTENT_MAX_CHARS = 500
        const val MAX_IMAGES = 9
        const val MAX_ATTACHMENTS = 1
        /** The server takes up to 8 MB. */
        const val IMAGE_MAX_BYTES = 8L * 1024 * 1024

        const val ERR_IMAGE_REJECTED = "image_rejected"
        const val ERR_MOMENT_REJECTED = "moment_rejected"
        const val ERR_RATE_LIMITED = "rate_limited"
        const val ERR_IMAGE_NOT_FOUND = "image_not_found"
        const val ERR_INVALID_ATTACHMENT = "invalid_attachment"
        const val ERR_MOMENT_NOT_FOUND = "moment_not_found"
        const val ERR_USER_BANNED = "user_banned"
    }
}

/**
 * An attachment of a moment, in its type's shape. One whose song or playlist can't be seen any
 * more is [Unavailable]; types this version doesn't know are [Unknown].
 *
 * @since 261106
 */
@Serializable(with = MomentAttachment.Serializer::class)
sealed interface MomentAttachment {
    val type: String

    data class Song(val song: SongModule.PublicSongDetail) : MomentAttachment {
        override val type get() = TYPE_SONG
    }

    data class Playlist(val playlist: PlaylistModule.PlaylistMetadata) : MomentAttachment {
        override val type get() = TYPE_PLAYLIST
    }

    data class Unavailable(override val type: String) : MomentAttachment

    data class Unknown(override val type: String) : MomentAttachment

    companion object {
        const val TYPE_SONG = "song"
        const val TYPE_PLAYLIST = "playlist"
    }

    object Serializer : KSerializer<MomentAttachment> {
        override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

        override fun deserialize(decoder: Decoder): MomentAttachment {
            val input = decoder as? JsonDecoder ?: throw SerializationException("Only JSON is supported")
            val element = input.decodeJsonElement().jsonObject
            val type = element["type"]?.jsonPrimitive?.contentOrNull.orEmpty()
            val data = element["data"]
            if (data == null || data is JsonNull) return Unavailable(type)
            // A shape this version can't read is shown like a type it doesn't know
            return try {
                when (type) {
                    TYPE_SONG -> Song(input.json.decodeFromJsonElement(SongModule.PublicSongDetail.serializer(), data))
                    TYPE_PLAYLIST -> Playlist(input.json.decodeFromJsonElement(PlaylistModule.PlaylistMetadata.serializer(), data))
                    else -> Unknown(type)
                }
            } catch (e: SerializationException) {
                Unknown(type)
            } catch (e: IllegalArgumentException) {
                Unknown(type)
            }
        }

        override fun serialize(encoder: Encoder, value: MomentAttachment) {
            val output = encoder as? JsonEncoder ?: throw SerializationException("Only JSON is supported")
            val data: JsonElement = when (value) {
                is Song -> output.json.encodeToJsonElement(SongModule.PublicSongDetail.serializer(), value.song)
                is Playlist -> output.json.encodeToJsonElement(PlaylistModule.PlaylistMetadata.serializer(), value.playlist)
                is Unavailable, is Unknown -> JsonNull
            }
            output.encodeJsonElement(JsonObject(mapOf("type" to kotlinx.serialization.json.JsonPrimitive(value.type), "data" to data)))
        }
    }
}
