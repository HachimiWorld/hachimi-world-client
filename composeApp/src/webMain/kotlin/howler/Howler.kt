@file:OptIn(ExperimentalWasmJsInterop::class)

package howler

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.JsBoolean
import kotlin.js.JsModule
import kotlin.js.JsNumber
import kotlin.js.JsString
import kotlin.js.definedExternally
import kotlin.js.js

external interface HowlerGlobal : JsAny {
    val usingWebAudio: JsBoolean
    val noAudio: JsBoolean
    var autoUnlock: JsBoolean
    var html5PoolSize: JsNumber
    var autoSuspend: JsBoolean
    val ctx: JsAny // AudioContext
    val masterGain: JsAny // GainNode

    fun mute(muted: JsBoolean)
    fun volume(volume: JsNumber? = definedExternally): JsNumber
    fun stop()
    fun codecs(ext: JsString): JsBoolean
    fun unload()

    // Spatial plugin methods (if used)
    fun stereo(pan: JsNumber)
    fun pos(x: JsNumber, y: JsNumber, z: JsNumber)
    fun orientation(x: JsNumber, y: JsNumber, z: JsNumber, xUp: JsNumber, yUp: JsNumber, zUp: JsNumber)
}

@JsModule("howler")
external val Howler: JsAny

@JsModule("howler")
external class Howl(options: HowlOptions) : JsAny {
    /**
     * `String` `Number`
     */
    fun play(spriteOrId: JsAny? = definedExternally): JsNumber
    fun pause(id: JsNumber? = definedExternally)
    fun stop(id: JsNumber? = definedExternally)
    fun mute(muted: JsBoolean? = definedExternally, id: JsNumber? = definedExternally)
    fun volume(volume: JsNumber? = definedExternally, id: JsNumber? = definedExternally): JsNumber
    fun fade(from: JsNumber, to: JsNumber, duration: JsNumber, id: JsNumber? = definedExternally)
    fun rate(rate: JsNumber? = definedExternally, id: JsNumber? = definedExternally): JsNumber

    /**
     * Get/set the seek position of a sound. This method can optionally take 0, 1 or 2 arguments.
     * - `seek()` -> Returns the first sound node's current seek position.
     * - `seek(id)` -> Returns the sound id's current seek position.
     * - `seek(seek)` -> Sets the seek position of the first sound node.
     * - `seek(seek, id)` -> Sets the seek position of passed sound id.
     * @return {Howl/Number} Returns self or the current seek position.
     */
    fun seek(seek: Double): Howl

    /**
     * Get/set the position of playback for a sound. This method optionally takes 0, 1 or 2 arguments.
     *
     * - `seek`: Number optional The position to move current playback to (in seconds).
     * - `id`: Number optional The sound ID. If none is passed, the first sound will seek.
     */
    fun seek(id: JsNumber? = definedExternally): JsNumber
    fun loop(loop: JsBoolean? = definedExternally, id: JsNumber? = definedExternally): JsBoolean
    fun playing(id: JsNumber? = definedExternally): JsBoolean

    /**
     * Get the duration of the audio source (in seconds). Will return `0` until after the load event fires.
     */
    fun duration(id: JsNumber? = definedExternally): JsNumber
    fun state(): JsString // 'unloaded', 'loading', 'loaded'
    fun on(event: JsString, fn: (JsAny) -> Unit, id: JsNumber? = definedExternally)
    fun once(event: JsString, fn: (JsAny) -> Unit, id: JsNumber? = definedExternally)
    fun off(event: JsString, fn: ((JsAny) -> Unit)?, id: JsNumber? = definedExternally)
    fun unload()

    // Spatial methods (if plugin enabled)
    fun stereo(pan: JsNumber, id: JsNumber? = definedExternally)
    fun pos(
        x: JsNumber? = definedExternally,
        y: JsNumber? = definedExternally,
        z: JsNumber? = definedExternally,
        id: JsNumber? = definedExternally
    ): JsAny

    fun orientation(x: JsNumber, y: JsNumber, z: JsNumber, id: JsNumber? = definedExternally)
    fun pannerAttr(o: JsAny, id: JsNumber? = definedExternally)
}

@Suppress("UNUSED_PARAMETER")
fun buildHowl(options: HowlOptions): Howl = js("new Howl(options)")

@Suppress("UNUSED_PARAMETER")
fun HowlOptions(
    src: JsArray<JsString>,
    html5: JsBoolean,
    format: JsArray<JsString>,
    onplay: (JsAny?) -> Unit,
    onpause: (JsAny?) -> Unit,
    onend: (JsAny?) -> Unit
): HowlOptions = js("({ src: src, html5: html5, format: format, onplay: onplay, onpause: onpause, onend: onend })")

@JsModule("howler")
external interface HowlOptions : JsAny {
    /**
     * `Array/String` `[]` *`required`*
     *
     * The sources to the track(s) to be loaded for the sound (URLs or base64 data URIs). These should be in order of preference, howler.js will automatically load the first one that is compatible with the current browser. If your files have no extensions, you will need to explicitly specify the extension using the `format` property.
     */
    var src: JsArray<JsString>?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * `Number` `1.0`
     *
     * The volume of the specific track, from `0.0` to `1.0`.
     */
    var volume: Double?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * `false`
     *
     * Set to `true` to force HTML5 Audio. This should be used for large audio files so that you don't have to wait for the full file to be downloaded and decoded before playing.
     */
    var html5: Boolean?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * `Boolean` `false`
     *
     * Set to true to automatically loop the sound forever.
     */
    var loop: Boolean?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * `Boolean|String` `true`
     *
     * Automatically begin downloading the audio file when the `Howl` is defined. If using HTML5 Audio, you can set this to `'metadata'` to only preload the file's metadata (to get its duration without download the entire file, for example).
     */
    var preload: String?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * `Boolean` `false`
     *
     * Set to `true` to automatically start playback when sound is loaded.
     */
    var autoplay: Boolean?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * `Boolean` `false`
     *
     * Set to `true` to load the audio muted.
     */
    var mute: Boolean?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * `Object` `{}`
     *
     * Define a sound sprite for the sound. The offset and duration are defined in milliseconds. A third (optional) parameter is available to set a sprite as looping. An easy way to generate compatible sound sprites is with [audiosprite](https://github.com/tonistiigi/audiosprite).
     * ```javascript
     * new Howl({
     *   sprite: {
     *     key1: [offset, duration, (loop)]
     *   },
     * });
     * ```
     */
    var sprite: JsAny?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * `Number` `1.0`
     *
     * The rate of playback. 0.5 to 4.0, with 1.0 being normal speed.
     */
    var rate: Double?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * `Number` `5`
     *
     * The size of the inactive sounds pool. Once sounds are stopped or finish playing, they are marked as ended and ready for cleanup. We keep a pool of these to recycle for improved performance. Generally this doesn't need to be changed. It is important to keep in mind that when a sound is paused, it won't be removed from the pool and will still be considered active so that it can be resumed later.
     */
    var pool: Int?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * `Array` `[]`
     *
     * howler.js automatically detects your file format from the extension, but you may also specify a format in situations where extraction won't work (such as with a SoundCloud stream).
     */
    var format: JsArray<JsString>?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * `Object` `null`
     *
     * When using Web Audio, howler.js uses an XHR request to load the audio files. If you need to send custom headers, set the HTTP method or enable `withCredentials` ([see reference](https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/withCredentials)), include them with this parameter. Each is optional (method defaults to `GET`, headers default to `null` and withCredentials defaults to `false`). For example:
     * ```javascript
     * // Using each of the properties.
     * new Howl({
     *   xhr: {
     *     method: 'POST',
     *     headers: {
     *       Authorization: 'Bearer:' + token,
     *     },
     *     withCredentials: true,
     *   }
     * });
     *
     * // Only changing the method.
     * new Howl({
     *   xhr: {
     *     method: 'POST',
     *   }
     * });
     * ```
     */
    var xhr: JsAny?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Fires when the sound is loaded.
     */
    var onload: (() -> Unit)?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Fires when the sound is unable to load. The first parameter is the ID of the sound (if it exists) and the second is the error message/code.
     *
     * The load error codes are [defined in the spec](http://dev.w3.org/html5/spec-author-view/spec.html#mediaerror):
     * * **1** - The fetching process for the media resource was aborted by the user agent at the user's request.
     * * **2** - A network error of some description caused the user agent to stop fetching the media resource, after the resource was established to be usable.
     * * **3** - An error of some description occurred while decoding the media resource, after the resource was established to be usable.
     * * **4** - The media resource indicated by the src attribute or assigned media provider object was not suitable.
     */
    var onloaderror: ((JsAny) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Fires when the sound is unable to play. The first parameter is the ID of the sound and the second is the error message/code.
     */
    var onplayerror: ((JsAny) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Fires when the sound begins playing. The first parameter is the ID of the sound.
     */
    var onplay: ((JsAny) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Fires when the sound finishes playing (if it is looping, it'll fire at the end of each loop). The first parameter is the ID of the sound.
     */
    var onend: ((JsAny) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Fires when the sound has been paused. The first parameter is the ID of the sound.
     */
    var onpause: ((JsAny) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Fires when the sound has been stopped. The first parameter is the ID of the sound.
     */
    var onstop: ((JsAny) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Fires when the sound has been muted/unmuted. The first parameter is the ID of the sound.
     */
    var onmute: ((JsAny) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Fires when the sound's volume has changed. The first parameter is the ID of the sound.
     */
    var onvolume: ((JsAny) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Fires when the sound's playback rate has changed. The first parameter is the ID of the sound.
     */
    var onrate: ((JsAny) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Fires when the sound has been seeked. The first parameter is the ID of the sound.
     */
    var onseek: ((JsAny) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Fires when the current sound finishes fading in/out. The first parameter is the ID of the sound.
     */
    var onfade: ((JsAny) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Fires when audio has been automatically unlocked through a touch/click event.
     */
    var onunlock: (() -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
}

/*@Suppress("UNUSED_PARAMETER")
fun HowlerOptions(
    src: JsArray<JsString>?,
    volume: Double? = null,
    html5: Boolean? = null,
    loop: Boolean? = null,
    preload: String? = null,
    autoplay: Boolean? = null,
    mute: Boolean? = null,
    sprite: JsAny? = null,
    rate: Double? = null,
    pool: Int? = null,
    format: JsArray<JsString>? = null,
    xhr: JsAny? = null,
    onload: (() -> Unit)? = null,
    onloaderror: ((JsAny) -> Unit)? = null,
    onplayerror: ((JsAny) -> Unit)? = null,
    onplay: ((JsAny) -> Unit)? = null,
    onend: ((JsAny) -> Unit)? = null,
    onpause: ((JsAny) -> Unit)? = null,
    onstop: ((JsAny) -> Unit)? = null,
    onmute: ((JsAny) -> Unit)? = null,
    onvolume: ((JsAny) -> Unit)? = null,
    onrate: ((JsAny) -> Unit)? = null,
    onseek: ((JsAny) -> Unit)? = null,
    onfade: ((JsAny) -> Unit)? = null,
    onunlock: (() -> Unit)? = null,
) : HowlOptions {
    js("return { src, volume, html5, loop, preload, autoplay, mute, sprite, rate, pool, format, xhr, onload, onloaderror, onplayerror, onplay, onend, onpause, onstop, onmute, onvolume, onrate, onseek, onfade, onunlock };")
}*/