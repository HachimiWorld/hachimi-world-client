package world.hachimi.app.player

import world.hachimi.app.model.SongDetailInfo
import world.hachimi.app.storage.SongCache

class SongCacheImpl: SongCache {
    override suspend fun getMetadata(key: String): SongDetailInfo? {
        // TODO:
        return null
    }

    override suspend fun saveMetadata(item: SongDetailInfo) {
        // TODO:
    }

    override suspend fun get(key: String): SongCache.Item? {
        // TODO:
        return null
    }

    override suspend fun save(item: SongCache.Item) {
        // TODO:
    }
}