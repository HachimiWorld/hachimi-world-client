package world.hachimi.app.ui

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeImageBitmap
import coil3.Image
import coil3.toBitmap

@Stable
actual fun calculateAvgColor(bitmap: Image): Color {
    val composeImageBitmap = bitmap.toBitmap().asComposeImageBitmap()
    return calculateAvgColor(composeImageBitmap)
}