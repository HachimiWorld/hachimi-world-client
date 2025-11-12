package world.hachimi.app

import io.github.vinceglb.filekit.PlatformFile
import platform.Foundation.*
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String = "ios"
    override val platformVersion: String = UIDevice.currentDevice.systemVersion
    override val variant: String = "${BuildKonfig.BUILD_TYPE}-ios"
    override val userAgent: String = "HachimiWorld-ios/${BuildKonfig.VERSION_NAME} (${UIDevice.currentDevice.model}; ${UIDevice.currentDevice.systemName})"

    override fun getCacheDir(): PlatformFile {
        val paths = NSFileManager.defaultManager.URLsForDirectory(
            NSCachesDirectory,
            NSUserDomainMask
        )
        val cachePath = (paths.firstOrNull() as? NSURL)?.path ?: error("Could not get cache directory")
        return PlatformFile(cachePath)
    }

    override fun getDataDir(): PlatformFile {
        val paths = NSFileManager.defaultManager.URLsForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask
        )
        val cachePath = (paths.firstOrNull() as? NSURL)?.path ?: error("Could not get cache directory")
        return PlatformFile(cachePath)
    }

    @Suppress("UNCHECKED_CAST")
    override fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(url = nsUrl, options = emptyMap<Any, Any>() as Map<Any?, *>, completionHandler =  null)
    }
}

actual fun getPlatform(): Platform = IOSPlatform()