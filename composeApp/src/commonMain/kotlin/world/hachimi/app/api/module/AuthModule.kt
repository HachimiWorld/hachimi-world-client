package world.hachimi.app.api.module

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import kotlin.time.Instant

class AuthModule(
    private val client: ApiClient
) {
    @Serializable
    data class TokenPair(
        val accessToken: String,
        val refreshToken: String,
        val expiresIn: Instant,
    )

    @Serializable
    data class LoginReq(
        val email: String,
        val password: String,
        val code: String?,
        val deviceInfo: String,
        val captchaKey: String
    )

    @Serializable
    data class LoginResp(
        val uid: Long,
        val username: String,
        val token: TokenPair
    )

    suspend fun loginEmail(req: LoginReq): WebResult<LoginResp> =
        client.post("/auth/login/email", req, auth = false)

    @Serializable
    data class RegisterReq(
        val email: String,
        val password: String,
        val code: String,
        val deviceInfo: String,
        val captchaKey: String
    )

    @Serializable
    data class RegisterResp(
        val uid: Long,
        val generatedUsername: String,
        val token: TokenPair,
    )

    suspend fun registerEmail(req: RegisterReq): WebResult<RegisterResp> =
        client.post("/auth/register/email", req, auth = false)


    @Serializable
    data class SendEmailCodeReq(val email: String)

    suspend fun sendEmailCode(req: SendEmailCodeReq): WebResult<Unit> =
        client.post("/auth/send_email_code", req, auth = false)

    @Serializable
    class RefreshTokenReq(
        val refreshToken: String,
        val deviceInfo: String,
    )

    suspend fun rawRefreshToken(req: RefreshTokenReq): HttpResponse  {
        return client.httpClient.post(client.buildUrl("/auth/refresh_token")) {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body<HttpResponse>()
    }

    @Serializable
    data class GenerateCaptchaResp(
        val captchaKey: String,
        val url: String,
    )

    suspend fun generateCaptcha(): WebResult<GenerateCaptchaResp>
        = client.get("/auth/captcha/generate", auth = false)

    @Serializable
    data class SubmitCaptchaReq(
        val captchaKey: String,
        val token: String,
    )

    suspend fun submitCaptcha(req: SubmitCaptchaReq): WebResult<Unit>
        = client.post("/auth/captcha/submit", req, auth = false)


    @Serializable
    data class ResetPasswordReq(
        val email: String,
        val code: String,
        val newPassword: String,
        val logoutAllDevices: Boolean,
        val captchaKey: String,
    )

    suspend fun resetPassword(req: ResetPasswordReq): WebResult<Unit>
        = client.post("/auth/reset_password", req, auth = false)


    @Serializable
    data class DeviceItem(
        val id: Long,
        val deviceInfo: String?,
        val ipAddress: String?,
        val lastUsedTime: Instant?,
        val createTime: Instant,
        /**
         * @since 260630
         */
        val tokenId: String
    )


    @Serializable
    data class DeviceListResp(
        val devices: List<DeviceItem>,
    )

    @Serializable
    data class DeviceLogoutReq(
        val deviceId: Long,
    )

    suspend fun deviceList(): WebResult<DeviceListResp> =
        client.get("/auth/device/list")

    suspend fun deviceLogout(req: DeviceLogoutReq): WebResult<Unit> =
        client.post("/auth/device/logout", req)

    /** For POSTs without a body. @since 261105 */
    @Serializable
    class EmptyReq

    /** @since 261105 */
    @Serializable
    data class PendingEmailChange(
        val newEmail: String,
        val effectiveTime: Instant,
        val createTime: Instant,
    )

    /** @since 261105 */
    @Serializable
    data class EmailInfoResp(
        val email: String,
        val pending: PendingEmailChange?,
        /** When a change without the current email can be requested again, or null if now. */
        val nextRequestTime: Instant?,
    )

    /** @since 261105 */
    suspend fun emailInfo(): WebResult<EmailInfoResp> =
        client.get("/auth/email/info")

    /** Sends a code to the current email. @since 261105 */
    suspend fun sendOldEmailCode(): WebResult<Unit> =
        client.post("/auth/email/send_old_code", EmptyReq())

    /** @since 261105 */
    @Serializable
    data class SendNewEmailCodeReq(val newEmail: String)

    /** Sends a code to the email to change to. @since 261105 */
    suspend fun sendNewEmailCode(req: SendNewEmailCodeReq): WebResult<Unit> =
        client.post("/auth/email/send_new_code", req)

    /** @since 261105 */
    @Serializable
    data class ChangeEmailReq(
        val newEmail: String,
        val oldCode: String,
        val newCode: String,
    )

    /** Changes the email at once, with codes from both emails. @since 261105 */
    suspend fun changeEmail(req: ChangeEmailReq): WebResult<Unit> =
        client.post("/auth/email/change", req)

    /** @since 261105 */
    @Serializable
    data class RequestEmailChangeReq(
        val newEmail: String,
        val newCode: String,
        val password: String,
    )

    /** Changes the email after a wait, for users who can't read their current email. @since 261105 */
    suspend fun requestEmailChange(req: RequestEmailChangeReq): WebResult<PendingEmailChange> =
        client.post("/auth/email/request_change", req)

    /** @since 261105 */
    suspend fun cancelEmailChange(): WebResult<Unit> =
        client.post("/auth/email/cancel_change", EmptyReq())

    /** @since 261105 */
    @Serializable
    data class RevertEmailChangeReq(
        val token: String,
        val newPassword: String,
    )

    /** From the link sent to the old email. No login needed. @since 261105 */
    suspend fun revertEmailChange(req: RevertEmailChangeReq): WebResult<Unit> =
        client.post("/auth/email/revert_change", req, auth = false)
}
