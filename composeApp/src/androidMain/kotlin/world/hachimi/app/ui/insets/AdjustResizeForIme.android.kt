package world.hachimi.app.ui.insets

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
actual fun AdjustResizeForIme() {
    val window = LocalActivity.current?.window
    DisposableEffect(window) {
        val previous = window?.attributes?.softInputMode
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        onDispose {
            if (previous != null) window.setSoftInputMode(previous)
        }
    }
}
