package world.hachimi.app.ui.insets

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import world.hachimi.app.ui.window.WindowsCaptionBarHeight

val MacOSCaptionBarHeight: Dp = 28.dp

actual val WindowInsets.Companion.multiplatformCaptionBar: WindowInsets
    get() = when (hostOs) {
        OS.Windows -> WindowInsets(top = WindowsCaptionBarHeight)
        OS.MacOS -> WindowInsets(top = MacOSCaptionBarHeight)
        else -> WindowInsets()
    }
