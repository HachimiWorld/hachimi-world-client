package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import hachimiworld.composeapp.generated.resources.need_login_message
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer
import org.jetbrains.compose.resources.StringResource
import org.koin.core.annotation.Singleton
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.MomentAttachment
import world.hachimi.app.api.module.MomentModule
import world.hachimi.app.api.module.PlaylistModule
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

/** What a moment is written to share. @since 261114 */
sealed interface MomentShare {
    data class Song(val songId: Long) : MomentShare
    data class Playlist(val playlistId: Long) : MomentShare
}

/**
 * The moment being written. It opens over the current page, from the square or from a song's or playlist's share
 * action, and the root screen draws it once.
 *
 * @since 261114
 */
@Singleton
class MomentComposer(
    private val api: ApiClient,
    private val global: GlobalStore,
    private val store: MomentStore,
) {
    /** Non-null while the composer is shown. */
    var draft by mutableStateOf<MomentDraft?>(null)
        private set

    fun open(share: MomentShare? = null) {
        if (!global.isLoggedIn) {
            global.alert(Res.string.need_login_message)
            return
        }
        draft?.dispose()
        draft = MomentDraft(share, api, global, store, onPosted = ::close)
    }

    /** Drops the draft, uploads included. */
    fun close() {
        draft?.dispose()
        draft = null
    }
}

/**
 * Writing a moment: text and images, plus the song or playlist it shares.
 *
 * @since 261106
 */
class MomentDraft internal constructor(
    /** The song or playlist it was opened to share. */
    val share: MomentShare?,
    private val api: ApiClient,
    private val global: GlobalStore,
    private val store: MomentStore,
    private val onPosted: () -> Unit,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var content by mutableStateOf("")
    val images = mutableStateListOf<DraftImage>()
    var publishing by mutableStateOf(false)
        private set

    /**
     * The shared song or playlist as the moment will show it: null while it loads, [MomentAttachment.Unavailable]
     * when it can't be shared (deleted, hidden or private).
     *
     * @since 261114
     */
    var shared by mutableStateOf<MomentAttachment?>(null)
        private set
    /** @since 261114 */
    var sharedLoadFailed by mutableStateOf(false)
        private set

    /** Uploads go one at a time, as the server checks a user's images one at a time. */
    private val uploadLock = Mutex()
    private var nextKey = 0L

    val canAddImages get() = images.size < MomentModule.MAX_IMAGES

    /** Everything's uploaded, and there's something to post. */
    val canPublish: Boolean
        get() = !publishing &&
            images.all { it.state is DraftImage.State.Uploaded } &&
            if (share != null) shared.let { it is MomentAttachment.Song || it is MomentAttachment.Playlist }
            else content.isNotBlank() || images.isNotEmpty()

    /** Whether leaving would lose something. */
    val hasDraft: Boolean get() = content.isNotBlank() || images.isNotEmpty()

    init {
        loadShared()
    }

    /** Loads the shared song or playlist, to show it before posting. */
    fun loadShared() {
        if (share == null) return
        sharedLoadFailed = false
        scope.launch {
            try {
                when (share) {
                    is MomentShare.Song -> {
                        val resp = api.songModule.detailById(share.songId, auth = true)
                        shared = if (resp.ok) MomentAttachment.Song(resp.ok())
                        else MomentAttachment.Unavailable(MomentAttachment.TYPE_SONG)
                    }
                    is MomentShare.Playlist -> {
                        val resp = api.playlistModule.detail(PlaylistModule.PlaylistIdReq(share.playlistId))
                        val detail = if (resp.ok) resp.ok<PlaylistModule.DetailResp>() else null
                        shared = if (detail != null && detail.playlistInfo.isPublic && !detail.playlistInfo.isHidden) {
                            MomentAttachment.Playlist(detail.toMetadata())
                        } else {
                            MomentAttachment.Unavailable(MomentAttachment.TYPE_PLAYLIST)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(TAG, "Failed to load what's being shared", e)
                sharedLoadFailed = true
            }
        }
    }

    fun pickImages() {
        scope.launch {
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
        image.job = scope.launch {
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
        scope.launch {
            publishing = true
            try {
                val req = MomentModule.CreateReq(
                    content = content.trim(),
                    imageIds = images.map { (it.state as DraftImage.State.Uploaded).imageId },
                    attachments = listOfNotNull(
                        when (share) {
                            is MomentShare.Song -> MomentModule.AttachmentInput.song(share.songId)
                            is MomentShare.Playlist -> MomentModule.AttachmentInput.playlist(share.playlistId)
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
                    onPosted()
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

    internal fun dispose() {
        scope.cancel()
    }

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

private fun PlaylistModule.DetailResp.toMetadata() = PlaylistModule.PlaylistMetadata(
    id = playlistInfo.id,
    userId = creatorProfile.uid,
    userName = creatorProfile.username,
    userAvatarUrl = creatorProfile.avatarUrl,
    name = playlistInfo.name,
    description = playlistInfo.description,
    coverUrl = playlistInfo.coverUrl,
    songsCount = playlistInfo.songsCount.toLong(),
    createTime = playlistInfo.createTime,
    updateTime = playlistInfo.updateTime,
)
