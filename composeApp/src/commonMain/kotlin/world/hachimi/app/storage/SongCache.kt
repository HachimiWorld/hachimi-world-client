package world.hachimi.app.storage

import kotlinx.io.Source

interface SongCache {
    suspend fun get(key: String): Source?
    suspend fun save(source: Source, key: String)
}