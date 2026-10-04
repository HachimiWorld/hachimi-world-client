package world.hachimi.app.ui.insets

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Modifier.multiplatformSafeDrawingPadding(): Modifier = this.windowInsetsPadding(
    WindowInsets.multiplatformSafeDrawing
)

@Composable
fun Modifier.multiplatformStatusBarsPadding(): Modifier = this.windowInsetsPadding(
    WindowInsets.multiplatformStatusBars
)

@Composable
fun Modifier.multiplatformSystemBarsPadding(): Modifier = this.windowInsetsPadding(
    WindowInsets.multiplatformSystemBars
)