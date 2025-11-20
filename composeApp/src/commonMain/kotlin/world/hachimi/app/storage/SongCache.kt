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
    suspend fun clear()
    suspend fun getSize(): Long
    suspend fun trim(maxSize: Long)
    suspend fun getFreeSpace(): Long
    suspend fun getTotalSpace(): Long

    data class Item(
        val key: String,
        val metadata: SongDetailInfo,
        val audio: Source,
        val cover: Source
    )
}