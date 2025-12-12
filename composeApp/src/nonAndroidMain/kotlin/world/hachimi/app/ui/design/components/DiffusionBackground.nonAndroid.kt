package world.hachimi.app.ui.design.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter

actual fun isDiffusionBackgroundSupported(): Boolean = true

@Composable
actual fun FallbackDiffusionBackground(
    modifier: Modifier,
    painter: Painter
) {
    // Nothing
}