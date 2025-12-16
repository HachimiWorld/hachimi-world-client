package world.hachimi.app.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger
import world.hachimi.app.model.SongDetailInfo

class SongCacheImpl : SongCache {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        prettyPrint = false
    }

    val cacheDir = getPlatform().getCacheDir().file.resolve("song_caches").also { it.mkdirs() }

    override suspend fun get(key: String): SongCache.Item? = withContext(Dispatchers.IO) {
        val audioFile = cacheDir.resolve(key)
            .takeIf { it.exists() && it.length() != 0L } ?: return@withContext null
        val coverFile = cacheDir.resolve("${key}_cover")
            .takeIf { it.exists() && it.length() != 0L } ?: return@withContext null
        val metadataFile = cacheDir.resolve("${key}_metadata")
            .takeIf { it.exists() && it.length() != 0L } ?: return@withContext null
        val metadata = try {
            json.decodeFromString<SongDetailInfo>(metadataFile.readText())
        } catch (e: Throwable) {
            Logger.w("SongCache", "Failed to decode metadata file, just skip cache", e)
            return@withContext null
        }
        SongCache.Item(
            key = key,
            metadata = metadata,
            audio = Buffer().apply { write(audioFile.readBytes()) },
            cover = Buffer().apply { write(coverFile.readBytes()) }
        )
    }

    override suspend fun save(item: SongCache.Item): Unit = withContext(Dispatchers.IO) {
        val audioFile = cacheDir.resolve("${item.key}_temp")
        val coverFile = cacheDir.resolve("${item.key}_cover_temp")
        val metadataFile = cacheDir.resolve("${item.key}_metadata_temp")

        audioFile.writeBytes(item.audio.readByteArray())
        audioFile.renameTo(cacheDir.resolve(item.key))
        coverFile.writeBytes(item.cover.readByteArray())
        coverFile.renameTo(cacheDir.resolve("${item.key}_cover"))
        metadataFile.writeText(json.encodeToString(item.metadata))
        metadataFile.renameTo(cacheDir.resolve("${item.key}_metadata"))
    }

    override suspend fun delete(key: String) {
        cacheDir.resolve(key).let { if (it.exists()) it.delete() }
        cacheDir.resolve("${key}_cover").let { if (it.exists()) it.delete() }
    }

    override suspend fun deleteMetadata(key: String) {
        cacheDir.resolve("${key}_metadata").let { if (it.exists()) it.delete() }
    }

    override suspend fun getMetadata(key: String): SongDetailInfo? = withContext(Dispatchers.IO) {
        val metadataFile = cacheDir.resolve("${key}_metadata").takeIf { it.exists() } ?: return@withContext null
        try {
            json.decodeFromString<SongDetailInfo>(metadataFile.readText())
        } catch (e: Throwable) {
            Logger.w("SongCache", "Failed to decode metadata file, just skip cache", e)
            null
        }
    }

    override suspend fun saveMetadata(item: SongDetailInfo) = withContext(Dispatchers.IO) {
        val metadataFile = cacheDir.resolve("${item.id}_metadata")
        metadataFile.writeText(json.encodeToString(item))
    }
}