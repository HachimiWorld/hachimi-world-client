package world.hachimi.app.ui.util

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.saveImageToGallery

actual suspend fun saveImage(bytes: ByteArray, fileName: String): SaveImageResult =
    FileKit.saveImageToGallery(bytes, fileName).fold({ SaveImageResult.SAVED }, { SaveImageResult.FAILED })
