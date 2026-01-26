package world.hachimi.app.api.module

import io.ktor.client.content.ProgressListener
import kotlinx.io.Source
import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import kotlin.time.Instant

class PostModule(
    private val client: ApiClient
) {
    @Serializable
    data class PageReq(
        val pageIndex: Int,
        val pageSize: Int,
    )

    @Serializable
    data class PageResp(
        val posts: List<PostDetail>,
    )

    @Serializable
    data class PostDetail(
        val id: Long,
        val author: UserModule.PublicUserProfile,
        val title: String,
        val content: String,
        val contentType: String,
        val coverUrl: String?,
        val createTime: Instant,
        val updateTime: Instant,
    )

    @Serializable
    data class PostIdReq(
        val postId: Long,
    )

    suspend fun page(req: PageReq): WebResult<PageResp> =
        client.get("/post/page", req)

    suspend fun detail(req: PostIdReq): WebResult<PostDetail> =
        client.get("/post/detail", req)

    @Serializable
    data class CreateReq(
        val title: String,
        val content: String,
        val contentType: String,
        val coverFileId: String?,
    )

    @Serializable
    data class CreateResp(
        val id: Long,
    )

    suspend fun create(req: CreateReq): WebResult<CreateResp> =
        client.post("/post/create", req)

    @Serializable
    data class EditReq(
        val postId: Long,
        val title: String?,
        val content: String?,
        val coverFileId: String?,
    )

    suspend fun edit(req: EditReq): WebResult<Unit> =
        client.post("/post/edit", req)

    suspend fun delete(req: PostIdReq): WebResult<Unit> =
        client.post("/post/delete", req)

    @Serializable
    data class UploadImageResp(
        val fileId: String
    )

    suspend fun uploadImage(filename: String, source: Source, listener: ProgressListener? = null): WebResult<UploadImageResp>
        = client.upload("/post/upload_image", filename, source, listener)
}