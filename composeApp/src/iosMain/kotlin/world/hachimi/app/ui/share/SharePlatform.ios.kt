package world.hachimi.app.ui.share

import coil3.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import platform.UIKit.UIPasteboard

actual fun copyToClipboard(context: PlatformContext, text: String) {
    UIPasteboard.generalPasteboard().string = text
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun savePosterImage(context: PlatformContext, bytes: ByteArray, fileName: String): Boolean {
    if (bytes.isEmpty()) return false
    val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
    val image = UIImage.imageWithData(data) ?: return false
    UIImageWriteToSavedPhotosAlbum(image, null, null, null)
    return true
}

actual val posterClipboardSupported: Boolean = true

@OptIn(ExperimentalForeignApi::class)
actual fun copyPosterToClipboard(context: PlatformContext, bytes: ByteArray): Boolean {
    if (bytes.isEmpty()) return false
    val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
    val image = UIImage.imageWithData(data) ?: return false
    UIPasteboard.generalPasteboard().image = image
    return true
}

actual val systemShareSupported: Boolean = false
