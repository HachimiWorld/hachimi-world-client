@file:OptIn(ExperimentalWasmJsInterop::class)

package howler

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.JsBoolean
import kotlin.js.JsString
import kotlin.js.js

fun buildHowl(options: HowlOptions): Howl = Howl(options)

@Suppress("UNUSED_PARAMETER")
fun HowlOptions(
    src: JsArray<JsString>,
    html5: JsBoolean,
    format: JsArray<JsString>,
    onplay: (JsAny?) -> Unit,
    onpause: (JsAny?) -> Unit,
    onend: (JsAny?) -> Unit
): HowlOptions = js("({ src: src, html5: html5, format: format, onplay: onplay, onpause: onpause, onend: onend })")
