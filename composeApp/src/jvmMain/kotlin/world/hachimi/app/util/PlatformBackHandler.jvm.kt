package world.hachimi.app.util

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Do nothing.
    // May be we can add a global shortcut key?
}