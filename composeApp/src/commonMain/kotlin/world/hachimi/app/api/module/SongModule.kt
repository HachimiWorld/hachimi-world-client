package world.hachimi.app.api.module

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
        val explicit: Boolean?,
        /**
         * @since 251106
         */
        val gain: Float?
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
    data class DetailByIdReq(
        val id: Long,
    )

    suspend fun detailById(id: Long): WebResult<PublicSongDetail> =
        client.get("/song/detail_by_id", DetailByIdReq(id), false)
    @Serializable
    data class SearchReq(
        val q: String,
        val limit: Int?,
        val offset: Int?,
        val filter: String?,
        /**
         * Default: relevance
         * @since 260114
         */
        val sortBy: String? = null
    ) {
        companion object {
            const val SORT_BY_RELEVANCE = "relevance"
            const val SORT_BY_RELEASE_TIME_DESC = "release_time_desc"
            const val SORT_BY_RELEASE_TIME_ASC = "release_time_asc"
        }
    }

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
        val explicit: Boolean?,
        /**
         * @since 260114
         */
        val originalArtists: List<String>,
        /**
         * @since 260114
         */
        val originalTitles: List<String>
    )

    suspend fun search(req: SearchReq): WebResult<SearchResp> =
        client.get("/song/search", req, false)

    @Serializable
    data class TagCreateReq(
        val name: String,
        val description: String?,
    )

    @Serializable
    data class TagCreateResp(
        val id: Long
    )

    suspend fun tagCreate(req: TagCreateReq): WebResult<TagCreateResp> = client.post("/song/tag/create", req)

    @Serializable
    data class TagSearchReq(
        val query: String
    )

    @Serializable
    data class TagSearchResp(
        val result: List<TagItem>
    )

    suspend fun tagSearch(req: TagSearchReq): WebResult<TagSearchResp> = client.get("/song/tag/search", req)

    @Serializable
    data class TagRecommendItem(
        val id: Long,
        val name: String,
        val description: String?,
        val score: Long
    )

    @Serializable
    data class TagRecommendResp(
        val result: List<TagRecommendItem>
    )

    suspend fun tagRecommend(): WebResult<TagRecommendResp> = client.get("/song/tag/recommend")

    suspend fun tagRecommendAnonymous(): WebResult<TagRecommendResp> = client.get("/song/tag/recommend_anonymous")

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

    suspend fun pageByUser(req: PageByUserReq): WebResult<PageByUserResp> = client.get("/song/page_by_user", req)


    @Serializable
    data class LikeReq(
        val songId: Long,
        val playbackPositionSecs: Int?,
    )

    /**
     * @since 260329
     */
    suspend fun like(req: LikeReq): WebResult<Unit> = client.post("/song/likes/like", req)

    @Serializable
    data class UnlikeReq(
        val songId: Long
    )

    /**
     * @since 260329
     */
    suspend fun unlike(req: UnlikeReq): WebResult<Unit> = client.post("/song/likes/unlike", req)

    @Serializable
    data class LikeStatusReq(
        val songId: Long,
    )

    @Serializable
    data class LikeStatusResp(
        val liked: Boolean
    )

    /**
     * @since 260329
     */
    suspend fun likeStatus(req: LikeStatusReq): WebResult<LikeStatusResp> =
        client.get("/song/likes/status", req)

    @Serializable
    data class MyLikesReq(
        val pageIndex: Long,
        val pageSize: Long,
    )

    @Serializable
    data class MyLikesResp(
        val data: List<MyLikeItem>,
        val pageSize: Long,
        val pageIndex: Long,
        val total: Long,
    )

    @Serializable
    data class MyLikeItem(
        val songData: PublicSongDetail,
        val likedTime: Instant,
    )

    /**
     * @since 260329
     */
    suspend fun pageMyLikes(req: MyLikesReq): WebResult<MyLikesResp> = client.get("/song/likes/page_my_likes", req)
}