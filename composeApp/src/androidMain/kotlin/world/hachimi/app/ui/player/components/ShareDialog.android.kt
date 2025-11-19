package world.hachimi.app.ui.player.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import coil3.PlatformContext

actual fun share(context: PlatformContext, text: String): Int {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(text, text)
    clipboard.setPrimaryClip(clip)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "分享音乐"))
    return 1
}