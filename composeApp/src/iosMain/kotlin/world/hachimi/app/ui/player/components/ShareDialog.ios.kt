package world.hachimi.app.ui.player.components

import coil3.PlatformContext
import platform.UIKit.UIPasteboard

actual fun share(context: PlatformContext, text: String): Int {
    UIPasteboard.generalPasteboard().string = text
    return 0
}