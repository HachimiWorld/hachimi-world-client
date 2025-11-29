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
        val audioFile = cacheDir.resolve(key).takeIf { it.exists() } ?: return@withContext null
        val coverFile = cacheDir.resolve("${key}_cover").takeIf { it.exists() } ?: return@withContext null
        val metadataFile = cacheDir.resolve("${key}_metadata").takeIf { it.exists() } ?: return@withContext null
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

    override suspend fun save(item: SongCache.Item) = withContext(Dispatchers.IO) {
        val audioFile = cacheDir.resolve(item.key)
        val coverFile = cacheDir.resolve("${item.key}_cover")
        val metadataFile = cacheDir.resolve("${item.key}_metadata")

        audioFile.writeBytes(item.audio.readByteArray())
        coverFile.writeBytes(item.cover.readByteArray())
        metadataFile.writeText(json.encodeToString(item.metadata))
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

    override suspend fun clear() = withContext(Dispatchers.IO) {
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
        Unit
    }

    override suspend fun getSize(): Long = withContext(Dispatchers.IO) {
        cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }

    override suspend fun trim(maxSize: Long) = withContext(Dispatchers.IO) {
        if (getSize() <= maxSize) return@withContext

        val files = cacheDir.listFiles() ?: return@withContext
        val audioFiles = files.filter { !it.name.endsWith("_cover") && !it.name.endsWith("_metadata") }
        val sortedAudioFiles = audioFiles.sortedBy { it.lastModified() }

        var currentSize = getSize()
        for (file in sortedAudioFiles) {
            if (currentSize <= maxSize) break
            val key = file.name
            val cover = cacheDir.resolve("${key}_cover")
            val metadata = cacheDir.resolve("${key}_metadata")

            val sizeFreed = file.length() + (if (cover.exists()) cover.length() else 0) + (if (metadata.exists()) metadata.length() else 0)

            file.delete()
            if (cover.exists()) cover.delete()
            if (metadata.exists()) metadata.delete()

            currentSize -= sizeFreed
        }
    }

    override suspend fun getFreeSpace(): Long = withContext(Dispatchers.IO) {
        cacheDir.freeSpace
    }

    override suspend fun getTotalSpace(): Long = withContext(Dispatchers.IO) {
        cacheDir.totalSpace
    }
}