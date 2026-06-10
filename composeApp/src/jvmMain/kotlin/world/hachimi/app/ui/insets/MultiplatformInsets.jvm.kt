package world.hachimi.app.ui.insets

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

actual fun getCurrentPlatform(): Platform = when (hostOs) {
    OS.Android -> Platform.Android
    OS.Linux -> Platform.Linux
    OS.Windows -> Platform.Windows
    OS.MacOS -> Platform.MacOS
    else -> Platform.Unknown
}