package world.hachimi.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import io.github.vinceglb.filekit.coil.addPlatformFileSupport

@Suppress("ComposableNaming")
@Composable
@ReadOnlyComposable
fun setupCoil() {
    // Let coil support PlatformFile
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                addPlatformFileSupport()
            }
            .diskCache {
                getPlatformDiskCache()
            }
            .build()
    }
}

expect fun getPlatformDiskCache(): DiskCache?