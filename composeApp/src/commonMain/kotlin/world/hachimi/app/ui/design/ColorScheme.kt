package world.hachimi.app.ui.design

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

object HachimiPalette {
    val backgroundLight = Color(0xFFEBE9E7)
    val primaryLight = Color(0xFFFF7022)
    val primaryContainerLight = Color(0xFFDA5D1A).copy(0.17f)
    val surfaceLight = Color(0xFFFFFFFF).copy(0.6f)
    val secondaryLight = Color(0xFF94C33E)
    val secondaryContainerLight = Color(0xFF5AA41D).copy(0.17f)
    val onSurfaceLight = Color(0xFF000000).copy(0.87f)
    val onSurfaceVariantLight = Color(0xFF000000).copy(0.60f)
    val onSurfaceReverseLight = Color(0xFFFFFFFF).copy(0.87f)
    val outlineLight = Color(0xFF000000).copy(0.10f)

    val backgroundDark = Color(0xFF2B2A25)
    val primaryDark = Color(0xFFFF782F)
    val primaryContainerDark = Color(0xFFFF9155).copy(0.17f)
    val surfaceDark = Color(0xFFFFFFFF).copy(0.07f)
    val secondaryDark = Color(0xFFA3D151)
    val secondaryContainerDark = Color(0xFF8CCC3E).copy(0.17f)
    val onSurfaceDark = Color(0xFFFFFFFF).copy(0.87f)
    val onSurfaceVariantDark = Color(0xFFFFFFFF).copy(0.60f)
    val onSurfaceReverseDark = Color(0xFF000000).copy(0.87f)
    val outlineDark = Color(0xFFFFFFFF).copy(0.10f)
}


@Immutable
class ColorScheme(
    val background: Color,
    val primary: Color,
    val primaryContainer: Color,
    val surface: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val onSurfaceReverse: Color,
    val outline: Color
)

val hachimiLightScheme = ColorScheme(
    background = HachimiPalette.backgroundLight,
    primary = HachimiPalette.primaryLight,
    primaryContainer = HachimiPalette.primaryContainerLight,
    surface = HachimiPalette.surfaceLight,
    secondary = HachimiPalette.secondaryLight,
    secondaryContainer = HachimiPalette.secondaryContainerLight,
    onSurface = HachimiPalette.onSurfaceLight,
    onSurfaceVariant = HachimiPalette.onSurfaceVariantLight,
    onSurfaceReverse = HachimiPalette.onSurfaceReverseLight,
    outline = HachimiPalette.outlineLight
)

val hachimiDarkScheme = ColorScheme(
    background = HachimiPalette.backgroundDark,
    primary = HachimiPalette.primaryDark,
    primaryContainer = HachimiPalette.primaryContainerDark,
    surface = HachimiPalette.surfaceDark,
    secondary = HachimiPalette.secondaryDark,
    secondaryContainer = HachimiPalette.secondaryContainerDark,
    onSurface = HachimiPalette.onSurfaceDark,
    onSurfaceVariant = HachimiPalette.onSurfaceVariantDark,
    onSurfaceReverse = HachimiPalette.onSurfaceReverseDark,
    outline = HachimiPalette.outlineDark
)