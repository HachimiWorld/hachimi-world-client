package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.moment_create_failed
import hachimiworld.composeapp.generated.resources.moment_create_pending
import hachimiworld.composeapp.generated.resources.moment_created
import hachimiworld.composeapp.generated.resources.moment_err_image_expired
import hachimiworld.composeapp.generated.resources.moment_err_invalid_attachment
import hachimiworld.composeapp.generated.resources.moment_err_rate_limited
import hachimiworld.composeapp.generated.resources.moment_err_rejected
import hachimiworld.composeapp.generated.resources.moment_err_user_banned
import hachimiworld.composeapp.generated.resources.moment_image_too_large
import hachimiworld.composeapp.generated.resources.moment_too_many_images
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer
import org.jetbrains.compose.resources.StringResource
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.MomentModule
import world.hachimi.app.api.module.PlayHistoryModule
import world.hachimi.app.api.module.PlaylistModule
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

/** An image picked for a new moment, uploaded as soon as it's picked. @since 261106 */
class DraftImage(
    val key: Long,
    /** Shown until it's uploaded. */
    val bytes: ByteArray,
) {
    var state by mutableStateOf<State>(State.Uploading(0f))
        internal set
    internal var job: Job? = null

    sealed interface State {
        data class Uploading(val progress: Float) : State
        data class Uploaded(val imageId: String, val thumbUrl: String) : State
        /** The check blocked it; it has to be swapped. */
        data object Rejected : State
        data class Failed(val message: String?) : State
    }
}

/** What a new moment links to. @since 261106 */
sealed interface DraftAttachment {
    data class Song(val song: SongModule.PublicSongDetail) : DraftAttachment
    data class Playlist(val playlist: PlaylistModule.PlaylistItem) : DraftAttachment
}

/** Writing a moment: text, images and an attachment. @since 261106 */
@KoinViewModel
class CreateMomentViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
    private val store: MomentStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    var content by mutableStateOf("")
    val images = mutableStateListOf<DraftImage>()
    var attachment by mutableStateOf<DraftAttachment?>(null)
    var publishing by mutableStateOf(false)
        private set

    private val _done = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    /** Sent once it's posted, to leave the page. */
    val done: SharedFlow<Unit> = _done

    /** Uploads go one at a time, as the server checks a user's images one at a time. */
    private val uploadLock = Mutex()
    private var nextKey = 0L

    val canAddImages get() = images.size < MomentModule.MAX_IMAGES

    /** Everything's uploaded, and there's something to post. */
    val canPublish: Boolean
        get() = !publishing &&
            images.all { it.state is DraftImage.State.Uploaded } &&
            (content.isNotBlank() || images.isNotEmpty() || attachment != null)

    /** Whether leaving would lose something. */
    val hasDraft: Boolean get() = content.isNotBlank() || images.isNotEmpty() || attachment != null

    fun pickImages() {
        viewModelScope.launch {
            val room = MomentModule.MAX_IMAGES - images.size
            if (room <= 0) {
                global.alert(Res.string.moment_too_many_images, MomentModule.MAX_IMAGES.toString())
                return@launch
            }
            val files = try {
                FileKit.openFilePicker(type = FileKitType.Image, mode = FileKitMode.Multiple(maxItems = room))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to pick images", e)
                global.alert(e.message)
                null
            } ?: return@launch
            if (files.size > room) global.alert(Res.string.moment_too_many_images, MomentModule.MAX_IMAGES.toString())
            files.take(room).forEach { add(it) }
        }
    }

    private suspend fun add(file: PlatformFile) {
        if (file.size() > MomentModule.IMAGE_MAX_BYTES) {
            global.alert(Res.string.moment_image_too_large, file.name)
            return
        }
        val bytes = file.readBytes()
        val image = DraftImage(++nextKey, bytes)
        images.add(image)
        upload(image, file.name)
    }

    private fun upload(image: DraftImage, filename: String) {
        image.state = DraftImage.State.Uploading(0f)
        image.job = viewModelScope.launch {
            uploadLock.withLock {
                try {
                    val size = image.bytes.size.toDouble()
                    val resp = api.momentModule.uploadImage(
                        filename = filename,
                        source = Buffer().apply { write(image.bytes) },
                        listener = { sent, _ ->
                            image.state = DraftImage.State.Uploading((sent / size).toFloat().coerceIn(0f, 1f))
                        },
                    )
                    image.state = if (resp.ok) {
                        val data = resp.ok<MomentModule.UploadImageResp>()
                        DraftImage.State.Uploaded(data.imageId, data.thumbUrl)
                    } else {
                        val err = resp.err()
                        if (err.code == MomentModule.ERR_IMAGE_REJECTED) DraftImage.State.Rejected
                        else DraftImage.State.Failed(err.msg)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Logger.e(TAG, "Failed to upload an image", e)
                    image.state = DraftImage.State.Failed(e.message)
                }
            }
        }
    }

    fun retryUpload(image: DraftImage) {
        if (image.state !is DraftImage.State.Failed) return
        upload(image, "image")
    }

    fun removeImage(image: DraftImage) {
        image.job?.cancel()
        images.remove(image)
    }

    fun moveImage(from: Int, to: Int) {
        if (from !in images.indices || to !in images.indices) return
        images.add(to, images.removeAt(from))
    }

    fun publish() {
        if (!canPublish) return
        viewModelScope.launch {
            publishing = true
            try {
                val req = MomentModule.CreateReq(
                    content = content.trim(),
                    imageIds = images.map { (it.state as DraftImage.State.Uploaded).imageId },
                    attachments = listOfNotNull(
                        when (val x = attachment) {
                            is DraftAttachment.Song -> MomentModule.AttachmentInput.song(x.song.id)
                            is DraftAttachment.Playlist -> MomentModule.AttachmentInput.playlist(x.playlist.id)
                            null -> null
                        }
                    ),
                )
                val resp = api.momentModule.create(req)
                if (resp.ok) {
                    val moment = resp.ok<MomentModule.CreateResp>().moment
                    if (moment != null) {
                        store.posted(moment)
                        global.alert(Res.string.moment_created)
                    } else {
                        global.alert(Res.string.moment_create_pending)
                    }
                    content = ""
                    images.clear()
                    attachment = null
                    _done.tryEmit(Unit)
                } else {
                    val err = resp.err()
                    if (err.code == MomentModule.ERR_IMAGE_NOT_FOUND) {
                        // Kept over an hour: they have to go up again
                        images.forEach { it.state = DraftImage.State.Failed(null) }
                    }
                    val message = createErrorMessage(err.code)
                    if (message != null) global.alert(message) else global.alert(err.msg)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to post a moment", e)
                global.alert(Res.string.moment_create_failed)
            } finally {
                publishing = false
            }
        }
    }

    // region Attachment picker

    var recentSongs by mutableStateOf<List<SongModule.PublicSongDetail>?>(null)
        private set
    var myPlaylists by mutableStateOf<List<PlaylistModule.PlaylistItem>?>(null)
        private set
    var pickerFailed by mutableStateOf(false)
        private set

    /** Loads what the picker lists: recently played songs and the user's playlists. */
    fun loadPickerSources() {
        pickerFailed = false
        viewModelScope.launch {
            try {
                if (recentSongs == null) {
                    val resp = api.playHistoryModule.cursor(PlayHistoryModule.CursorReq(cursor = null, size = 50))
                    if (resp.ok) {
                        recentSongs = resp.ok<PlayHistoryModule.CursorResp>().list
                            .map { it.songInfo }
                            .distinctBy { it.id }
                    } else {
                        pickerFailed = true
                    }
                }
                if (myPlaylists == null) {
                    val resp = api.playlistModule.list()
                    if (resp.ok) myPlaylists = resp.ok<PlaylistModule.ListResp>().playlists
                    else pickerFailed = true
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load what can be attached", e)
                pickerFailed = true
            }
        }
    }

    // endregion

    companion object {
        private const val TAG = "create_moment"

        fun createErrorMessage(code: String): StringResource? = when (code) {
            MomentModule.ERR_MOMENT_REJECTED -> Res.string.moment_err_rejected
            MomentModule.ERR_RATE_LIMITED -> Res.string.moment_err_rate_limited
            MomentModule.ERR_IMAGE_NOT_FOUND -> Res.string.moment_err_image_expired
            MomentModule.ERR_INVALID_ATTACHMENT -> Res.string.moment_err_invalid_attachment
            MomentModule.ERR_USER_BANNED -> Res.string.moment_err_user_banned
            else -> null
        }
    }
}
