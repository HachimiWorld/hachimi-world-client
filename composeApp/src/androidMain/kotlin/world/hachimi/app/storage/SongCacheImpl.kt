package world.hachimi.app.storage

import io.github.vinceglb.filekit.AndroidFile
import kotlinx.io.Source
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import world.hachimi.app.getPlatform

class SongCacheImpl : SongCache {
    override suspend fun get(key: String): Source? {
        val dir = getPlatform().getCacheDir().androidFile as AndroidFile.FileWrapper
        val cacheFile = dir.file
            .resolve("song_caches").also {
                it.mkdirs()
            }
            .resolve(key)
        return if (cacheFile.exists()) {
            cacheFile.inputStream().asSource().buffered()
        } else {
            null
        }
    }

    override suspend fun save(source: Source, key: String) {
        val dir = getPlatform().getCacheDir().androidFile as AndroidFile.FileWrapper
        val cacheFile = dir.file
            .resolve("song_caches").also {
                it.mkdirs()
            }
            .resolve(key)
        cacheFile.outputStream().use {
            source.transferTo(it.asSink())
        }
    }
}