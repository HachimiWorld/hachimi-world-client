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
import kotlin.time.Instant

class SongModule(
    private val client: ApiClient
) {
    @Serializable
    data class SongListResp(
        val songIds: List<String>
    )

    @Deprecated(message = "Deprecated since 250831", level = DeprecationLevel.ERROR)
    suspend fun recent(): WebResult<SongListResp> = client.get("/song/recent", false)

    @Deprecated(message = "Deprecated since 250831", level = DeprecationLevel.ERROR)
    suspend fun hot(): WebResult<SongListResp> = client.get("/song/hot", false)

    @Serializable
    data class RecentResp(
        val songs: List<PublicSongDetail>
    )

    /**
     * @since 251102
     */
    @Serializable
    data class RecentReq(
        val cursor: Instant?,
        val limit: Int,
        val after: Boolean
    )

    suspend fun recentV2(req: RecentReq): WebResult<RecentResp> = client.get("/song/recent_v2", req, true)

    @Serializable
    data class RecommendResp(
        val songs: List<PublicSongDetail>
    )

    suspend fun recommend(): WebResult<RecommendResp> = client.get("/song/recommend", true)
    suspend fun recommendAnonymous(): WebResult<RecommendResp> = client.get("/song/recommend_anonymous", true)

    @Serializable
    data class HotResp(
        val songs: List<PublicSongDetail>
    )

    suspend fun hotWeekly(): WebResult<HotResp> = client.get("/song/hot/weekly", true)

    @Serializable
    data class PublicSongDetail(
        val id: Long,
        val displayId: String,
        val title: String,
        val subtitle: String,
        val description: String,
        val durationSeconds: Int,
        val tags: List<TagItem>,
        val lyrics: String,
        val audioUrl: String,
        val coverUrl: String,
        val productionCrew: List<SongProductionCrew>,
        val creationType: Int,
        val originInfos: List<CreationTypeInfo>,
        val uploaderUid: Long,
        val uploaderName: String,
        val playCount: Long,
        val likeCount: Long,
        val externalLinks: List<ExternalLink>,
        /**
         * @since 251102
         */
        val createTime: Instant,
        /**
         * @since 251102
         */
        val releaseTime: Instant,
        /**
         * @since 251105
         */
        val explicit: Boolean? = null
    )

    @Serializable
    data class TagItem(
        val id: Long,
        val name: String,
        val description: String?,
    )

    @Serializable
    data class SongProductionCrew(
        val id: Long,
        val role: String,
        val uid: Long?,
        val personName: String?,
    )

    @Serializable
    data class CreationTypeInfo(
        // If `song_id` is Some, the rest fields could be None
        val songDisplayId: String?,
        val title: String?,
        val artist: String?,
        val url: String?,
        val originType: Int,
    )

    @Serializable
    data class ExternalLink(
        val platform: String,
        val url: String,
    )

    @Serializable
    data class DetailReq(
        /// Actually displayed id
        val id: String,
    )

    suspend fun detail(displayId: String): WebResult<PublicSongDetail> =
        client.get("/song/detail", DetailReq(displayId), false)

    @Serializable
    data class UploadImageResp(
        val tempId: String
    )

    suspend fun uploadCoverImage(filename: String, source: Source, listener: ProgressListener? = null): WebResult<UploadImageResp> {
        return client.postWith("/song/upload_cover_image") {
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
    data class UploadAudioFileResp(
        val tempId: String,
        val durationSecs: Long,
        val title: String?,
        val bitrate: String?,
        val artist: String?,
    )

    suspend fun uploadAudioFile(filename: String, source: Source, listener: ProgressListener? = null): WebResult<UploadAudioFileResp> {
        return client.postWith("/song/upload_audio_file") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "audio",
                            InputProvider { source },
                            headersOf(HttpHeaders.ContentDisposition, "filename=\"${filename}\"")
                        )
                    }
                )
            )
            timeout {
                socketTimeoutMillis = 60_000
                connectTimeoutMillis = 60_000
                requestTimeoutMillis = 60_000
            }
            onUpload(listener)
        }
    }


    @Serializable
    data class PublishReq (
        val songTempId: String,
        val coverTempId: String,
        val title: String,
        val subtitle: String,
        val description: String,
        val lyrics: String,
        val tagIds: List<Long>,
        val creationInfo: CreationInfo,
        val productionCrew: List<ProductionItem>,
        val externalLinks: List<ExternalLink>,
        /**
         * @since 251105
         */
        val explicit: Boolean?
    ) {
        @Serializable
        data class CreationInfo (
            // 0: original, 1: derivative work, 2: tertiary work
            val creationType: Int,
            val originInfo: CreationTypeInfo?,
            val derivativeInfo: CreationTypeInfo?,
        )

        @Serializable
        data class  ProductionItem(
            val role: String,
            val uid: Long?,
            val name: String?,
        )
    }

    @Serializable
    data class PublishResp(
        val songDisplayId: String
    )

    suspend fun publish(req: PublishReq): WebResult<PublishResp> =
        client.post("/song/publish", req)

    @Serializable
    data class SearchReq(
        val q: String,
        val limit: Int?,
        val offset: Int?,
        val filter: String?,
    )

    @Serializable
    data class SearchResp(
        val hits: List<SearchSongItem>,
        val query: String,
        val processingTimeMs: Long,
        val totalHits: Int?,
        val limit: Int,
        val offset: Int,
    )

    @Serializable
    data class SearchSongItem(
        val id: Long,
        val displayId: String,
        val title: String,
        val subtitle: String,
        val description: String,
        val artist: String,
        val durationSeconds: Int,
        val playCount: Long,
        val likeCount: Long,
        val coverArtUrl: String,
        val audioUrl: String,
        val uploaderUid: Long,
        val uploaderName: String,
        /**
         * @since 251105
         */
        val explicit: Boolean? = null
    )

    suspend fun search(req: SearchReq): WebResult<SearchResp> =
        client.get("/song/search", req, false)

    @Serializable
    data class TagCreateReq (
        val name: String,
        val description: String?,
    )

    @Serializable
    data class TagCreateResp (
        val id: Long
    )

    suspend fun tagCreate(req: TagCreateReq): WebResult<TagCreateResp>
        = client.post("/song/tag/create", req)

    @Serializable
    data class TagSearchReq(
        val query: String
    )

    @Serializable
    data class TagSearchResp(
        val result: List<TagItem>
    )

    suspend fun tagSearch(req: TagSearchReq): WebResult<TagSearchResp>
        = client.get("/song/tag/search", req)

    @Serializable
    data class PageByUserReq(
        val userId: Long,
        val page: Long?,
        val size: Long?,
    )

    @Serializable
    data class PageByUserResp(
        val songs: List<PublicSongDetail>,
        val total: Long,
        val page: Long,
        val size: Long,
    )

    suspend fun pageByUser(req: PageByUserReq): WebResult<PageByUserResp>
        = client.get("/song/page_by_user", req)
}