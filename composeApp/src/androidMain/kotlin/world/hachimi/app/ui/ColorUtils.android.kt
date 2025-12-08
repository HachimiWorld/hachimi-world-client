package world.hachimi.app.ui

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import coil3.Image
import coil3.toBitmap

@Stable
actual fun calculateAvgColor(bitmap: Image): Color {
    val composeImageBitmap = bitmap.toBitmap().asImageBitmap()
    return calculateAvgColor(composeImageBitmap)
}