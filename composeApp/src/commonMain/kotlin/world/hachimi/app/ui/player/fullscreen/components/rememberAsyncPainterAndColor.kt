package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.asPainter
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.hachimi.app.ui.calculateAvgColor

@Composable
fun rememberAsyncPainterAndColor(
    model: String?,
    context: PlatformContext = LocalPlatformContext.current
): Pair<Painter?, Color> {
    var painter by remember { mutableStateOf<Painter?>(null) }
    var dominantColor by remember { mutableStateOf(Color.Gray) }

    LaunchedEffect(model) {
        val request = ImageRequest.Builder(context)
            .data(model)
            .size(Size.ORIGINAL)
            .build()

        val result = SingletonImageLoader.get(context).execute(request)
        when (result) {
            is ErrorResult -> {
                painter = ColorPainter(Color.Gray)
                dominantColor = Color.Gray
            }

            is SuccessResult -> {
                val image = result.image
                val avgColor = withContext(Dispatchers.Default) { calculateAvgColor(image) }
                println("AVG Color: $avgColor, Luminance: ${avgColor.luminance()}")
                painter = image.asPainter(context, FilterQuality.None)
                dominantColor = avgColor
            }
        }
    }
    return painter to dominantColor
}