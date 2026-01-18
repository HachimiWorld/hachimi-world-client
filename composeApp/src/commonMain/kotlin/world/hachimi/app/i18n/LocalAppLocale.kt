package world.hachimi.app.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.key

expect object LocalAppLocale {
    val current: String @Composable get
    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}

@Composable
fun AppEnvironment(
    locale: String?,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppLocale provides locale,
    ) {
        key(locale) {
            content()
        }
    }
}