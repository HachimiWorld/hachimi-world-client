package world.hachimi.app.ui.share

import coil3.PlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO

actual fun copyToClipboard(context: PlatformContext, text: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}

actual suspend fun savePosterImage(context: PlatformContext, bytes: ByteArray, fileName: String): Boolean =
    withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(System.getProperty("user.home"), "Pictures/Hachimi World").apply { mkdirs() }
            val file = File(dir, fileName).apply { writeBytes(bytes) }
            // Says where it went, rather than only a toast
            reveal(file)
        }.isSuccess
    }

actual val posterClipboardSupported: Boolean = true

/** Both flavours, as some apps take the picture and others the PNG itself. */
private class PosterTransferable(private val image: Image, private val png: ByteArray) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor, PngFlavor)

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean =
        flavor == DataFlavor.imageFlavor || flavor == PngFlavor

    override fun getTransferData(flavor: DataFlavor): Any = when {
        flavor == DataFlavor.imageFlavor -> image
        flavor == PngFlavor -> ByteArrayInputStream(png)
        else -> throw UnsupportedFlavorException(flavor)
    }

    companion object {
        /** A mime flavour like this carries a stream, which is what clipboard consumers expect. */
        private val PngFlavor = DataFlavor("image/png", "PNG image")
    }
}

actual fun copyPosterToClipboard(context: PlatformContext, bytes: ByteArray): Boolean = runCatching {
    val image = ImageIO.read(ByteArrayInputStream(bytes)) ?: return false
    Toolkit.getDefaultToolkit().systemClipboard.setContents(PosterTransferable(image, bytes), null)
    true
}.getOrDefault(false)

private fun reveal(file: File): Boolean {
    val os = System.getProperty("os.name").lowercase()
    val command = when {
        os.contains("mac") -> listOf("open", "-R", file.absolutePath)
        os.contains("win") -> listOf("explorer", "/select,${file.absolutePath}")
        else -> listOf("xdg-open", file.parentFile?.absolutePath ?: file.absolutePath)
    }
    return runCatching { ProcessBuilder(command).start() }.isSuccess
}
