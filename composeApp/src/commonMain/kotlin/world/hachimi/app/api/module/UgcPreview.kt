package world.hachimi.app.api.module

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * How a piece of user content looks in a list, in its own kind's shape, tagged by `type`.
 * Kinds this version doesn't know decode as [Unknown].
 *
 * @since 261021
 */
@Serializable(with = UgcPreview.Serializer::class)
sealed interface UgcPreview {
    /** The main line: a song's or post's title, a playlist's name, a username, or a comment's text. */
    val headline: String

    /** A cover or avatar; a comment shows the picture of what it's on. */
    val imageUrl: String?

    /** The display id of the song it is, or is on; null otherwise. */
    val songDisplayId: String? get() = null

    @Serializable
    data class Song(val title: String, val coverUrl: String, val displayId: String) : UgcPreview {
        override val headline get() = title
        override val imageUrl get() = coverUrl
        override val songDisplayId get() = displayId
    }

    @Serializable
    data class Playlist(val name: String, val coverUrl: String?) : UgcPreview {
        override val headline get() = name
        override val imageUrl get() = coverUrl
    }

    @Serializable
    data class User(val username: String, val avatarUrl: String?) : UgcPreview {
        override val headline get() = username
        override val imageUrl get() = avatarUrl
    }

    /** @since 261031 */
    @Serializable
    data class Post(val id: Long, val title: String, val coverUrl: String?) : UgcPreview {
        override val headline get() = title
        override val imageUrl get() = coverUrl
    }

    @Serializable
    data class Comment(
        val content: String,
        /** What it's on; null if that's gone. */
        val on: UgcPreview?,
    ) : UgcPreview {
        override val headline get() = content
        override val imageUrl get() = on?.imageUrl
        override val songDisplayId get() = on?.songDisplayId
    }

    @Serializable
    data object Unknown : UgcPreview {
        override val headline get() = ""
        override val imageUrl get() = null
    }

    object Serializer : JsonContentPolymorphicSerializer<UgcPreview>(UgcPreview::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<UgcPreview> =
            when (element.jsonObject["type"]?.jsonPrimitive?.contentOrNull) {
                "song" -> Song.serializer()
                "playlist" -> Playlist.serializer()
                "user" -> User.serializer()
                "post" -> Post.serializer()
                "comment" -> Comment.serializer()
                else -> Unknown.serializer()
            }
    }
}
