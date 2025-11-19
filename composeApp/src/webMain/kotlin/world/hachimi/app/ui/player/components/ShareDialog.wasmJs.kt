package world.hachimi.app.ui.player.components

import coil3.PlatformContext
import kotlinx.browser.window
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
actual fun share(context: PlatformContext, text: String): Int {
    // Share by using Web Share API
    window.navigator.clipboard.writeText(text)
    return 0
}