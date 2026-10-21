package world.hachimi.app.model

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.comment_error_banned
import hachimiworld.composeapp.generated.resources.comment_error_empty
import hachimiworld.composeapp.generated.resources.comment_error_not_found
import hachimiworld.composeapp.generated.resources.comment_error_permission_denied
import hachimiworld.composeapp.generated.resources.comment_error_rate_limited
import hachimiworld.composeapp.generated.resources.comment_error_rejected
import hachimiworld.composeapp.generated.resources.comment_error_target_not_found
import hachimiworld.composeapp.generated.resources.comment_error_too_long
import org.jetbrains.compose.resources.StringResource

/**
 * The localized message for an error code from the `/comment/` endpoints, or null to show the
 * server's message. Content checks don't say why a comment was rejected, so neither does this.
 * @since 261021
 */
internal fun commentErrorMessage(code: String): StringResource? = when (code) {
    "comment_rejected" -> Res.string.comment_error_rejected
    "comment_empty" -> Res.string.comment_error_empty
    "comment_too_long" -> Res.string.comment_error_too_long
    "rate_limited" -> Res.string.comment_error_rate_limited
    "user_banned" -> Res.string.comment_error_banned
    "comment_not_found" -> Res.string.comment_error_not_found
    "target_not_found" -> Res.string.comment_error_target_not_found
    "permission_denied" -> Res.string.comment_error_permission_denied
    else -> null
}
