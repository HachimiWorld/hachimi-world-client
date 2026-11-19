package world.hachimi.app.ui.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import coil3.PlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

actual fun copyToClipboard(context: PlatformContext, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(text, text))
}

actual fun encodePng(image: ImageBitmap): ByteArray? {
    val out = ByteArrayOutputStream()
    return if (image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)) out.toByteArray() else null
}

actual suspend fun savePosterImage(context: PlatformContext, bytes: ByteArray, fileName: String): Boolean =
    withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Hachimi World")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext false
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return@withContext false
                true
            } else {
                // Before scoped storage the gallery needs a permission, so this keeps to the app's own pictures
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return@withContext false
                File(dir, fileName).writeBytes(bytes)
                true
            }
        } catch (e: Throwable) {
            false
        }
    }

actual val posterClipboardSupported: Boolean = false

actual fun copyPosterToClipboard(context: PlatformContext, bytes: ByteArray): Boolean = false

actual val systemShareSupported: Boolean = true
