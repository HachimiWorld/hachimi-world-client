package world.hachimi.app.api.module

import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

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
            header("X-Real-IP", "127.0.0.1")
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
}