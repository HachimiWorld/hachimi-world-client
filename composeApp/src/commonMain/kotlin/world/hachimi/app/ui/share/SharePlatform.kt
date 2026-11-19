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
