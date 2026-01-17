package world.hachimi.app.api.module

import io.ktor.client.content.ProgressListener
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.io.Source
import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import kotlin.time.Instant

class PlaylistModule(
    private val client: ApiClient
) {
    @Serializable
    data class PlaylistItem(
        val id: Long,
        val name: String,
        val coverUrl: String?,
        val description: String?,
        val createTime: Instant,
        val isPublic: Boolean,
        val songsCount: Int,
    )

    @Serializable
    data class ListResp(
        val playlists: List<PlaylistItem>,
    )

    suspend fun list(): WebResult<ListResp> =
        client.get("/playlist/list")

    @Serializable
    data class ListContainingReq(
        val songId: Long
    )

    @Serializable
    data class ListContainingResp(
        val playlistIds: List<Long>
    )

    suspend fun listContaining(req: ListContainingReq): WebResult<ListContainingResp> =
        client.get("/playlist/list_containing", req)

    @Serializable
    data class PlaylistIdReq(
        val id: Long
    )

    @Serializable
    data class DetailResp(
        val playlistInfo: PlaylistItem,
        val songs: List<SongItem>
    )

    @Serializable
    data class SongItem(
        val songId: Long,
        val songDisplayId: String,
        val title: String,
        val subtitle: String,
        val coverUrl: String,
        val uploaderName: String,
        val uploaderUid: Long,
        val durationSeconds: Int,
        val orderIndex: Int,
        val addTime: Instant,
    )

    suspend fun detailPrivate(req: PlaylistIdReq): WebResult<DetailResp> =
        client.get("/playlist/detail_private", req)

    @Serializable
    data class CreatePlaylistReq(
        val name: String,
        val description: String?,
        val isPublic: Boolean,
    )

    @Serializable
    data class CreatePlaylistResp(
        val id: Long,
    )

    suspend fun create(req: CreatePlaylistReq): WebResult<CreatePlaylistResp> =
        client.post("/playlist/create", req)

    @Serializable
    data class UpdatePlaylistReq(
        val id: Long,
        val name: String,
        val description: String?,
        val isPublic: Boolean,
    )

    suspend fun update(req: UpdatePlaylistReq): WebResult<Unit> =
        client.post("/playlist/update", req)

    suspend fun delete(req: PlaylistIdReq): WebResult<Unit> =
        client.post("/playlist/delete", req)

    @Serializable
    data class AddSongReq(
        val playlistId: Long,
        val songId: Long,
    )

    suspend fun addSong(req: AddSongReq): WebResult<Unit> =
        client.post("/playlist/add_song", req)

    suspend fun removeSong(req: AddSongReq): WebResult<Unit> =
        client.post("/playlist/remove_song", req)

    @Serializable
    data class ChangeOrderReq(
        val playlistId: Long,
        val songId: Long,
        /// Start from 0
        val targetOrder: Int,
    )

    suspend fun changeOrder(req: ChangeOrderReq): WebResult<Unit> =
        client.post("/playlist/change_order", req)

    @Serializable
    data class SetCoverReq(
        val playlistId: Long
    )

    suspend fun setCover(
        req: SetCoverReq,
        filename: String,
        source: Source,
        listener: ProgressListener
    ): WebResult<Unit> =
        client.postWith("/playlist/set_cover") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "json",
                            client.json.encodeToString(req),
                            headersOf(HttpHeaders.ContentType, "application/json")
                        )
                        append(
                            "image",
                            source,
                            headersOf(HttpHeaders.ContentDisposition, "filename=\"${filename}\"")
                        )
                    }
                )
            )
            onUpload(listener)
        }
}