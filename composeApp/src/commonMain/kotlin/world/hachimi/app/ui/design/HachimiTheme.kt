package world.hachimi.app.ui.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalColorScheme = staticCompositionLocalOf<ColorScheme> { hachimiLightScheme }

object HachimiTheme {
    val colorScheme: ColorScheme
        @Composable @ReadOnlyComposable get() = LocalColorScheme.current
}

@Composable
fun HachimiTheme(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalColorScheme provides colorScheme) {
        content()
    }
}