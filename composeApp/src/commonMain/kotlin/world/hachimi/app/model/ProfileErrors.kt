package world.hachimi.app.model

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.user_profile_bio_rejected
import hachimiworld.composeapp.generated.resources.user_profile_username_rejected
import org.jetbrains.compose.resources.StringResource

/**
 * The localized message for an error code from `/user/update_profile`, or null to show the
 * server's message. Content checks don't say why something was rejected, so neither does this.
 * @since 261017
 */
internal fun profileUpdateErrorMessage(code: String): StringResource? = when (code) {
    "username_rejected" -> Res.string.user_profile_username_rejected
    "bio_rejected" -> Res.string.user_profile_bio_rejected
    else -> null
}
