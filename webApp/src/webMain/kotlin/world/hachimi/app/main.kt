package world.hachimi.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.github.terrakok.navigation3.browser.HierarchicalBrowserNavigation
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.context.startKoin
import world.hachimi.app.di.appModule
import world.hachimi.app.font.WithFont
import world.hachimi.app.i18n.AppEnvironment
import world.hachimi.app.logging.Logger
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.Navigator
import world.hachimi.app.nav.Route
import world.hachimi.app.nav.decodeFromBrowserPathString
import world.hachimi.app.nav.encodeToBrowserPath
import world.hachimi.app.player.WebPlayerHelper
import world.hachimi.app.ui.App
import world.hachimi.app.util.parseJmid
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(
    ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class,
    DelicateCoroutinesApi::class,
    ExperimentalSerializationApi::class
)
fun main() {
    val startRoute = getStartDestination()
    val playIntent = getPlayIntent()
    val koin = startKoin {
        modules(appModule)
    }

    val global = koin.koin.get<GlobalStore>()
    val webPlayerHelper = koin.koin.get<WebPlayerHelper>()
    val navigator = Navigator(startRoute)

    window.addEventListener("popstate") {
        if (global.playerExpanded) {
            it.preventDefault()
            global.shrinkPlayer()
        }
    }

    ComposeViewport {
        HierarchicalBrowserNavigation(
            currentDestination = remember {
                derivedStateOf { navigator.backStack.lastOrNull() }
            },
            currentDestinationName = { key ->
                when (key) {
                    is Route -> encodeToBrowserPath(key).let {
                        "#" + it.encodeToString()
                    }
                    else -> null
                }
            }
        )

        LaunchedEffect(Unit) {
            document.querySelector("#loading")?.remove()
            global.initialize().join()
            webPlayerHelper.initialize()

            playIntent?.let { jmid ->
                global.player.insertToQueueWithFetch(jmid, true, false)
                global.expandPlayer()
            }
        }

        WithFont(global) {
            // Apply locale environment so the app follows the selected locale
            AppEnvironment(global.settings.locale) {
                App(global, navigator)
            }
        }
    }
}


private fun getPlayIntent(): String? {
    // https://hachimi.world/app/#/song/jm-abcd-123
    val hash = window.location.hash
    if (hash.startsWith("#/song/")) {
        val segment = hash.substringAfter("#/song/")
        val (prefix, number) = parseJmid(segment.uppercase()) ?: return null
        return "JM-$prefix-$number"
    }
    return null
}

private fun getStartDestination(): Route {
    val hash = window.location.hash
    if (hash.isNotEmpty()) {
        val route = try {
            decodeFromBrowserPathString(hash.trimStart('#'))
        } catch (e: Exception) {
            Logger.w("main", "Failed to parse route from url hash: $hash, fallback to default route", e)
            null
        }
        return route ?: Route.Root.Default
    } else {
        return Route.Root.Default
    }
}