package world.hachimi.app.ui.util

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.CancellationException
import world.hachimi.app.logging.Logger

/** Asks where to put the image with the system's save dialog, then writes it there. @since 261113 */
internal suspend fun saveWithDialog(bytes: ByteArray, fileName: String): SaveImageResult {
    val extension = fileName.substringAfterLast('.', "").ifEmpty { null }
    return try {
        val file = FileKit.openFileSaver(
            suggestedName = fileName.substringBeforeLast('.'),
            defaultExtension = extension,
            allowedExtensions = extension?.let { setOf(it) },
        ) ?: return SaveImageResult.CANCELLED
        file.write(bytes)
        SaveImageResult.SAVED
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Logger.e("save_image", "Failed to save an image", e)
        SaveImageResult.FAILED
    }
}
