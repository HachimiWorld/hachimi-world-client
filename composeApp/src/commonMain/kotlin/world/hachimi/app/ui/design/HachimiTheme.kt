package world.hachimi.app.ui.design

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
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
    val selectionColors = rememberTextSelectionColors(colorScheme)
    CompositionLocalProvider(
        LocalColorScheme provides colorScheme,
        LocalTextSelectionColors provides selectionColors
    ) {
        content()
    }
}

@Composable
fun rememberTextSelectionColors(colorScheme: ColorScheme): TextSelectionColors {
    val primaryColor = colorScheme.primary
    return remember(primaryColor) {
        TextSelectionColors(
            handleColor = primaryColor,
            backgroundColor = primaryColor.copy(alpha = TextSelectionBackgroundOpacity),
        )
    }
}

private const val TextSelectionBackgroundOpacity = 0.4f