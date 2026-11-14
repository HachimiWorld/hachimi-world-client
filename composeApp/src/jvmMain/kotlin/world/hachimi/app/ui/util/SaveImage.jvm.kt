package world.hachimi.app.ui.util

actual suspend fun saveImage(bytes: ByteArray, fileName: String): SaveImageResult = saveWithDialog(bytes, fileName)
