package world.hachimi.app.ui.insets

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class SafeAreaInsets(
    val top: Dp = 0.dp,
    val bottom: Dp = 0.dp,
    val start: Dp = 0.dp,
    val end: Dp = 0.dp
)

@Composable
fun currentSafeAreaInsets(): SafeAreaInsets {
    return when (remember { getCurrentPlatform() }) {
        Platform.MacOS -> SafeAreaInsets(top = 28.dp)
        Platform.Windows -> SafeAreaInsets(top = 32.dp)
        Platform.Linux -> SafeAreaInsets()
        Platform.Android, Platform.iOS -> {
            val density = LocalDensity.current
            val insets = WindowInsets.safeDrawing
            val direction = LocalLayoutDirection.current
            SafeAreaInsets(
                top = with(density) { insets.getTop(density).toDp() },
                bottom = with(density) { insets.getBottom(density).toDp() },
                start = with(density) { insets.getLeft(density, direction).toDp() },
                end = with(density) { insets.getRight(density, direction).toDp() }
            )
        }
        else -> SafeAreaInsets()
    }
}

@Composable
fun Modifier.safeAreaPadding(): Modifier = this.then(
    Modifier.padding(
        start = currentSafeAreaInsets().start,
        top = currentSafeAreaInsets().top,
        end = currentSafeAreaInsets().end,
        bottom = currentSafeAreaInsets().bottom
    )
)

enum class Platform {
    MacOS, Windows, Linux, Android, Web, iOS, Unknown
}

expect fun getCurrentPlatform(): Platform