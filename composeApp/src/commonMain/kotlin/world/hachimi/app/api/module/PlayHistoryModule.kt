package world.hachimi.app.api.module

import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import kotlin.time.Instant

class PlayHistoryModule(
    private val client: ApiClient
) {
    @Serializable
    data class CursorReq(
        val cursor: Instant?,
        val size: Int
    )

    @Serializable
    data class CursorResp(
        val list: List<PlayHistoryItem>
    )

    @Serializable
    data class PlayHistoryItem(
        @Deprecated("Meaningless, deprecated since 260331")
        val id: Long,
        val songInfo: SongModule.PublicSongDetail,
        val playTime: Instant
    )

    suspend fun cursor(req: CursorReq): WebResult<CursorResp> = client.get("/play_history/cursor", req)

    @Serializable
    data class TouchReq(
        val songId: Long
    )

    suspend fun touch(songId: Long): WebResult<Unit> = client.post("/play_history/touch", TouchReq(songId))

    suspend fun touchAnonymous(songId: Long): WebResult<Unit> = client.post("/play_history/touch_anonymous", TouchReq(songId), auth = false)

    @Serializable
    data class DeleteReq(
        val historyId: Long
    )

    suspend fun delete(historyId: Long): WebResult<Unit> = client.post("/play_history/delete", DeleteReq(historyId))
}