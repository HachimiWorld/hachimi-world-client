package world.hachimi.app.model

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.playlist_cover_rate_limited
import hachimiworld.composeapp.generated.resources.playlist_cover_rejected
import hachimiworld.composeapp.generated.resources.playlist_description_rejected
import hachimiworld.composeapp.generated.resources.playlist_name_rejected
import org.jetbrains.compose.resources.StringResource

/**
 * The localized message for an error code from `/playlist/create` and `/playlist/update`, or null
 * to show the server's message. Content checks don't say why something was rejected, so neither
 * does this.
 * @since 261018
 */
internal fun playlistUpdateErrorMessage(code: String): StringResource? = when (code) {
    "name_rejected" -> Res.string.playlist_name_rejected
    "description_rejected" -> Res.string.playlist_description_rejected
    else -> null
}

/**
 * The localized message for an error code from `/playlist/set_cover`, or null to show the server's
 * message.
 * @since 261018
 */
internal fun playlistCoverErrorMessage(code: String): StringResource? = when (code) {
    "cover_rejected" -> Res.string.playlist_cover_rejected
    "rate_limited" -> Res.string.playlist_cover_rate_limited
    else -> null
}
