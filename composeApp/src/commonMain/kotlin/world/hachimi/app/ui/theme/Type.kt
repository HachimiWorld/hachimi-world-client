package world.hachimi.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

val AppTypography = Typography()

fun buildMdTypo(bodyStyle: TextStyle): Typography {
    return Typography().let {
        Typography(
            displayLarge = it.displayLarge.copy(fontFamily = bodyStyle.fontFamily),
            displayMedium = it.displayMedium.copy(fontFamily = bodyStyle.fontFamily),
            displaySmall = it.displaySmall.copy(fontFamily = bodyStyle.fontFamily),
            headlineLarge = it.headlineLarge.copy(fontFamily = bodyStyle.fontFamily),
            headlineMedium = it.headlineMedium.copy(fontFamily = bodyStyle.fontFamily),
            headlineSmall = it.headlineSmall.copy(fontFamily = bodyStyle.fontFamily),
            titleLarge = it.titleLarge.copy(fontFamily = bodyStyle.fontFamily),
            titleMedium = it.titleMedium.copy(fontFamily = bodyStyle.fontFamily),
            titleSmall = it.titleSmall.copy(fontFamily = bodyStyle.fontFamily),
            bodyLarge = it.bodyLarge.copy(fontFamily = bodyStyle.fontFamily),
            bodyMedium = it.bodyMedium.copy(fontFamily = bodyStyle.fontFamily),
            bodySmall = it.bodySmall.copy(fontFamily = bodyStyle.fontFamily),
            labelLarge = it.labelLarge.copy(fontFamily = bodyStyle.fontFamily),
            labelMedium = it.labelMedium.copy(fontFamily = bodyStyle.fontFamily),
            labelSmall = it.labelSmall.copy(fontFamily = bodyStyle.fontFamily)
        )
    }
}