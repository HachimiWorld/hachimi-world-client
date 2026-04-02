package world.hachimi.app.api.module

import io.ktor.client.content.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.io.Source
import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult


class UserModule(
    private val client: ApiClient
) {

    @Serializable
    data class ProfileReq(
        val uid: Long
    )

    @Serializable
    data class PublicUserProfile(
        val uid: Long,
        val username: String,
        val avatarUrl: String?,
        val bio: String?,
        val gender: Int?,
        val isBanned: Boolean,
        /**
         * @since 260401
         */
        val connectedAccounts: List<ConnectedAccountItem>
    )

    @Serializable
    data class ConnectedAccountItem(
        val type: String,
        val id: String,
        val name: String
    )

    suspend fun profile(uid: Long): WebResult<PublicUserProfile> =
        client.get("/user/profile", ProfileReq(uid))

    @Serializable
    data class UpdateProfileReq(
        val username: String,
        val bio: String?,
        val gender: Int?,
    )

    suspend fun updateProfile(req: UpdateProfileReq): WebResult<Unit> =
        client.post("/user/update_profile", req)

    suspend fun setAvatar(filename: String, source: Source, listener: ProgressListener? = null): WebResult<Unit> {
        return client.postWith("/user/set_avatar") {
            timeout {
                connectTimeoutMillis = 30_000
                requestTimeoutMillis = 30_000
            }
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "image",
                            InputProvider { source },
                            headersOf(HttpHeaders.ContentDisposition, "filename=\"${filename}\"")
                        )
                    }
                )
            )
            onUpload(listener)
        }
    }

    @Serializable
    data class SearchReq(
        val q: String,
        val page: Int,
        val size: Int,
    )

    @Serializable
    data class SearchResp(
        val hits: List<PublicUserProfile>,
        val query: String,
        val processingTimeMs: Long,
        val totalHits: Long?,
        val limit: Long,
        val offset: Long,
    )

    suspend fun search(req: SearchReq): WebResult<SearchResp> =
        client.get("/user/search", req, false)

    @Serializable
    data class ConnectionResp(
        val items: List<ConnectionItem>,
    )

    @Serializable
    data class ConnectionItem(
        val type: String,
        val id: String,
        val name: String,
        val public: Boolean,
    )

    companion object {
        const val CONNECTION_TYPE_BILIBILI = "bilibili"
    }

    suspend fun connectionList(): WebResult<ConnectionResp> =
        client.get("/user/connection/list")

    @Serializable
    data class ConnectionUnlinkReq(
        val type: String
    )

    suspend fun connectionUnlink(req: ConnectionUnlinkReq): WebResult<Unit> =
        client.post("/user/connection/unlink", req)

    @Serializable
    data class ConnectionSetVisibilityReq(
        val type: String,
        val visibility: String,
    )

    suspend fun connectionSetVisibility(req: ConnectionSetVisibilityReq): WebResult<Unit> =
        client.post("/user/connection/set_visibility", req)

    @Serializable
    data class ConnectionSyncReq(
        val type: String,
    )

    suspend fun connectionSync(req: ConnectionSyncReq): WebResult<Unit> =
        client.post("/user/connection/sync", req)

    @Serializable
    data class ConnectionGenerateChallengeReq(
        val type: String,
        val providerAccountId: String,
    )

    @Serializable
    data class ConnectionGenerateChallengeResp(
        val challengeId: String,
        val challenge: String,
    )

    suspend fun connectionGenerateChallenge(req: ConnectionGenerateChallengeReq): WebResult<ConnectionGenerateChallengeResp> =
        client.post("/user/connection/generate_challenge", req)

    @Serializable
    data class ConnectionVerifyChallengeReq(
        val challengeId: String,
    )

    suspend fun connectionVerifyChallenge(req: ConnectionVerifyChallengeReq): WebResult<Unit> =
        client.post("/user/connection/verify_challenge", req)
}