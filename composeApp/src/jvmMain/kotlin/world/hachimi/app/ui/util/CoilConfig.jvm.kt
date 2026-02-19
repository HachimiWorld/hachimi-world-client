package world.hachimi.app.ui.util

import coil3.disk.DiskCache
import io.github.vinceglb.filekit.resolve
import okio.Path.Companion.toOkioPath
import world.hachimi.app.getPlatform

actual fun getPlatformDiskCache(): DiskCache? {
    return DiskCache.Builder()
        .directory(getPlatform().getCacheDir().resolve("image_caches").file.toOkioPath())
        .build()
}