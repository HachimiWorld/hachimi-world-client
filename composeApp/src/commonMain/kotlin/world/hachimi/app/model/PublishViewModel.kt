package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.*
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
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
    val tags = mutableStateListOf<SongModule.TagItem>()
    var description by mutableStateOf("")
    var lyricsType by mutableStateOf(0)
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

    var isOperating by mutableStateOf(false)

    val staffs = mutableStateListOf<CrewItem>()

    /*    data class TagItem(
            val id: Long,
            val label: String
        )
        */
    data class CrewItem(
        val role: String,
        val uid: Long?,
        val name: String?,
    )

    val externalLinks = mutableStateListOf<SongModule.ExternalLink>()
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
    private fun clearInput() {
        title = ""
        subtitle = ""

        jmidPrefix = null
        jmidValid = null
        jmidNumber = null
        jmidSupportText = null

        tags.clear()
        description = ""
        lyricsType = 0
        lyrics = ""

        creationType = 0
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

        audioFileName = ""
        audioDurationSecs = 0
        audioUploaded = false
        audioTempId = null

        staffs.clear()
        externalLinks.clear()
        explicit = null
    }

    fun mounted() {
        // Get the next jmid or popup a dialog
        viewModelScope.launch {
            try {
                val data = api.publishModule.jmidGetNext()
                if (data.ok) {
                    val jmid = data.ok().jmid
                    val (prefix, number) = parseJmid(jmid) ?: return@launch global.alert("获取可用基米ID失败")
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
                        }
                    }
                }
            } catch (e: Throwable) {
                Logger.e("publish", "Failed to get jmid prefix", e)
                global.alert(e.message)
                return@launch
            }
        }
    }

    fun dispose() {

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
                    global.alert("音频文件过大，最大支持20MB")
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
                        global.alert(resp.errData<CommonError>().msg)
                        return@launch
                    }

                    resp.okData<SongModule.UploadAudioFileResp>()
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
                    global.alert("图片过大，最大支持 10MB")
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
                        global.alert(resp.errData<CommonError>().msg)
                        return@launch
                    }

                    resp.okData<SongModule.UploadImageResp>()
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
                        val data = resp.okData<SongModule.TagSearchResp>()
                        tagSearchMutex.withLock {
                            if (tagSearchSign == sign) {
                                tagCandidates = data.result
                            }
                        }
                    } else {
                        global.alert(resp.errData<CommonError>().msg)
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
            global.alert("标签名称不可为空")
            return
        }
        if (tags.any { item -> item.name == label }) {
            global.alert("标签已存在")
            return
        }
        viewModelScope.launch {
            val candidate = tagCandidates.find { item -> item.name == label }
            if (candidate != null) {
                tags.add(candidate)
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
                        tags.add(item)
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
            global.alert("标签已存在")
            return
        }
        tags.add(item)
        clearTagInput()
    }

    fun removeTag(index: Int) {
        tags.removeAt(index)
    }

    fun addLink(platform: String, link: String) {
        externalLinks.add(SongModule.ExternalLink(platform, link))
    }

    fun removeLink(index: Int) {
        externalLinks.removeAt(index)
    }

    fun publish() = viewModelScope.launch {
        if (!validateInputs()) return@launch

        try {
            isOperating = true

            val creationInfo = SongModule.PublishReq.CreationInfo(
                creationType = creationType,
                originInfo = if (creationType > 0) SongModule.CreationTypeInfo(
                    songDisplayId = originId.takeIf { it.isNotBlank() },
                    title = originTitle.takeIf { it.isNotBlank() },
                    url = originLink.takeIf { it.isNotBlank() }?.also {
                        try {
                            val url = Url(it)
                            if (url.protocolOrNull != URLProtocol.HTTPS)  {
                                global.alert("请填写 HTTPS 的原作链接，请勿使用 HTTP")
                                return@launch
                            }
                        } catch (_: URLParserException) {
                            global.alert("请填写正确的 HTTPS 格式的原作链接 https://xxxx")
                            return@launch
                        }
                    },
                    artist = originArtist.takeIf { it.isNotBlank() },
                    originType = 0
                ) else null,
                derivativeInfo = if (creationType > 1) SongModule.CreationTypeInfo(
                    songDisplayId = deriveId.takeIf { it.isNotBlank() },
                    title = deriveTitle.takeIf { it.isNotBlank() },
                    url = deriveLink.takeIf { it.isNotBlank() }?.also {
                        try {
                            val url = Url(it)
                            if (url.protocolOrNull != URLProtocol.HTTPS)  {
                                global.alert("请填写 HTTPS 的原作链接，请勿使用 HTTP")
                                return@launch
                            }
                        } catch (_: URLParserException) {
                            global.alert("请填写正确的 HTTPS 格式的原作链接 https://xxxx")
                            return@launch
                        }
                    },
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

            val resp = api.songModule.publish(
                SongModule.PublishReq(
                    songTempId = audioTempId!!,
                    coverTempId = coverTempId!!,
                    title = title,
                    subtitle = subtitle,
                    description = description,
                    lyrics = lyrics.takeIf { lyricsType != 2 } ?: "",
                    tagIds = tags.map { it.id },
                    creationInfo = creationInfo,
                    productionCrew = crew,
                    externalLinks = externalLinks,
                    explicit = explicit,
                    jmid = "JM-${jmidPrefix!!}-${jmidNumber!!}",
                    comment = null,
                )
            )
            if (resp.ok) {
                val data = resp.okData<SongModule.PublishResp>()
                publishedSongId = data.songDisplayId
                showSuccessDialog = true
                clearInput()
            } else {
                val data = resp.errData<CommonError>()
                global.alert(data.msg)
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
                    global.alert("请输入正确的 UID")
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
                    global.alert("获取用户信息失败")
                    return@launch
                } finally {
                    addStaffOperating = false
                }
            }

            staffs.add(CrewItem(addStaffRole, addStaffUid.toLongOrNull(), addStaffName))
            addStaffOperating = false
            showAddStaffDialog = false
        }
    }

    fun removeStaff(index: Int) {
        staffs.removeAt(index)
    }

    private fun validateInputs(): Boolean {
        if (audioTempId == null) {
            global.alert("请选择音频文件")
            return false
        }

        if (coverTempId == null) {
            global.alert("请上传封面")
            return false
        }

        if (title.isBlank()) {
            global.alert("请填写标题")
            return false
        }

        if (subtitle.isBlank()) {
            // Do nothing
        }
        if (subtitle.length > 32) {
            global.alert("副标题过长")
            return false
        }

        if (description.isBlank()) {
            // Do nothing
        }

        if (lyricsType != 2 && lyrics.isBlank()) {
            global.alert("请填写歌词")
            return false
        }

        if (lyricsType == 0) {
            val lines = try {
                LrcParser.parse(lyrics)
            } catch (e: Throwable) {
                global.alert("请填写正确的 LRC 格式歌词")
                return false
            }
            if (lines.isEmpty()) {
                global.alert("请填写正确的 LRC 格式歌词")
                return false
            }
        }

        if (creationType > 0) {
            if (originId.isBlank() && originTitle.isBlank()) {
                global.alert("请填写原作信息")
                return false
            }
        }

        if (creationType > 1) {
            if (deriveId.isBlank() && deriveTitle.isBlank()) {
                global.alert("请填写二作信息")
                return false
            }
        }

        if (explicit == null) {
            global.alert("请选择是否包含露骨内容")
            return false
        }

        if (jmidPrefix == null || jmidNumber == null) {
            global.alert("请设置基米ID")
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
                    checkJmidPrefixJob?.cancelAndJoin()
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
                                        initJmidValid = true
                                        initJmidSupportText = "该前缀已被使用，如被抢占请联系维护者"
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
            initJmidSupportText = "请填写 3 到 4 位字母"
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
            jmidSupportText = "未设置基米ID前缀"
            jmidValid = false
            return
        }

        val jmidFull = "JM-$prefix-$mappedInput"

        if (Regex("\\d{3}").matches(mappedInput)) {
            viewModelScope.launch {
                checkJmidMutex.withLock {
                    checkJmidJob?.cancelAndJoin()
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
                                        jmidSupportText = "该基米ID已被使用"
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
            jmidSupportText = "数字段必须为 3 位，如 001"
        }
    }
}