package world.hachimi.app.player

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerRateDidChangeNotification
import platform.AVFoundation.AVPlayerTimeControlStatusPaused
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.CMTimeRangeValue
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.loadedTimeRanges
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.timeControlStatus
import platform.AVFoundation.volume
import platform.CoreMedia.CMTimeCompare
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSValue
import platform.Foundation.dataTaskWithURL
import platform.Foundation.dataWithBytes
import platform.Foundation.temporaryDirectory
import platform.MediaPlayer.MPMediaItemArtwork
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyArtwork
import platform.MediaPlayer.MPMediaItemPropertyPlaybackDuration
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.UIKit.UIImage
import world.hachimi.app.logging.Logger
import world.hachimi.app.player.Player.Companion.mixVolume

@OptIn(ExperimentalForeignApi::class)
class IosPlayer : Player {
    private var session: AVAudioSession? = null
    private var player: AVPlayer? = null
    private var isPlayerReady = false
    private val listeners = mutableSetOf<Player.Listener>()
    private var timeObserverToken: Any? = null
    val nowPlayingCenter = MPNowPlayingInfoCenter.defaultCenter()
    private var replayGainDb: Float = 0f
    private var userVolume = 1f
    private var replayGainEnabled: Boolean = true
    private var currentSongId: String? = null

    override val supportRemotePlay: Boolean
        get() = true

    override suspend fun isPlaying(): Boolean {
        return player?.timeControlStatus == AVPlayerTimeControlStatusPlaying
    }

    override suspend fun isEnd(): Boolean {
        return player?.let {
            val durationSeconds = player?.currentItem?.duration
            val current = player?.currentItem?.currentTime()
            if (durationSeconds != null && current != null) {
                CMTimeCompare(current, durationSeconds) >= 0
            } else {
                false
            }
        } ?: false
    }

    override suspend fun currentPosition(): Long {
        return player?.let {
            val millis = it.currentTime().useContents {
                value.toDouble() * 1000 / timescale.toDouble()
            }
            millis.toLong()
        } ?: -1
    }

    override suspend fun bufferedProgress(): Float {
        val item = player?.currentItem ?: return 0f
        val ranges = item.loadedTimeRanges() ?: return 0f

        @Suppress("UNCHECKED_CAST")
        val cast =  ranges as List<NSValue>
        val bufferedSecs = cast.firstOrNull()?.let {
            it.CMTimeRangeValue().useContents {
                CMTimeGetSeconds(duration.readValue())
            }
        } ?: 0.0

        val duration = CMTimeGetSeconds(item.duration)
        return (bufferedSecs / duration).toFloat()
    }

    override suspend fun play() {
        withContext(Dispatchers.Main) {
            player?.play()
        }
    }

    override suspend fun pause() {
        withContext(Dispatchers.Main) {
            player?.pause()
        }
    }

    override suspend fun stop() {
        withContext(Dispatchers.Main) {
            player?.replaceCurrentItemWithPlayerItem(null)
        }
    }

    override suspend fun seek(position: Long, autoStart: Boolean) {
        val time = CMTimeMakeWithSeconds(position.toDouble() / 1000, 1000)
        player?.seekToTime(time) { completed ->
            if (completed && autoStart) {
                player?.play()
            }
        }
    }

    override suspend fun getVolume(): Float {
        return userVolume
    }

    override suspend fun setVolume(value: Float) {
        this.userVolume = value
        val rg = if (replayGainEnabled) replayGainDb else 0f
        val volume = mixVolume(replayGain = rg, volume = value)
        withContext(Dispatchers.Main) {
            player?.volume = volume
        }
    }

    override suspend fun setReplayGainEnabled(enabled: Boolean) {
        replayGainEnabled = enabled
        setVolume(userVolume)
    }

    override suspend fun prepare(item: SongItem, autoPlay: Boolean) {
        // Write bytes to a temporary file
        val url: NSURL
        var image: MPMediaItemArtwork? = null
        when(item) {
            is SongItem.Local -> {
                withContext(Dispatchers.IO) {
                    val tempDir = NSFileManager.defaultManager.temporaryDirectory
                    val tempFile =
                        tempDir.URLByAppendingPathComponent("temp_audio.${item.format}") ?: error("Could not create temp file")

                    item.audioBytes.usePinned { pinned ->
                        NSFileManager.defaultManager.createFileAtPath(
                            tempFile.path!!,
                            NSData.dataWithBytes(pinned.addressOf(0), item.audioBytes.size.toULong()),
                            null
                        )
                    }
                    url = NSURL.fileURLWithPath(tempFile.path!!)
                    Logger.i("player", "temp url: ${url.absoluteString}")

                    image = item.coverBytes?.usePinned {
                        UIImage(NSData.dataWithBytes(it.addressOf(0), item.coverBytes.size.toULong()))
                    }?.let {
                        MPMediaItemArtwork(it)
                    }
                }
            }
            is SongItem.Remote -> {
                url = NSURL.URLWithString(item.audioUrl) ?: error("Invalid audio url: ${item.audioUrl}")
                image = null
                updateCoverAsync(item)
            }
        }

        val playerItem = AVPlayerItem.playerItemWithURL(url)
        player?.replaceCurrentItemWithPlayerItem(playerItem)

        currentSongId = item.id
        replayGainDb = item.replayGainDB

        setVolume(userVolume)
        updateNowPlayingInfo(item, image)

        if (autoPlay) play()
    }

    private fun updateCoverAsync(item: SongItem.Remote) {
        item.coverUrl?.let {
            // Load cover url asynchronously
            Logger.d("player", "Loading cover image: $it with URLSession")
            val session = NSURLSession.sharedSession.dataTaskWithURL(
                NSURL.URLWithString(it) ?: error("Invalid cover url: ${item.coverUrl}"),
                completionHandler = { data, response, error ->
                    Logger.d("player", "URLSession completed: data: ${data != null}, resp: ${response != null}, err: ${error != null}")
                    if (error == null && data != null) {
                        if (currentSongId == item.id) {
                            try {
                                val image = MPMediaItemArtwork(UIImage(data = data))
                                updateNowPlayingInfo(item, image)
                            } catch (e: Throwable) {
                                Logger.e("player", "Error loading cover image: $e")
                            }
                        }
                    } else {
                        Logger.e("player", "Error loading cover image: $error")
                    }
                }
            )
            session.resume()
        }
    }

    private fun updateNowPlayingInfo(item: SongItem, cover: MPMediaItemArtwork?) {

        val info = buildMap {
            put(MPMediaItemPropertyTitle, item.title)
            put(MPMediaItemPropertyArtist, item.artist)
            put(MPMediaItemPropertyPlaybackDuration, item.durationSeconds)
            if (cover != null) put(MPMediaItemPropertyArtwork, cover)
        }

        @Suppress("UNCHECKED_CAST")
        nowPlayingCenter.nowPlayingInfo = info as Map<Any?, *>?

    }

    override suspend fun isReady(): Boolean {
        return isPlayerReady
    }

    override suspend fun release() {
        timeObserverToken?.let { player?.removeTimeObserver(it) }
        NSNotificationCenter.defaultCenter.removeObserver(this)
        player?.pause()
//        player?.removeObserver(timeControlObserver, "timeControlStatus")
        player = null
        isPlayerReady = false
    }

    override suspend fun initialize() = withContext(Dispatchers.Main) {
        val session = AVAudioSession.sharedInstance()
        this@IosPlayer.session = session
        session.setCategory(category = AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, null)
        player = AVPlayer()

        NSNotificationCenter.defaultCenter.addObserverForName(
            AVPlayerItemDidPlayToEndTimeNotification,
            null,
            null
        ) { _ ->
            currentSongId = null
            Logger.i("player", "End event")
            listeners.forEach { it.onEvent(PlayEvent.End) }
        }

        NSNotificationCenter.defaultCenter.addObserverForName(
            AVPlayerRateDidChangeNotification,
            null,
            null
        ) { _ ->
            when (player?.timeControlStatus) {
                AVPlayerTimeControlStatusPlaying -> {
                    Logger.i("player", "Play event")
                    listeners.forEach { it.onEvent(PlayEvent.Play) }
                }

                AVPlayerTimeControlStatusPaused -> {
                    Logger.i("player", "Pause event")
                    listeners.forEach { it.onEvent(PlayEvent.Pause) }
                }

                else -> {}
            }
        }

        isPlayerReady = true
    }

    override fun addListener(listener: Player.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)
    }

    /*private val timeControlObserver: NSObject = object : NSObject(), NSKeyValueObservingProtocol {

        override fun observeValueForKeyPath(
            keyPath: String?,
            ofObject: Any?,
            change: Map<Any?, *>?,
            context: COpaquePointer?
        ) {
            println("${keyPath} has been updated to: ${change!![NSKeyValueChangeNewKey]!!}")
        }
    }*/
}