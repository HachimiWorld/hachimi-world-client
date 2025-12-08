package world.hachimi.app.ui

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

@Stable
expect fun calculateAvgColor(bitmap: coil3.Image): Color

@Stable
fun calculateAvgColor(bitmap: ImageBitmap): Color {
    val buffer = IntArray(bitmap.width * bitmap.height)
    bitmap.readPixels(buffer)

    var totalRed = 0.0
    var totalGreen = 0.0
    var totalBlue = 0.0

    for (pixel in buffer) {
        val color = Color(pixel)
        totalRed += color.red
        totalGreen += color.green
        totalBlue += color.blue
    }

    val pixelCount = bitmap.width * bitmap.height
    val avgColor = Color(
        red = (totalRed / pixelCount).toFloat(),
        green = (totalGreen / pixelCount).toFloat(),
        blue = (totalBlue / pixelCount).toFloat()
    )

    return avgColor
}
