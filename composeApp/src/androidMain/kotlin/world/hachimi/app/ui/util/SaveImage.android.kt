package world.hachimi.app.ui.util

import android.os.Build
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.saveImageToGallery

actual suspend fun saveImage(bytes: ByteArray, fileName: String): SaveImageResult =
    // Before Android 10 the gallery needs a storage permission; the system's save dialog doesn't
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        FileKit.saveImageToGallery(bytes, fileName).fold({ SaveImageResult.SAVED }, { SaveImageResult.FAILED })
    } else {
        saveWithDialog(bytes, fileName)
    }
