package world.hachimi.app.ui.player.components

import coil3.PlatformContext

actual fun share(context: PlatformContext, text: String): Int {
    java.awt.Toolkit.getDefaultToolkit().systemClipboard.setContents(
        java.awt.datatransfer.StringSelection(text),
        null
    )
    return 0
}