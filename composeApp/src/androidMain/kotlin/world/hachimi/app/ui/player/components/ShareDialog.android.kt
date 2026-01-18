package world.hachimi.app.ui.player.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import coil3.PlatformContext
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.share_share_music_title
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString

@OptIn(DelicateCoroutinesApi::class)
actual fun share(context: PlatformContext, text: String): Int {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(text, text)
    clipboard.setPrimaryClip(clip)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }

    GlobalScope.launch {
        val title = getString(Res.string.share_share_music_title)
        withContext(Dispatchers.Main) {
            context.startActivity(Intent.createChooser(intent, title))
        }
    }
    return 1
}