package world.hachimi.app.model

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.change_email_err_email_existed
import hachimiworld.composeapp.generated.resources.change_email_err_invalid_email
import hachimiworld.composeapp.generated.resources.change_email_err_invalid_new_code
import hachimiworld.composeapp.generated.resources.change_email_err_invalid_old_code
import hachimiworld.composeapp.generated.resources.change_email_err_password_not_match
import hachimiworld.composeapp.generated.resources.change_email_err_pending_exists
import hachimiworld.composeapp.generated.resources.change_email_err_same_email
import hachimiworld.composeapp.generated.resources.change_email_err_too_frequent
import hachimiworld.composeapp.generated.resources.change_email_err_too_many_attempts
import hachimiworld.composeapp.generated.resources.change_email_err_too_many_requests
import org.jetbrains.compose.resources.StringResource

/**
 * The localized message for an error code from `/auth/email/...`, or null to show the server's message.
 * @since 261105
 */
internal fun emailChangeErrorMessage(code: String): StringResource? = when (code) {
    "invalid_email" -> Res.string.change_email_err_invalid_email
    "same_email" -> Res.string.change_email_err_same_email
    "email_existed" -> Res.string.change_email_err_email_existed
    "too_many_requests" -> Res.string.change_email_err_too_many_requests
    "invalid_old_code" -> Res.string.change_email_err_invalid_old_code
    "invalid_new_code" -> Res.string.change_email_err_invalid_new_code
    "password_not_match" -> Res.string.change_email_err_password_not_match
    "too_many_attempts" -> Res.string.change_email_err_too_many_attempts
    "too_frequent" -> Res.string.change_email_err_too_frequent
    "pending_exists" -> Res.string.change_email_err_pending_exists
    else -> null
}
