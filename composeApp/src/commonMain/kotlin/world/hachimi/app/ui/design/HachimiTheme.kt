package world.hachimi.app.ui.design

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.components.LocalDefaultFontFamily
import world.hachimi.app.ui.design.components.LocalTextStyle

val LocalColorScheme = staticCompositionLocalOf<ColorScheme> { hachimiLightScheme }

object HachimiTheme {
    val colorScheme: ColorScheme
        @Composable @ReadOnlyComposable get() = LocalColorScheme.current
}

val bodyTextStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = TextUnit.Unspecified // TODO
)

@Composable
fun HachimiTheme(
    colorScheme: ColorScheme,
    typography: TextStyle = bodyTextStyle,
    content: @Composable () -> Unit
) {
    val selectionColors = rememberTextSelectionColors(colorScheme)
    CompositionLocalProvider(
        LocalColorScheme provides colorScheme,
        LocalTextSelectionColors provides selectionColors,
        LocalTextStyle provides typography,
        LocalContentColor provides colorScheme.onSurface,
        LocalDefaultFontFamily provides (typography.fontFamily ?: FontFamily.Default)
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