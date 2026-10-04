package world.hachimi.app.ui.player.components

import coil3.PlatformContext
import web.navigator.navigator
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
actual fun share(context: PlatformContext, text: String): Int {
    // Share by using Web Share API
    navigator.clipboard.writeTextAsync(text)
    return 0
}