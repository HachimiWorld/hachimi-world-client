package world.hachimi.app.storage

import kotlinx.io.Source
import world.hachimi.app.model.SongDetailInfo

interface SongCache {
    suspend fun getMetadata(key: String): SongDetailInfo?
    suspend fun saveMetadata(item: SongDetailInfo)
    suspend fun get(key: String): Item?
    suspend fun save(item: Item)
    suspend fun delete(key: String)
    suspend fun deleteMetadata(key: String)

    data class Item(
        val key: String,
        val metadata: SongDetailInfo,
        val audio: Source,
        val cover: Source
    )
}