package world.hachimi.app

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import world.hachimi.app.di.appModule
import world.hachimi.app.font.WithFont
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.player.WasmPlayerHelper
import world.hachimi.app.ui.App
import world.hachimi.app.util.parseJmid
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.toJsString

@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
fun main() {
    val playIntent = getPlayIntent()
    val koin = startKoin {
        modules(appModule)
    }

    val global = koin.koin.get<GlobalStore>()
    val wasmPlayerHelper = koin.koin.get<WasmPlayerHelper>()

    GlobalScope.launch {
        global.initialize().join()

        wasmPlayerHelper.initialize()

        playIntent?.let { jmid ->
            global.player.insertToQueueWithFetch(jmid, true, false)
            global.expandPlayer()
        }
    }

    window.addEventListener("popstate") {
        it.preventDefault()
        if (global.playerExpanded) {
            global.shrinkPlayer()
        } else {
            global.nav.back()
        }
    }

    window.history.pushState(null, "", "#/")

    val previousBackStack = mutableStateOf(global.nav.backStack.toList())
    val nav = global.nav
    ComposeViewport {
        LaunchedEffect(Unit) {
            document.querySelector("#loading")?.remove()
        }
        LaunchedEffect(Unit) {
            snapshotFlow { nav.backStack.toList() }
                .collect { newBackStack ->
                    val lastEntry = newBackStack.last()
                    if (previousBackStack.value.size < newBackStack.size) {
                        window.history.pushState(lastEntry.toString().toJsString(), "", "#/${lastEntry}")
                    } else if (previousBackStack.value.size == newBackStack.size) {
                        window.history.replaceState(lastEntry.toString().toJsString(), "", "#/${lastEntry}")
                    } else if (previousBackStack.value.size > newBackStack.size) {

                    }
                    previousBackStack.value = newBackStack
                }
        }
        WithFont {
            if (global.initialized) {
                App()
            } else {
                // TODO: Add splash screen
                Box {}
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