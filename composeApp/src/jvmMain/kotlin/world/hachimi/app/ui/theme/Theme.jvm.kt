package world.hachimi.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.skiko.hostOs
import world.hachimi.app.BuildKonfig
import world.hachimi.app.LocalWindow
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger
import world.hachimi.app.ui.design.HachimiPalette
import world.hachimi.app.ui.design.bodyTextStyle
import world.hachimi.app.ui.window.LocalWindowFrameState
import world.hachimi.app.ui.window.WindowFrameState
import java.awt.Color
import kotlin.io.path.createTempFile
import kotlin.io.path.moveTo
import kotlin.io.path.writeBytes

@Composable
fun JvmTheme(
    darkMode: Boolean,
    content: @Composable () -> Unit
) {
    val fontState = remember { FontState }
    LaunchedEffect(fontState) {
        fontState.mounted()
    }

    AppTheme(
        darkTheme = darkMode,
        typography = bodyTextStyle.copy(fontFamily = fontState.currentFontFamily),
        content = content
    )

    /*Button(onClick = {
        fontState.switchToDefault()
    }) {
        Text("Toggle Default: ${fontState.default}")
    }*/
}

object FontState {
    private val fullFontUrl = BuildKonfig.ASSETS_BASE_URL + "/fonts/HarmonyOS_Sans_SC_Regular.ttf"
    var currentFontFamily by mutableStateOf<FontFamily>(getPreferredFontFamily())
    var default by mutableStateOf(false)

    fun mounted() {
        // Do nothing haha
    }

    fun switchToDefault() {
        if (default) {
            currentFontFamily = FontFamily.Default
        } else {
            currentFontFamily = getPreferredFontFamily()
        }
        default = !default
    }

    @OptIn(ExperimentalTextApi::class)
    private fun getPreferredFontFamily(): FontFamily {
        return if (hostOs.isWindows) {
            val fontFile = getFontFile()
            if (fontFile.exists()) {
                Logger.d("font", "Load font from disk")
                FontFamily(Font(fontFile))
            } else {
                downloadFontAsync()
                FontFamily.Default
            }
        } else {
            FontFamily.Default
        }
    }

    private fun getFontFile() = getPlatform().getDataDir().file
        .resolve("fonts").also { it.mkdirs() }
        .resolve("HarmonyOS_Sans_SC_Regular.ttf")

    @OptIn(DelicateCoroutinesApi::class)
    private fun downloadFontAsync() {
        Logger.d("font", "Downloading font from web")

        val client = HttpClient {
            install(HttpTimeout) {
                connectTimeoutMillis = 10_000
                requestTimeoutMillis = 60_000
                socketTimeoutMillis = 60_000
            }
        }

        GlobalScope.launch {
            try {
                val resp = client.get(fullFontUrl)
                val bytes = resp.bodyAsBytes()
                val temp = createTempFile()
                temp.writeBytes(bytes)
                temp.moveTo(getFontFile().toPath())
                currentFontFamily = getPreferredFontFamily()
            } catch (e: Throwable) {
                Logger.e("font", "Failed to download font from web", e)
            }
        }
    }
}

class JvmSystemUIController(
    private val window: ComposeWindow,
    private val windowFrameState: WindowFrameState?
): SystemUIController {
    override fun setSystemBarsTheme(darkTheme: Boolean) {
        // FIXME: Not work on Windows
        val bg = if (darkTheme) HachimiPalette.backgroundDark else HachimiPalette.backgroundLight
        window.background = Color(bg.red, bg.green, bg.blue, bg.alpha)
        windowFrameState?.darkMode = darkTheme
    }
}

@Composable
actual fun rememberSystemUIController(): SystemUIController {
    val window = LocalWindow.current
    val windowFrame = LocalWindowFrameState.current
    return remember(window) { JvmSystemUIController(window, windowFrame) }
}