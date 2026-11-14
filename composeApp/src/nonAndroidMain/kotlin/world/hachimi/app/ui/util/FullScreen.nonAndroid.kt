package world.hachimi.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

actual fun fullScreenDialogProperties(): DialogProperties =
    DialogProperties(usePlatformDefaultWidth = false, usePlatformInsets = false)

@Composable
actual fun HideSystemBars() {
}
