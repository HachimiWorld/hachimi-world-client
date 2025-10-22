package world.hachimi.app

import io.github.vinceglb.filekit.PlatformFile
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIDevice
import platform.UIKit.UIApplication
import world.hachimi.app.logging.Logger

class IOSPlatform : Platform {
    override val name: String = "ios"
    override val platformVersion: String = UIDevice.currentDevice.systemVersion
    override val variant: String = "${BuildKonfig.BUILD_TYPE}-ios"

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