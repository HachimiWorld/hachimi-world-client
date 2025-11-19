@file:OptIn(ExperimentalJsExport::class, ExperimentalWasmJsInterop::class)

package world.hachimi.app.player

import world.hachimi.app.model.PlayerService
import kotlin.js.ExperimentalJsExport
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.definedExternally
import kotlin.js.js
import kotlin.js.toJsArray

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/MediaMetadata/MediaMetadata
 */
external class MediaMetadata : JsAny {
    constructor()
    constructor(init: MediaMetadataInit)

    var album: String
        get() = definedExternally
        set(value) = definedExternally
    var artist: String
        get() = definedExternally
        set(value) = definedExternally
    var artwork: JsArray<MediaImage>
        get() = definedExternally
        set(value) = definedExternally
    var title: String
        get() = definedExternally
        set(value) = definedExternally
}

external interface MediaMetadataInit : JsAny {
    var album: String
    var artist: String
    var artwork: JsArray<MediaImage>
    var title: String
}

@Suppress("UNUSED_PARAMETER")
fun MediaMetadataInit(
    artist: String = "",
    album: String = "",
    artwork: JsArray<MediaImage> = emptyArray<MediaImage>().toJsArray(),
    title: String = "",
): MediaMetadataInit = js(
    """({
    title: title,
    artist: artist,
    album: album,
    artwork: artwork
})"""
)

external interface MediaImage : JsAny {
    var src: String
    var sizes: String
    var type: String
}

@Suppress("UNUSED_PARAMETER")
fun MediaImage(
    src: String,
    sizes: String = "",
    type: String = ""
): MediaImage = js(
    """({
    src: src,
    sizes: sizes,
    type: type
})"""
)

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/Media_Session_API
 */
external interface MediaSession : JsAny {
    var metadata: MediaMetadata
    fun setActionHandler(action: String, handler: () -> Unit)
}

external interface ChapterInfo : JsAny {

}

external interface Navigator : JsAny {
    val mediaSession: MediaSession?
}

external val navigator: Navigator

class WasmPlayerHelper(
    private val playerService: PlayerService
) {
    fun initialize() {
        navigator.mediaSession?.let { mediaSession ->
            mediaSession.setActionHandler("play") {
                playerService.playOrPause()
            }
            mediaSession.setActionHandler("pause") {
                // Damn. I should separate it to play()/pause()
                playerService.playOrPause()
            }
            mediaSession.setActionHandler("stop") {
                playerService.playOrPause()
            }
            mediaSession.setActionHandler("previoustrack") {
                playerService.previous()
            }
            mediaSession.setActionHandler("nexttrack") {
                playerService.next()
            }
        }
    }
}

