package world.hachimi.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.bodyTextStyle
import world.hachimi.app.ui.design.hachimiDarkScheme
import world.hachimi.app.ui.design.hachimiLightScheme

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

val LocalDarkMode = compositionLocalOf { false }

/**
 * @param typography Default TextStyle for default Text component.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    typography: TextStyle = bodyTextStyle,
    content: @Composable () -> Unit
) {
    val hachimiColorScheme = if (darkTheme) hachimiDarkScheme else hachimiLightScheme
    val mdColorScheme = when {
        darkTheme -> darkScheme
        else -> lightScheme
    }
    CompositionLocalProvider(LocalDarkMode provides darkTheme) {
        MaterialTheme(
            colorScheme = mdColorScheme,
            typography = buildMdTypo(typography),
        ) {
            HachimiTheme(
                colorScheme = hachimiColorScheme,
                typography = typography
            ) {
                SystemAppearance(darkTheme)
                content()
            }
        }
    }
}


@Composable
fun PreviewTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    background: Boolean,
    content: @Composable () -> Unit
) {
    val hachimiColorScheme = if (darkTheme) hachimiDarkScheme else hachimiLightScheme
    val mdColorScheme = when {
        darkTheme -> darkScheme
        else -> lightScheme
    }
    CompositionLocalProvider(LocalDarkMode provides darkTheme) {
        MaterialTheme(
            colorScheme = mdColorScheme,
            typography = buildMdTypo(bodyTextStyle),
        ) {
            HachimiTheme(
                colorScheme = hachimiColorScheme
            ) {
                if (background) {
                    Surface(color = HachimiTheme.colorScheme.background, content = content)
                } else {
                    content()
                }
            }
        }
    }
}

@Composable
internal fun SystemAppearance(darkTheme: Boolean) {
    val uiController = rememberSystemUIController()
    LaunchedEffect(uiController, darkTheme) {
        uiController.setSystemBarsTheme(darkTheme)
    }
}

interface SystemUIController {
    /**
     * @param darkTheme Dark theme means dark background with light ui
     */
    fun setSystemBarsTheme(darkTheme: Boolean)
}

@Composable
expect fun rememberSystemUIController(): SystemUIController

internal val LocalSystemUIController = staticCompositionLocalOf { error("Not initialized") }