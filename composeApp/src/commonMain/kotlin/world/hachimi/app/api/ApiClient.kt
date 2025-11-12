package world.hachimi.app.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import world.hachimi.app.api.module.*
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TAG = "ApiClient"

/**
 * Auto get refresh token
 */
class ApiClient(
    private val baseUrl: String,
) {
    companion object {
        const val VERSION: Int = 251106
    }

    @OptIn(ExperimentalSerializationApi::class)
    internal val json = Json {
        // Ignoring unknown keys increases the forward compatibility
        // Compatible schema changes:
        // - Add: Added a new field in a new writer. (by `ignoreUnknownKeys`)
        // Not compatible schema changes:
        // - Delete: Read a missing field without default value.
        // - Rename: Rename a field.
        // - Change primitive data type
        // - Change data type between primitive value, object, array

        // Backward compatibility:
        // - Add: When you read a field that is not present in the old data, should provide a default value.
        //     Generally it is `null`. Set `explicitNulls` to false automatically get the backward compatibility by default for nullable field.
        // - Delete: Do not read a field in the old data anymore. It's compatible by `ignoreUnknownKeys`
        ignoreUnknownKeys = true
        explicitNulls = false

        prettyPrint = false
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    internal val httpClient = HttpClient {
        install(ContentEncoding) {
            gzip()
        }
        install(ContentNegotiation) {
            json(json)
        }
        install(UserAgent) {
            agent = getPlatform().userAgent
        }
        install(HttpCache)
    }

    private val authLock = Mutex()
    internal var accessToken: String? = null
    internal var refreshToken: String? = null

    fun setToken(accessToken: String?, refreshToken: String?) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    private var authListener: AuthenticationListener = DefaultAuthenticationListener

    fun setAuthListener(authenticationListener: AuthenticationListener) {
        this.authListener = authenticationListener
    }

    internal fun buildUrl(path: String): String {
        return baseUrl + path
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateRequestId(): String = Uuid.random().toHexDashString()

    private fun HttpRequestBuilder.applyRequestId(id: String) {
        header("X-Request-Id", id)
    }

    internal suspend inline fun <reified T> postWith(
        path: String,
        auth: Boolean = true,
        crossinline block: HttpRequestBuilder.() -> Unit
    ): WebResult<T> {
        return withContext(Dispatchers.Default) {
            if (auth) refreshToken()

            val url = buildUrl(path)
            val requestId = generateRequestId()
            Logger.d(TAG, "POST(requestId=$requestId) to $path")
            val resp = httpClient.post(url) {
                applyAuth()
                applyRequestId(requestId)
                block()
            }.body<HttpResponse>()

            val data = checkAndDecode<T>(resp, path, requestId)
            data
        }
    }

    internal suspend inline fun <reified T> post(path: String, body: Any, auth: Boolean = true): WebResult<T> =
        withContext(Dispatchers.Default) {
            if (auth) refreshToken()

            val url = buildUrl(path)
            val requestId = generateRequestId()
            Logger.d(TAG, "POST(requestId=$requestId) to $path")
            val resp = httpClient.post(url) {
                applyAuth()
                applyRequestId(requestId)
                contentType(ContentType.Application.Json)
                setBody(body)
            }.body<HttpResponse>()

            val data = checkAndDecode<T>(resp, path, requestId)
            data
        }

    internal suspend inline fun <reified T> get(path: String, auth: Boolean = true): WebResult<T> =
        withContext(Dispatchers.Default) {
            if (auth) refreshToken()
            val url = buildUrl(path)
            val requestId = generateRequestId()
            Logger.d(TAG, "GET(requestId=${requestId}) to $path")
            val resp = httpClient.get(url) {
                applyAuth()
                applyRequestId(requestId)
            }.body<HttpResponse>()
            val data = checkAndDecode<T>(resp, path, requestId)
            data
        }

    internal suspend inline fun <reified P, reified T> get(path: String, query: P, auth: Boolean = true): WebResult<T> =
        withContext(Dispatchers.Default) {
            if (auth) refreshToken()
            val url = buildUrl(path)
            val requestId = generateRequestId()
            Logger.d(TAG, "GET(requestId=${requestId}) $path")
            val resp = httpClient.get(url) {
                applyAuth()
                applyRequestId(requestId)

                val encoded = json.encodeToJsonElement(query)
                encoded.jsonObject.forEach { (key, value) ->
                    if (value is JsonNull) return@forEach
                    parameter(key, value.jsonPrimitive.content)
                }
            }.body<HttpResponse>()
            val data = checkAndDecode<T>(resp, path, requestId)
            data
        }

    // FIXME(api): If any api call triggered refreshing token, and then be canceled manually,
    //  after refreshing request be sent, before the responded token is set to state and be saved,
    //  any further requests will take the old used refresh-token, then cause 401 error.
    //  Update: This issue might be fixed by using `NonCancellable` context
    internal suspend fun refreshToken() = withContext(Dispatchers.Default + NonCancellable) {
        // Only execute once at the same time
        if (authLock.isLocked) {
            authLock.withLock { }
        } else {
            authLock.withLock {
                if (accessToken != null) {
                    // Pick exp time from access toke
                    val claims = parseJwtWithoutVerification(accessToken!!)
                    val expiration = claims.getValue("exp").jsonPrimitive.long // In seconds
                    val now = Clock.System.now()
                    val expDate = Instant.fromEpochSeconds(expiration)

                    if (now >= expDate) {
                        Logger.d(TAG, "Access token expired, refreshing token")

                        val resp = try {
                            authModule.rawRefreshToken(
                                AuthModule.RefreshTokenReq(
                                    refreshToken = refreshToken!!,
                                    deviceInfo = "Desktop Client" // TODO: Get device info
                                )
                            )
                        } catch (e: Throwable) {
                            Logger.e(TAG, "refreshToken: Error requesting to refreshing token", e)
                            error("Error requesting to refreshing token: ${e.message}")
                        }

                        val requestId = resp.headers["X-Request-Id"] ?: "unknown"
                        if (resp.status == HttpStatusCode.OK) {
                            val result = checkAndDecode<AuthModule.TokenPair>(resp, "refresh_token", requestId)

                            if (result.ok) {
                                val data = result.okData<AuthModule.TokenPair>()
                                accessToken = data.accessToken
                                refreshToken = data.refreshToken
                                authListener.onTokenChange(data.accessToken, data.refreshToken)
                            } else {
                                val data = result.errData<CommonError>()
                                Logger.w(TAG, "refreshToken: Refreshing token returns error: ${data.code}, ${data.msg}")
                                authListener.onAuthenticationError(
                                    AuthError.RefreshTokenError(
                                        requestId,
                                        "refresh_token",
                                        data,
                                    )
                                )
                            }
                        } else {
                            Logger.w(TAG, "Refreshing token failed with ${resp.status}")
                            authListener.onAuthenticationError(
                                AuthError.ErrorHttpResponse(
                                    requestId,
                                    "refresh_token",
                                    resp,
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    internal suspend inline fun <reified T> checkAndDecode(
        resp: HttpResponse,
        endpoint: String,
        requestId: String
    ): WebResult<T> {
        if (resp.status == HttpStatusCode.OK) {
            return resp.body<WebResult<T>>()
        } else if (resp.status == HttpStatusCode.Unauthorized) {
            authListener.onAuthenticationError(AuthError.UnauthorizedDuringRequest(requestId, endpoint, resp))
        }
        val content = resp.bodyAsText()
        error("Error response for request[${requestId}]: ${resp.status} ${content.take(100)}")
    }

    internal fun HttpRequestBuilder.applyAuth() {
        if (accessToken != null) {
            header("Authorization", "Bearer $accessToken")
        }
        header("X-Real-IP", "127.0.0.1")
    }

    val authModule by lazy { AuthModule(this) }
    val userModule by lazy { UserModule(this) }
    val songModule by lazy { SongModule(this) }
    val playHistoryModule by lazy { PlayHistoryModule(this) }
    val publishModule by lazy { PublishModule(this) }
    val playlistModule by lazy { PlaylistModule(this) }
    val versionModule by lazy { VersionModule(this) }
}

interface AuthenticationListener {
    suspend fun onTokenChange(accessToken: String, refreshToken: String)
    suspend fun onAuthenticationError(err: AuthError)
}

/**
 * @return payload
 */
@OptIn(ExperimentalEncodingApi::class)
fun parseJwtWithoutVerification(token: String): JsonObject {
    val jwtParts = token.split(".")
    if (jwtParts.size >= 2) { // Check if token has at least header and payload
        val decoder = Base64.UrlSafe.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL)
        val headerJson = decoder.decode(jwtParts[0]).decodeToString()
        val payloadJson = decoder.decode(jwtParts[1]).decodeToString()

        return Json.parseToJsonElement(payloadJson).jsonObject
    }
    throw IllegalArgumentException("JWT format error")
}

sealed class AuthError {
    /**
     * This error should not happen since we check and refresh tokens before requesting.
     *
     * But could happen when Api Client access a protected endpoint without setting `auth` to `true`
     */
    data class UnauthorizedDuringRequest(val requestId: String, val endpoint: String, val response: HttpResponse) :
        AuthError()

    /**
     * If server returns HTTP status error during refreshing token
     *
     * When this occurs, it's better to report to backend, or retry.
     */
    data class ErrorHttpResponse(val requestId: String, val endpoint: String, val response: HttpResponse) : AuthError()

    /**
     * If the refresh token request returns an error, such as
     * - `invalid_refresh_token`
     * - `inconsistent_device`
     * - `token_revoked`?
     *
     * When this occurs, should let the user to re-log in
     */
    data class RefreshTokenError(val requestId: String, val endpoint: String, val error: CommonError) : AuthError()

    /**
     * Unknown exception during authentication procedure
     */
    data class UnknownError(val requestId: String, val endpoint: String, val exception: Throwable) : AuthError()
}

object DefaultAuthenticationListener : AuthenticationListener {
    override suspend fun onTokenChange(accessToken: String, refreshToken: String) {

    }

    override suspend fun onAuthenticationError(err: AuthError) {
        error("Authentication error: $err")
    }
}

@Serializable
data class WebResp<T, E>(
    val ok: Boolean,
    val data: JsonElement
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = false
            namingStrategy = JsonNamingStrategy.SnakeCase
        }
    }

    inline fun <reified U : T> okData(): U {
        return json.decodeFromJsonElement<U>(this.data)
    }

    inline fun <reified D : E> errData(): E {
        return json.decodeFromJsonElement<D>(this.data)
    }
}

typealias WebResult<T> = WebResp<T, CommonError>

@Serializable
data class CommonError(
    val code: String,
    val msg: String
)

// Ohh! I like extension function! :p
inline fun <reified T> WebResp<T, *>.ok(): T {
    if (!ok) error("Trying to decode ok data with error response!")
    return okData<T>()
}

inline fun <reified E> WebResp<*, E>.err(): E {
    if (ok) error("Trying to decode error data with ok response!")
    return errData<E>()
}