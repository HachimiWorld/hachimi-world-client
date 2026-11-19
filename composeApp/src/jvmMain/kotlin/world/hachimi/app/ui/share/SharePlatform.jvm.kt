package world.hachimi.app.ui.share

import coil3.PlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File

actual fun copyToClipboard(context: PlatformContext, text: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}

actual suspend fun savePosterImage(context: PlatformContext, bytes: ByteArray, fileName: String): Boolean =
    withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(System.getProperty("user.home"), "Pictures/Hachimi World").apply { mkdirs() }
            File(dir, fileName).writeBytes(bytes)
        }.isSuccess
    }
