package world.hachimi.app.model

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.post_content_required
import hachimiworld.composeapp.generated.resources.publish_cover_required
import hachimiworld.composeapp.generated.resources.publish_image_too_large
import hachimiworld.composeapp.generated.resources.publish_title_required
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.PostModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

@KoinViewModel
class CreatePostViewModel(
    private val global: GlobalStore,
    private val api: ApiClient,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {

    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set

    var title by mutableStateOf("")
    var content by mutableStateOf("")

    var coverImageUploadProgress by mutableStateOf(0f)
        private set
    var coverImageUploading by mutableStateOf(false)
        private set
    var coverImage by mutableStateOf<PlatformFile?>(null)
        private set

    private var coverFileId: String? = null

    var isOperating by mutableStateOf(false)
        private set

    var publishedPostId by mutableStateOf<Long?>(null)
        private set

    var showSuccessDialog by mutableStateOf(false)
        private set

    fun mounted() {
        if (initializeStatus == InitializeStatus.INIT) {
            initializeStatus = InitializeStatus.LOADED
        }
    }

    fun retry() {
        if (initializeStatus == InitializeStatus.FAILED) {
            initializeStatus = InitializeStatus.LOADED
        }
    }

    fun setCoverImage() {
        viewModelScope.launch(Dispatchers.Default) {
            val image = FileKit.openFilePicker(type = FileKitType.Image)
            if (image != null) {
                val size = image.size()

                if (size > 10 * 1024 * 1024) {
                    global.alert(Res.string.publish_image_too_large)
                    return@launch
                }

                val buffer = Buffer().apply { write(image.readBytes()) }
                coverImage = image

                val data = try {
                    coverImageUploading = true
                    coverImageUploadProgress = 0f

                    val resp = api.postModule.uploadImage(
                        filename = image.name,
                        source = buffer,
                        listener = { sent, total ->
                            val progress = (sent.toDouble() / size).toFloat()
                            coverImageUploadProgress = progress.coerceIn(0f, 1f)
                        }
                    )
                    if (!resp.ok) {
                        val err = resp.err()
                        global.alert(err.msg)
                        return@launch
                    }
                    resp.ok()
                } catch (e: Throwable) {
                    Logger.e("post", "Failed to upload cover image", e)
                    global.alert(e.message)
                    return@launch
                } finally {
                    coverImageUploading = false
                }

                coverFileId = data.fileId
            }
        }
    }

    fun publish() {
        viewModelScope.launch(Dispatchers.Default) {
            val t = title.trim()
            val c = content.trim()
            if (t.isBlank()) {
                global.alert(Res.string.publish_title_required)
                return@launch
            }
            if (c.isBlank()) {
                global.alert(Res.string.post_content_required)
                return@launch
            }
            if (coverFileId.isNullOrBlank()) {
                global.alert(Res.string.publish_cover_required)
                return@launch
            }

            try {
                isOperating = true
                val resp = api.postModule.create(
                    PostModule.CreateReq(
                        title = t,
                        content = c,
                        coverFileId = coverFileId,
                        contentType = "markdown"
                    )
                )
                if (resp.ok) {
                    val id = resp.ok().id
                    publishedPostId = id
                    showSuccessDialog = true
                } else {
                    val err = resp.err()
                    global.alert(err.msg)
                }
            } catch (e: Throwable) {
                Logger.e("post", "Failed to create post", e)
                global.alert(e.message)
            } finally {
                isOperating = false
            }
        }
    }

    fun closeSuccessDialog() {
        showSuccessDialog = false
    }
}