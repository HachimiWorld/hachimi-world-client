package world.hachimi.app.ui.share

import androidx.compose.ui.graphics.ImageBitmap
import coil3.PlatformContext

/** Puts plain text on the system clipboard. @since 261119 */
expect fun copyToClipboard(context: PlatformContext, text: String)

/** Encodes a rendered image as PNG. @since 261119 */
expect fun encodePng(image: ImageBitmap): ByteArray?

/**
 * Saves a PNG where the platform keeps pictures: the photo library on Android and iOS, the Pictures folder on
 * desktop, a download in a browser. Returns whether it was saved.
 *
 * @since 261119
 */
expect suspend fun savePosterImage(context: PlatformContext, bytes: ByteArray, fileName: String): Boolean

/** Whether a picture can be put on the clipboard here. @since 261119 */
expect val posterClipboardSupported: Boolean

/** Puts a PNG on the clipboard. Returns whether it got there. @since 261119 */
expect fun copyPosterToClipboard(context: PlatformContext, bytes: ByteArray): Boolean

/**
 * Whether [share] reaches a system share sheet. Where it doesn't, the share sheet leaves that option out and
 * keeps to copying.
 *
 * @since 261119
 */
expect val systemShareSupported: Boolean
