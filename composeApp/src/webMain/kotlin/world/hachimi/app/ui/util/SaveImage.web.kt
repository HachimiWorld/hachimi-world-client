package world.hachimi.app.ui.util

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download
import kotlinx.coroutines.CancellationException

actual suspend fun saveImage(bytes: ByteArray, fileName: String): SaveImageResult = try {
    FileKit.download(bytes, fileName)
    SaveImageResult.DOWNLOADING
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    SaveImageResult.FAILED
}
