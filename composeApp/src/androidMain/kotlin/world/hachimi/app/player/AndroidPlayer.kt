package world.hachimi.app.player

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import io.github.vinceglb.filekit.AndroidFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger
import world.hachimi.app.player.Player.Companion.mixVolume

class AndroidPlayer(
    private val controllerFuture: ListenableFuture<MediaController>
) : Player {
    private var controller: MediaController? = null
    private var ready = false
    private val listeners: MutableSet<Player.Listener> = mutableSetOf()
    private var initialized = MutableStateFlow<Boolean>(false)
    private var replayGainDb: Float = 0f
    private var userVolume = 1f

    init {
        Logger.i("player", "Waiting for MediaController")
        controllerFuture.addListener({
            val controller = try {
                controllerFuture.get()
            } catch (e: Throwable) {
                Logger.e("player", "Failed to get MediaController", e)
                // TODO: Should we notify user or just throw?
                throw e
            }
            ready = true
            initialized.tryEmit(true)
            Logger.i("player", "MediaController is ready")
            controller.addListener(object : androidx.media3.common.Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        listeners.forEach { listener -> listener.onEvent(PlayEvent.Play) }
                    } else {
                        // TODO[opt](player): We assume the player won't go wrong. We should handle it.
                        if (controller.playbackState == androidx.media3.common.Player.STATE_ENDED) {
                            listeners.forEach { listener -> listener.onEvent(PlayEvent.End) }
                        } else {
                            listeners.forEach { listener -> listener.onEvent(PlayEvent.Pause) }
                        }

                        if (controller.playerError != null) {
                            Logger.e("player", "Error occurred: ${controller.playerError?.message}")
                        }
                    }
                }
            })
            this@AndroidPlayer.controller = controller
        }, MoreExecutors.directExecutor())
    }

    override suspend fun isPlaying(): Boolean = withContext(Dispatchers.Main) {
        controller?.isPlaying ?: false
    }

    override suspend fun isEnd(): Boolean = withContext(Dispatchers.Main) {
        controller?.let {
            return@withContext it.currentPosition >= it.duration && !it.isPlaying
        }
        return@withContext false
    }

    override suspend fun currentPosition(): Long = withContext(Dispatchers.Main) {
        controller?.currentPosition ?: 0
    }

    override suspend fun play() {
        withContext(Dispatchers.Main) {
            controller?.play()
        }
    }

    override suspend fun pause() {
        withContext(Dispatchers.Main) {
            controller?.pause()
        }
    }

    override suspend fun stop() {
        try {
            withContext(Dispatchers.Main) {
                controller?.stop()
            }
        } catch (e: Throwable) {
            Logger.e("player", "Failed to stop", e)
        }
    }

    override suspend fun seek(position: Long, autoStart: Boolean) = withContext(Dispatchers.Main) {
        withContext(Dispatchers.Main) {
            controller?.seekTo(position)
            if (autoStart) {
                controller?.play()
            }
        }
    }

    override suspend fun getVolume(): Float = userVolume

    override suspend fun setVolume(value: Float) = withContext(Dispatchers.Main) {
        userVolume = value
        val volume = mixVolume(replayGain = replayGainDb, volume = value)
        controller?.volume = volume
    }

    override suspend fun prepare(item: SongItem, autoPlay: Boolean) {
        // TODO[refactor]: This is a workaround to get uri. Consider to use network uri or other ways in the future.
        val audioFile = withContext(Dispatchers.IO) {
            val cacheDir = (getPlatform().getCacheDir().androidFile as AndroidFile.FileWrapper)
            cacheDir.file.resolve("playing.${item.format}").also {
                it.writeBytes(item.audioBytes)
            }
        }
        val audioUri = audioFile.toUri()


        /*val coverFile = item.coverBytes?.let { bytes ->
            withContext(Dispatchers.IO) {
                getPlatform().getCacheDir().resolve("playing_cover").also {
                    it.writeBytes(bytes)
                }
            }
        }

        val coverUri = coverFile?.toUri()*/

        val metadata = MediaMetadata.Builder()
            .setTitle(item.title)
            .setArtist(item.artist)
            .setArtworkData(item.coverBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
            .build()
        val mediaItem = MediaItem.Builder()
            .setUri(audioUri)
            .setMediaMetadata(metadata)
            .build()
        replayGainDb = item.replayGainDB
        setVolume(userVolume)

        withContext(Dispatchers.Main) {
            controller?.setMediaItem(mediaItem)
            if (autoPlay) {
                controller?.play()
            }
        }
    }

    override suspend fun isReady(): Boolean = withContext(Dispatchers.Main) {
        ready
    }

    override suspend fun release(): Unit = withContext(Dispatchers.Main) {
        controller?.release()
    }

    override fun addListener(listener: Player.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)
    }

    override suspend fun initialize() {
        initialized.first { initialized -> initialized }
    }
}