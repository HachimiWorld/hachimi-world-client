package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.artwork_jmid_already_used
import hachimiworld.composeapp.generated.resources.artwork_jmid_prefix_not_set
import hachimiworld.composeapp.generated.resources.publish_audio_file_required
import hachimiworld.composeapp.generated.resources.publish_audio_too_large
import hachimiworld.composeapp.generated.resources.publish_cover_required
import hachimiworld.composeapp.generated.resources.publish_derive_info_required
import hachimiworld.composeapp.generated.resources.publish_description_too_long
import hachimiworld.composeapp.generated.resources.publish_explicit_required
import hachimiworld.composeapp.generated.resources.publish_fetch_jmid_failed
import hachimiworld.composeapp.generated.resources.publish_fetch_user_failed
import hachimiworld.composeapp.generated.resources.publish_https_format
import hachimiworld.composeapp.generated.resources.publish_https_required
import hachimiworld.composeapp.generated.resources.publish_image_too_large
import hachimiworld.composeapp.generated.resources.publish_init_jmid_invalid_format
import hachimiworld.composeapp.generated.resources.publish_init_jmid_prefix_used
import hachimiworld.composeapp.generated.resources.publish_invalid_uid
import hachimiworld.composeapp.generated.resources.publish_jmid_number_format
import hachimiworld.composeapp.generated.resources.publish_jmid_required
import hachimiworld.composeapp.generated.resources.publish_lyrics_invalid_lrc
import hachimiworld.composeapp.generated.resources.publish_lyrics_required
import hachimiworld.composeapp.generated.resources.publish_origin_info_required
import hachimiworld.composeapp.generated.resources.publish_subtitle_too_long
import hachimiworld.composeapp.generated.resources.publish_tag_already_exists
import hachimiworld.composeapp.generated.resources.publish_tag_name_empty
import hachimiworld.composeapp.generated.resources.publish_title_required
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import io.ktor.http.URLParserException
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger
import world.hachimi.app.util.LrcParser
import world.hachimi.app.util.parseJmid
import world.hachimi.app.util.singleLined
import kotlin.random.Random

class PublishViewModel(
    private val global: GlobalStore,
    private val api: ApiClient
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    enum class Type {
        CREATE, EDIT
    }

    data class CrewItem(
        val role: String,
        val uid: Long?,
        val name: String?,
    )

    enum class LyricsType {
        LRC, TEXT, NONE
    }

    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set

    var type by mutableStateOf(Type.CREATE)
        private set
    var songId by mutableStateOf<Long?>(null)
        private set

    var title by mutableStateOf("")
    var subtitle by mutableStateOf("")
    var jmidPrefix by mutableStateOf<String?>(null)
        private set
    var jmidNumber by mutableStateOf<String?>(null)
        private set
    var jmidValid by mutableStateOf<Boolean?>(null)
        private set
    var jmidSupportText by mutableStateOf<String?>(null)
        private set
    var tags by mutableStateOf<List<SongModule.TagItem>>(emptyList())
        private set
    var description by mutableStateOf("")
    var lyricsType by mutableStateOf<LyricsType>(LyricsType.LRC)
    var lyrics by mutableStateOf("")

    var creationType by mutableStateOf(1)
    var originId by mutableStateOf("")
    var originTitle by mutableStateOf("")
    var originArtist by mutableStateOf("")
    var originLink by mutableStateOf("")
    var deriveId by mutableStateOf("")
    var deriveTitle by mutableStateOf("")
    var deriveArtist by mutableStateOf("")
    var deriveLink by mutableStateOf("")

    var coverImageUploadProgress by mutableStateOf(0f)
        private set
    var coverImageUploading by mutableStateOf(false)
        private set
    var coverImage by mutableStateOf<PlatformFile?>(null)
        private set
    var coverImageUrl by mutableStateOf<String?>(null)
        private set
    private var coverTempId: String? = null

    var audioUploadProgress by mutableStateOf(0f)
        private set
    var audioUploading by mutableStateOf(false)
        private set
    var audioFileName by mutableStateOf("")
    var audioDurationSecs by mutableStateOf(0)
        private set
    var audioUploaded by mutableStateOf(false)
    private var audioTempId: String? = null
    var audioUrl by mutableStateOf<String?>(null)
        private set

    var isOperating by mutableStateOf(false)

    var staffs by mutableStateOf<List<CrewItem>>(emptyList())
        private set
    var externalLinks by mutableStateOf<List<SongModule.ExternalLink>>(emptyList())
        private set
    var explicit by mutableStateOf<Boolean?>(null)
    var publishedSongId by mutableStateOf<String?>(null)
        private set
    var showSuccessDialog by mutableStateOf(false)
        private set

    var showAddStaffDialog by mutableStateOf(false)
        private set
    var addStaffUid by mutableStateOf("")
    var addStaffName by mutableStateOf("")
    var addStaffRole by mutableStateOf("")
    var addStaffOperating by mutableStateOf(false)

    var showAddExternalLinkDialog by mutableStateOf(false)
        private set

    var showInitJmidDialog by mutableStateOf(false)
        private set
    var initJmidInput by mutableStateOf("")
        private set
    var initJmidValid by mutableStateOf<Boolean?>(null)
        private set
    var initJmidSupportText by mutableStateOf<String?>(null)
        private set
    var showPrefixInactiveDialog by mutableStateOf(false)

    var loading by mutableStateOf(false)
        private set

    private var originData: SongModule.PublicSongDetail? = null

    private fun clearInput() {
        title = ""
        subtitle = ""

        jmidPrefix = null
        jmidValid = null
        jmidNumber = null
        jmidSupportText = null

        tags = emptyList()
        description = ""
        lyricsType = LyricsType.LRC
        lyrics = ""

        creationType = 1
        originId = ""
        originTitle = ""
        originArtist = ""
        originLink = ""
        deriveId = ""
        deriveTitle = ""
        deriveArtist = ""
        deriveLink = ""

        coverImage = null
        coverTempId = null
        coverImageUrl = null

        audioFileName = ""
        audioDurationSecs = 0
        audioUploaded = false
        audioTempId = null
        audioUrl = null

        staffs = emptyList()
        externalLinks = emptyList()
        explicit = null
    }

    fun mounted(songId: Long?) {
        if (this.songId != songId) {
            // Initialize or change
            this.songId = songId
            init()
        } else {
            // Refresh, actually init
            init()
        }
    }

    fun dispose() {

    }

    fun retry() {
        if (initializeStatus == InitializeStatus.FAILED) {
            init()
        }
    }

    private fun init() {
        initializeStatus = InitializeStatus.INIT
        clearInput()
        if (songId != null) {
            // Edit
            type = Type.EDIT
            loadOriginData()
        } else {
            // Create
            type = Type.CREATE
            loadNextJmid()
        }
    }

    private fun loadOriginData() {
        loading = true
        viewModelScope.launch {
            val data = try {
                val resp = api.songModule.detailById(songId!!)
                if (resp.ok) {
                    resp.ok()
                } else {
                    global.alert(resp.err().msg)
                    if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.FAILED
                    return@launch
                }
            } catch (e: Throwable) {
                Logger.e("publish", "Failed to get origin data", e)
                global.alert(e.message)
                if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.FAILED
                return@launch
            } finally {
                loading = false
            }

            originData = data

            coverImageUrl = data.coverUrl
            audioUrl = data.audioUrl

            title = data.title
            subtitle = data.subtitle

            tags = data.tags
            description = data.description

            // Lyrics
            lyricsType = if (data.lyrics.isEmpty()) {
                LyricsType.NONE
            } else {
                try {
                    LrcParser.parse(data.lyrics)
                    LyricsType.LRC
                } catch (_: Throwable) {
                    LyricsType.TEXT
                }
            }
            lyrics = data.lyrics

            // Origin infos
            creationType = data.creationType
            val origin1 = data.originInfos.find { it.originType == 0 }
            originId = origin1?.songDisplayId ?: ""
            originTitle = origin1?.title ?: ""
            originArtist = origin1?.artist ?: ""
            originLink = origin1?.url ?: ""
            val origin2 = data.originInfos.find { it.originType == 1 }
            deriveId = origin2?.songDisplayId ?: ""
            deriveTitle = origin2?.title ?: ""
            deriveArtist = origin2?.artist ?: ""
            deriveLink = origin2?.url ?: ""

            // Staff
            staffs = data.productionCrew.map {
                CrewItem(it.role, it.uid, it.personName)
            }

            // External links
            externalLinks = data.externalLinks
            explicit = data.explicit

            if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.LOADED
        }
    }

    private fun loadNextJmid() {
        // Get the next jmid or popup a dialog
        viewModelScope.launch {
            try {
                val data = api.publishModule.jmidGetNext()
                if (data.ok) {
                    val jmid = data.ok().jmid
                    val (prefix, number) = parseJmid(jmid) ?: return@launch global.alert(Res.string.publish_fetch_jmid_failed)
                    jmidPrefix = prefix
                    updateJmidNumber(number)
                } else {
                    val err = data.err()
                    when (err.code) {
                        "jmid_prefix_not_specified" -> {
                            // Do nothing
                        }
                        "jmid_prefix_inactive" -> {
                            showPrefixInactiveDialog = true
                            return@launch
                        }
                        else -> {
                            global.alert(data.err().msg)
                            if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.FAILED
                            return@launch
                        }
                    }
                }
            } catch (e: Throwable) {
                Logger.e("publish", "Failed to get jmid prefix", e)
                global.alert(e.message)
                return@launch
            }

            if (initializeStatus == InitializeStatus.INIT) initializeStatus = InitializeStatus.LOADED
        }
    }

    fun setAudioFile() {
        viewModelScope.launch(Dispatchers.Default) {
            val audio = FileKit.openFilePicker(
                type = FileKitType.File("mp3", "flac")
            )
            Logger.d("publish", "Picked audio file: ${audio?.name}, ${audio?.size()} bytes")
            if (audio != null) {
                // 1. Validate
                val size = audio.size()
                if (size > 20 * 1024 * 1024) {
                    global.alert(Res.string.publish_audio_too_large)
                    return@launch
                }
                val buffer = Buffer().apply { write(audio.readBytes()) }

                // 2. Upload
                val data = try {
                    audioUploading = true
                    audioUploadProgress = 0f

                    val resp = api.songModule.uploadAudioFile(
                        filename = audio.name,
                        source = buffer,
                        listener = { sent, total ->
                            audioUploadProgress = (sent.toDouble() / size).toFloat().coerceIn(0f, 1f)
                        }
                    )
                    if (!resp.ok) {
                        global.alert(resp.err().msg)
                        return@launch
                    }

                    resp.ok()
                } catch (e: Throwable) {
                    Logger.e("creation", "Failed to upload audio file", e)
                    global.alert(e.message)
                    return@launch
                } finally {
                    audioUploading = false
                }

                data.title?.let {
                    title = it
                }

                // 3. Save temp id
                Snapshot.withMutableSnapshot {
                    audioTempId = data.tempId
                    audioFileName = audio.name
                    audioDurationSecs = data.durationSecs.toInt()
                    audioUploaded = true
                }
            }
        }
    }

    fun setCoverImage() {
        viewModelScope.launch(Dispatchers.Default) {
            val image = FileKit.openFilePicker(
                type = FileKitType.Image
            )
            if (image != null) {
                // 1. Validate image
                val size = image.size()
                if (size > 10 * 1024 * 1024) {
                    global.alert(Res.string.publish_image_too_large)
                    return@launch
                }
                val buffer = Buffer().apply { write(image.readBytes()) }
                coverImage = image

                // 2. Upload
                val data = try {
                    coverImageUploading = true
                    coverImageUploadProgress = 0f

                    val resp = api.songModule.uploadCoverImage(
                        filename = image.name,
                        source = buffer,
                        listener = { sent, total ->
                            coverImageUploadProgress = (sent.toDouble() / size).toFloat().coerceIn(0f, 1f)
                        }
                    )
                    if (!resp.ok) {
                        global.alert(resp.err().msg)
                        return@launch
                    }

                    resp.ok()
                } catch (e: Throwable) {
                    Logger.e("creation", "Failed to upload cover image", e)
                    global.alert(e.message)
                    return@launch
                } finally {
                    coverImageUploading = false
                }

                // 3. Save temp id
                coverTempId = data.tempId
            }
        }
    }

    var tagInput by mutableStateOf("")
        private set

    private var tagSearchJob: Job? = null
    private val tagSearchMutex = Mutex()
    private var tagSearchSign = 0L
    var tagSearching by mutableStateOf(false)
        private set
    var tagCandidates by mutableStateOf<List<SongModule.TagItem>>(emptyList())
        private set
    var tagCreating by mutableStateOf(false)

    fun updateTagInput(content: String) {
        tagInput = content

        viewModelScope.launch {
            val sign = Random.nextLong()
            val tagJob = launch {
                tagSearchMutex.withLock {
                    tagSearchSign = sign
                }
                tagSearching = true
                try {
                    delay(500) // delay to avoid too many requests
                    val resp = api.songModule.tagSearch(SongModule.TagSearchReq(content))
                    if (resp.ok) {
                        val data = resp.ok()
                        tagSearchMutex.withLock {
                            if (tagSearchSign == sign) {
                                tagCandidates = data.result
                            }
                        }
                    } else {
                        global.alert(resp.err().msg)
                        return@launch
                    }
                } catch (_: CancellationException) {
                    // Do nothing, it's just canceled
                } catch (e: Throwable) {
                    Logger.e("publish", "Failed to search tag", e)
                    global.alert(e.message)
                } finally {
                    tagSearchMutex.withLock {
                        if (tagSearchSign == sign) {
                            tagSearching = false
                        }
                    }
                }
            }

            tagSearchMutex.withLock {
                tagSearchJob?.cancel()
                tagSearchJob = tagJob
            }
        }
    }

    fun clearTagInput() {
        tagInput = ""
        viewModelScope.launch {
            tagSearchMutex.withLock {
                tagSearchJob?.cancel()
                tagSearchJob = null
            }
            tagCandidates = emptyList()
        }
    }

    fun addTag() {
        val label = tagInput
        if (label.isBlank()) {
            global.alert(Res.string.publish_tag_name_empty)
            return
        }
        if (tags.any { item -> item.name == label }) {
            global.alert(Res.string.publish_tag_already_exists)
            return
        }
        viewModelScope.launch {
            val candidate = tagCandidates.find { item -> item.name == label }
            if (candidate != null) {
                tags += candidate
                clearTagInput()
            } else {
                // Create new tag
                tagCreating = true
                try {
                    val resp = api.songModule.tagCreate(
                        SongModule.TagCreateReq(
                            name = label,
                            description = null
                        )
                    )
                    if (resp.ok) {
                        val item = SongModule.TagItem(resp.ok().id, label, null)
                        tags += item
                        clearTagInput()
                    } else {
                        global.alert(resp.err().msg)
                    }
                } catch (e: Throwable) {
                    Logger.e("publish", "Failed to create tag", e)
                    global.alert(e.message)
                } finally {
                    tagCreating = false
                }
            }
        }
    }

    fun selectTag(item: SongModule.TagItem) {
        if (tags.any { it -> it.name == item.name }) {
            global.alert(Res.string.publish_tag_already_exists)
            return
        }
        tags += item
        clearTagInput()
    }

    fun removeTag(index: Int) {
        tags = tags.toMutableList().also {
            it.removeAt(index)
        }.toList()
    }

    fun addLink(platform: String, link: String) {
        externalLinks += SongModule.ExternalLink(platform, link)
    }

    fun removeLink(index: Int) {
        externalLinks = externalLinks.toMutableList().also {
            it.removeAt(index)
        }
    }

    fun publish() = viewModelScope.launch {
        val checkPass = if (type == Type.CREATE) {
            checkInputForCreate()
        } else {
            checkInputForEdit()
        }
        if (!checkPass) return@launch

        try {
            isOperating = true

            val creationInfo = SongModule.PublishReq.CreationInfo(
                creationType = creationType,
                originInfo = if (creationType > 0) SongModule.CreationTypeInfo(
                    songDisplayId = originId.takeIf { it.isNotBlank() },
                    title = originTitle.takeIf { it.isNotBlank() },
                    url = originLink.takeIf { it.isNotBlank() },
                    artist = originArtist.takeIf { it.isNotBlank() },
                    originType = 0
                ) else null,
                derivativeInfo = if (creationType > 1) SongModule.CreationTypeInfo(
                    songDisplayId = deriveId.takeIf { it.isNotBlank() },
                    title = deriveTitle.takeIf { it.isNotBlank() },
                    url = deriveLink.takeIf { it.isNotBlank() },
                    artist = deriveArtist.takeIf { it.isNotBlank() },
                    originType = 1
                ) else null
            )

            val crew = staffs.map {
                SongModule.PublishReq.ProductionItem(
                    role = it.role,
                    uid = it.uid,
                    name = it.name
                )
            }

            val lyrics = lyrics.takeIf { lyricsType != LyricsType.NONE } ?: ""
            val tagIds = tags.map { it.id }

            if (type == Type.CREATE) {
                val resp = api.songModule.publish(
                    SongModule.PublishReq(
                        songTempId = audioTempId!!,
                        coverTempId = coverTempId!!,
                        title = title,
                        subtitle = subtitle,
                        description = description,
                        lyrics = lyrics,
                        tagIds = tagIds,
                        creationInfo = creationInfo,
                        productionCrew = crew,
                        externalLinks = externalLinks,
                        explicit = explicit,
                        jmid = "JM-${jmidPrefix!!}-${jmidNumber!!}",
                        comment = null,
                    )
                )
                if (resp.ok) {
                    val data = resp.ok()
                    publishedSongId = data.songDisplayId
                    showSuccessDialog = true
                    clearInput()
                } else {
                    val data = resp.err()
                    global.alert(data.msg)
                }
            } else {
                val resp = api.publishModule.modify(
                    PublishModule.ModifyReq(
                        songId = songId!!,
                        songTempId = audioTempId,
                        coverTempId = coverTempId,
                        title = title,
                        subtitle = subtitle,
                        description = description,
                        lyrics = lyrics,
                        tagIds = tagIds,
                        creationInfo = creationInfo,
                        productionCrew = crew,
                        externalLinks = externalLinks,
                        explicit = explicit!!,
                        comment = null
                    )
                )
                if (resp.ok) {
                    showSuccessDialog = true
                    clearInput()
                } else {
                    global.alert(resp.err().msg)
                }
            }
        } catch (e: Throwable) {
            Logger.e("creation", "Failed to publish song", e)
            global.alert(e.message)
        } finally {
            isOperating = false
        }
    }

    fun closeDialog() {
        showSuccessDialog = false
        global.nav.back()
    }

    fun addStaff() {
        addStaffUid = ""
        addStaffName = ""
        addStaffRole = ""
        showAddStaffDialog = true
    }

    fun cancelAddStaff() {
        showAddStaffDialog = false
    }

    fun confirmAddStaff() {
        viewModelScope.launch {
            addStaffOperating = true

            // Check uid and get name
            if (addStaffUid.isNotBlank()) {
                val uid = addStaffUid.toLongOrNull()
                if (uid == null) {
                    global.alert(Res.string.publish_invalid_uid)
                    return@launch
                }
                try {
                    val resp = api.userModule.profile(uid)
                    if (resp.ok) {
                        val data = resp.ok()
                        addStaffName = data.username
                    } else {
                        val data = resp.err()
                        global.alert(data.msg)
                        return@launch
                    }
                } catch (e: Throwable) {
                    Logger.e("publish", "Failed to get user info", e)
                    global.alert(Res.string.publish_fetch_user_failed)
                    return@launch
                } finally {
                    addStaffOperating = false
                }
            }

            staffs += CrewItem(addStaffRole, addStaffUid.toLongOrNull(), addStaffName)
            addStaffOperating = false
            showAddStaffDialog = false
        }
    }

    fun removeStaff(index: Int) {
        staffs = staffs.toMutableList().also {
            it.removeAt(index)
        }
    }

    private fun checkInputForCreate(): Boolean {
        if (audioTempId == null) {
            global.alert(Res.string.publish_audio_file_required)
            return false
        }

        if (coverTempId == null) {
            global.alert(Res.string.publish_cover_required)
            return false
        }

        if (jmidPrefix == null || jmidNumber == null) {
            global.alert(Res.string.publish_jmid_required)
            return false
        }

        return checkInputsCommon()
    }

    private fun checkInputForEdit(): Boolean {
        return checkInputsCommon()
    }

    private fun checkInputsCommon(): Boolean {
        if (title.isBlank()) {
            global.alert(Res.string.publish_title_required)
            return false
        }

        if (subtitle.isBlank()) {
            // Do nothing
        }
        if (subtitle.length > 32) {
            global.alert(Res.string.publish_subtitle_too_long)
            return false
        }

        if (description.isBlank()) {
            // Do nothing
        }
        if (description.length > 500) {
            global.alert(Res.string.publish_description_too_long)
            return false
        }

        if (lyricsType != LyricsType.NONE && lyrics.isBlank()) {
            global.alert(Res.string.publish_lyrics_required)
            return false
        }

        if (lyricsType == LyricsType.LRC) {
            val lines = try {
                LrcParser.parse(lyrics)
            } catch (_: Throwable) {
                global.alert(Res.string.publish_lyrics_invalid_lrc)
                return false
            }
            if (lines.isEmpty()) {
                global.alert(Res.string.publish_lyrics_invalid_lrc)
                return false
            }
        }

        if (creationType > 0) {
            if (originId.isBlank() && originTitle.isBlank()) {
                global.alert(Res.string.publish_origin_info_required)
                return false
            }
        }

        if (creationType > 1) {
            if (deriveId.isBlank() && deriveTitle.isBlank()) {
                global.alert(Res.string.publish_derive_info_required)
                return false
            }
        }

        listOf(originLink, deriveLink).forEach {
            if (it.isNotEmpty()) {
                try {
                    val url = Url(it)
                    if (url.protocolOrNull != URLProtocol.HTTPS) {
                        global.alert(Res.string.publish_https_required)
                        return false
                    }
                } catch (_: URLParserException) {
                    global.alert(Res.string.publish_https_format)
                    return false
                }
            }
        }

        if (explicit == null) {
            global.alert(Res.string.publish_explicit_required)
            return false
        }

        return true
    }

    fun showAddExternalLinkDialog() {
        showAddExternalLinkDialog = true
    }

    fun closeAddExternalLinkDialog() {
        showAddExternalLinkDialog = false
    }

    fun showJmidDialog() {
        showInitJmidDialog = true
    }

    private var checkJmidPrefixJob: Job? = null
    private val checkJmidPrefixMutex = Mutex()

    fun updateInitJmidInput(input: String) {
        initJmidValid = null
        initJmidSupportText = null
        val mappedInput = input.singleLined().uppercase()
        initJmidInput = mappedInput

        if (Regex("[A-Z]{3,4}").matches(mappedInput)) {
            viewModelScope.launch {
                checkJmidPrefixMutex.withLock {
                    try {
                        checkJmidPrefixJob?.cancelAndJoin()
                    } catch (_: CancellationException) {}
                    checkJmidPrefixJob = launch {
                        delay(500)
                        try {
                            val resp = api.publishModule.jmidCheckPrefix(PublishModule.JmidCheckPReq(mappedInput))
                            if (mappedInput == initJmidInput) {
                                if (resp.ok) {
                                    if (resp.ok().result) {
                                        initJmidValid = true
                                        initJmidSupportText = null
                                    } else {
                                        initJmidValid = false
                                        viewModelScope.launch {
                                            initJmidSupportText = org.jetbrains.compose.resources.getString(Res.string.publish_init_jmid_prefix_used)
                                        }
                                    }
                                } else {
                                    initJmidValid = false
                                    initJmidSupportText = resp.err().msg
                                }
                            }
                        } catch (e: Throwable) {
                            Logger.e("publish", "Failed to check jmid", e)
                            initJmidValid = false
                            initJmidSupportText = e.message
                        }
                    }
                }
            }
        } else {
            viewModelScope.launch {
                initJmidSupportText = org.jetbrains.compose.resources.getString(Res.string.publish_init_jmid_invalid_format)
            }
            initJmidValid = false
        }
    }

    fun confirmInitJmid() {
        jmidPrefix = initJmidInput
        showInitJmidDialog = false

        initJmidInput = ""
        initJmidValid = null
        initJmidSupportText = null
    }

    fun cancelInitJmid() {
        showInitJmidDialog = false
    }

    private var checkJmidJob: Job? = null
    private val checkJmidMutex = Mutex()

    fun updateJmidNumber(number: String) {
        jmidValid = null
        jmidSupportText = null
        val mappedInput = number.singleLined()
        jmidNumber = mappedInput
        val prefix = jmidPrefix ?: run {
            // unreachable
            jmidValid = false
            viewModelScope.launch {
                jmidSupportText = org.jetbrains.compose.resources.getString(Res.string.artwork_jmid_prefix_not_set)
            }
            return
        }

        val jmidFull = "JM-$prefix-$mappedInput"

        if (Regex("\\d{3}").matches(mappedInput)) {
            viewModelScope.launch {
                checkJmidMutex.withLock {
                    try {
                        checkJmidJob?.cancelAndJoin()
                    } catch (_: CancellationException) {}

                    checkJmidJob = launch {
                        delay(500)
                        try {
                            val resp = api.publishModule.jmidCheck(PublishModule.JmidCheckReq(jmidFull))
                            if (jmidNumber == mappedInput) {
                                if (resp.ok) {
                                    if (resp.ok().result) {
                                        jmidValid = true
                                        jmidSupportText = null
                                    } else {
                                        jmidValid = false
                                        viewModelScope.launch {
                                            jmidSupportText = org.jetbrains.compose.resources.getString(Res.string.artwork_jmid_already_used)
                                        }
                                    }
                                } else {
                                    jmidValid = false
                                    jmidSupportText = resp.err().msg
                                }
                            }
                        } catch (e: Throwable) {
                            Logger.e("publish", "Failed to check jmid", e)
                            jmidValid = false
                            jmidSupportText = e.message
                        }
                    }
                }
            }
        } else {
            jmidValid = false
            viewModelScope.launch {
                jmidSupportText = org.jetbrains.compose.resources.getString(Res.string.publish_jmid_number_format)
            }
        }
    }
}

