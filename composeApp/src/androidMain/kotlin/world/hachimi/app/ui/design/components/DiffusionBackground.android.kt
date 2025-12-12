package world.hachimi.app.ui.design.components

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter

actual fun isDiffusionBackgroundSupported() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
actual fun FallbackDiffusionBackground(
    modifier: Modifier,
    painter: Painter
) {
    // ... maybe we do not need this
    /*val painterLoader = rememberPainterLoader()
    val paletteState = rememberPaletteState(painterLoader)
    LaunchedEffect(painter) {
        paletteState.generate(painter)
    }*/
}