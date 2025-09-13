package world.hachimi.app.storage

import kotlinx.io.Source

class SongCacheImpl : SongCache {
    override suspend fun get(key: String): Source? {
        // TODO[feat](cache): Currently not supported on WASM.
        return null
    }

    override suspend fun save(source: Source, key: String) {
        // TODO[feat](cache): Currently not supported on WASM.
    }
}