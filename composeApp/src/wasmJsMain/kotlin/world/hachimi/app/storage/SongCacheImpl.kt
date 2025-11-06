package world.hachimi.app.storage

import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json
import world.hachimi.app.logging.Logger
import world.hachimi.app.model.SongDetailInfo

class SongCacheImpl : SongCache {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    override suspend fun delete(key: String) {
    }

    override suspend fun deleteMetadata(key: String) {
        localStorage.removeItem("song_caches/${key}_metadata")
    }

    override suspend fun get(key: String): SongCache.Item? {
        // TODO[feat](cache): Currently not supported on WASM.
        return null
    }

    override suspend fun save(item: SongCache.Item) {
        // TODO[feat](cache): Currently not supported on WASM.
    }

    override suspend fun getMetadata(key: String): SongDetailInfo? {
        val data = localStorage.getItem("song_caches/${key}_metadata") ?: return null
        return try {
            json.decodeFromString<SongDetailInfo>(data)
        } catch (e: Throwable) {
            Logger.w("SongCache", "Failed to decode metadata file, just skip cache", e)
            null
        }
    }

    override suspend fun saveMetadata(item: SongDetailInfo) {
        localStorage.setItem("song_caches/${item.displayId}_metadata", json.encodeToString(item))
    }
}