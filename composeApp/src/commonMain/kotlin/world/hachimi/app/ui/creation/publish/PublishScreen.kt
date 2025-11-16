package world.hachimi.app.ui.creation.publish

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.getPlatform
import world.hachimi.app.model.PublishViewModel
import world.hachimi.app.ui.creation.publish.components.FormItem
import world.hachimi.app.ui.creation.publish.components.InitJmidDialog
import world.hachimi.app.ui.creation.publish.components.TagEdit
import world.hachimi.app.util.formatSongDuration
import world.hachimi.app.util.singleLined
import kotlin.time.Duration.Companion.seconds

@Composable
fun PublishScreen(
    vm: PublishViewModel = koinViewModel(),
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }

    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier.fillMaxWidth().wrapContentWidth().widthIn(max = 700.dp).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("发布作品", style = MaterialTheme.typography.headlineSmall)

            Text(
                "尊重劳动成果，请勿搬运作品。暂不收录时长或结构明显短于 TV Size 的作品。",
                style = MaterialTheme.typography.bodyMedium
            )

            FormItem(
                header = { Text("上传音频*") },
                subtitle = { Text("支持 flac 和 mp3 格式，大小不超过 20MB") }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(onClick = { vm.setAudioFile() }, enabled = !vm.audioUploading) {
                        Text("选择文件")
                    }

                    if (vm.audioUploading) {
                        if (vm.audioUploadProgress == 0f || vm.audioUploadProgress == 1f) CircularProgressIndicator()
                        else CircularProgressIndicator(progress = { vm.audioUploadProgress })
                    }

                    if (vm.audioUploaded) {
                        Text(
                            text = "${vm.audioFileName}\n${formatSongDuration(vm.audioDurationSecs.seconds)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            FormItem(
                header = { Text("设置封面*") },
                subtitle = {
                    Text("支持 jpg, png, webp 格式的图片。封面在所有地方都只会以裁剪的方式显示为正方形，如果您的封面原先是长方形，建议进行适当的调整。请勿通过拉伸比例的方式来调整，请勿使用透明图片。")
                }
            ) {
                Card(
                    modifier = Modifier.size(200.dp),
                    onClick = { vm.setCoverImage() },
                    enabled = !vm.coverImageUploading
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = vm.coverImage,
                            contentDescription = "Cover Image",
                            modifier = Modifier.fillMaxSize(),
                            filterQuality = FilterQuality.Medium,
                            contentScale = ContentScale.Crop
                        )
                        if (vm.coverImageUploading) {
                            if (vm.coverImageUploadProgress == 0f || vm.coverImageUploadProgress == 1f) CircularProgressIndicator()
                            else CircularProgressIndicator(progress = { vm.coverImageUploadProgress })
                        }
                    }
                }
            }

            FormItem(
                header = { Text("基米ID*") }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val prefix = vm.jmidPrefix
                    if (prefix == null) {
                        TextButton(onClick = { vm.showJmidDialog() }) {
                            Text("设置前缀")
                        }
                    } else {
                        TextField(
                            modifier = Modifier.width(300.dp),
                            value = vm.jmidNumber ?: "",
                            onValueChange = vm::updateJmidNumber,
                            singleLine = true,
                            leadingIcon = {
                                Text(
                                    modifier = Modifier.padding(start = 24.dp),
                                    text = "JM - $prefix - "
                                )
                            },
                            trailingIcon = {
                                when(vm.jmidValid) {
                                    true -> Icon(Icons.Default.CheckCircle, contentDescription = "Available", tint = MaterialTheme.colorScheme.primary)
                                    false -> Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                                    null -> if (vm.jmidNumber != null) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 1.dp)
                                }
                            },
                            supportingText = {
                                Text(vm.jmidSupportText ?: "")
                            }
                        )
                    }
                }
            }

            FormItem(
                header = { Text("标题*") },
                subtitle = { Text("填写一个您认为适合永久流传的纯文字标题。如 钢铁雄基4 这类与原曲标题关联性强的纯文字标题。请不要在标题中添加标签、Emoji等复杂内容。请不要使用标题来引流，后续可能会做专门用于推荐的标题。") }
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.title,
                    onValueChange = { vm.title = it.singleLined() },
                    singleLine = true
                )
            }

            FormItem(
                header = { Text("副标题") },
                subtitle = { Text("可选。副标题通常是一句简短的描述，或是 OST 的出处，如《XXX》OP、《XXX》游戏原声带。无需在此处填写原作标题，原作信息请在后方对应的输入框中填写。") }
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.subtitle,
                    onValueChange = { vm.subtitle = it.singleLined() },
                    singleLine = true
                )
            }

            FormItem(
                header = { Text("标签") },
                subtitle = { Text("使用标签描述你的曲风类型（如古典、流行、J-Pop、ACG、R&B）、创作类型（如纯净哈基米、原曲不使用）。不建议添加过多的标签。若只有英文请按照每单词首字母大写空格隔开，或使用行业标准写法。请勿使用符号和 Emoji") }
            ) {
                TagEdit(vm)
            }

            FormItem(
                header = { Text("简介") },
                subtitle = { Text("介绍一下你的作品，编写一段故事，或是描述一下你的创作历程吧") }
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.description,
                    onValueChange = { vm.description = it }
                )
            }

            FormItem(
                header = { Text("歌词") },
                subtitle = {
                    Text("建议使用LRC格式的滚动歌词", modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        getPlatform().openUrl("https://lrc-maker.github.io/")
                    }) {
                        Text("制作工具")
                    }
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = vm.lyricsType == 0, onClick = { vm.lyricsType = 0 })
                    Text("LRC歌词", style = MaterialTheme.typography.labelLarge)

                    RadioButton(selected = vm.lyricsType == 1, onClick = { vm.lyricsType = 1 })
                    Text("文本歌词", style = MaterialTheme.typography.labelLarge)

                    RadioButton(selected = vm.lyricsType == 2, onClick = { vm.lyricsType = 2 })
                    Text("不填写", style = MaterialTheme.typography.labelLarge)
                }

                if (vm.lyricsType == 2) {
                    Text("强烈建议至少使用文本歌词", color = MaterialTheme.colorScheme.error)
                }

                if (vm.lyricsType != 2) OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.lyrics,
                    onValueChange = { vm.lyrics = it },
                    minLines = 8,
                    maxLines = 8,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            FormItem(
                header = { Text("创作类型*") },
                subtitle = { Text("如果你的作品是对现有作品的再创作（如对《D大调卡农》的改编），请选择二创并填写原作信息。如果你的作品是对哈基米音乐的再创作（如翻唱），请选择三创") }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = vm.creationType == 0, onClick = { vm.creationType = 0 })
                    Text("原创", style = MaterialTheme.typography.labelLarge)

                    RadioButton(selected = vm.creationType == 1, onClick = { vm.creationType = 1 })
                    Text("二创", style = MaterialTheme.typography.labelLarge)

                    RadioButton(selected = vm.creationType == 2, onClick = { vm.creationType = 2 })
                    Text("三创", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (vm.creationType == 0) {
                Text(
                    "原创指的是作词、作曲、编曲等全部原创，改编作品请勿选择原创。选择错误会被退回，请再次确认！",
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (vm.creationType > 0) {
                /*FormItem(header = { Text("原作基米ID") }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.originId,
                        onValueChange = { vm.originId = it.singleLined() },
                        singleLine = true,
                        supportingText = { Text("如果原作是基米天堂站内的作品，填写基米 ID 即可，无需再填写标题与链接") }
                    )
                }*/
                FormItem(header = { Text("原作标题*") }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.originTitle,
                        onValueChange = { vm.originTitle = it.singleLined() },
                        singleLine = true,
                        supportingText = { Text("如 D 大调卡农") }
                    )
                }
                FormItem(header = { Text("原作艺术家") }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.originArtist,
                        onValueChange = { vm.originArtist = it.singleLined() },
                        singleLine = true,
                        supportingText = { Text("涉及到多位艺术家的，暂时填写主要的一位歌手即可") }
                    )
                }
                FormItem(header = { Text("原作链接") }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.originLink,
                        onValueChange = { vm.originLink = it.singleLined() },
                        singleLine = true,
                        placeholder = { Text("https://") },
                        supportingText = { Text("建议填写，请使用 https:// 格式的链接") }
                    )
                }
            }

            if (vm.creationType > 1) {
                FormItem(header = { Text("二作基米ID") }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.deriveId,
                        onValueChange = { vm.deriveId = it.singleLined() },
                        singleLine = true,
                        supportingText = { Text("如果二作是站内作品，填写 ID 即可，则无需再填写标题与链接") }
                    )
                }
                FormItem(header = { Text("二作标题*") }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.deriveTitle,
                        onValueChange = { vm.deriveTitle = it.singleLined() },
                        singleLine = true
                    )
                }
                FormItem(header = { Text("二作艺术家") }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.deriveArtist,
                        onValueChange = { vm.deriveArtist = it.singleLined() },
                        singleLine = true
                    )
                }
                FormItem(header = { Text("二作链接") }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.deriveLink,
                        onValueChange = { vm.deriveLink = it.singleLined() },
                        singleLine = true
                    )
                }
            }

            FormItem(
                header = {
                    Text("制作团队")
                    IconButton(onClick = { vm.addStaff() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                },
                subtitle = { Text("如果该作品的制作者不止一人，请在此添加并选择角色（如混音、编曲）。你可以选择站内用户，也可以仅填写他的名字") }
            ) {
                vm.staffs.fastForEachIndexed { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.role,
                            modifier = Modifier.width(120.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.name ?: "uid: ${item.uid}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        IconButton(onClick = { vm.removeStaff(index) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove")
                        }
                    }
                }
            }

            FormItem(
                header = {
                    Text("外部链接")

                    IconButton(onClick = { vm.showAddExternalLinkDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                },
                subtitle = { Text("如果你的作品已发表在其他平台上，请在此添加链接") }
            ) {
                vm.externalLinks.fastForEachIndexed { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            modifier = Modifier.width(120.dp),
                            text = translatePlatformLabel(item.platform),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            modifier = Modifier.weight(1f),
                            text = item.url,
                            overflow = TextOverflow.MiddleEllipsis,
                            style = MaterialTheme.typography.bodySmall
                        )

                        IconButton(onClick = { vm.removeLink(index) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove")
                        }
                    }
                }
            }

            FormItem(
                header = { Text("是否含有露骨内容*") },
                subtitle = { Text("如果作品含有脏话、低俗、暴力等内容，尤其是儿童不宜内容，请务必正确标记") }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = vm.explicit == false, onClick = { vm.explicit = false })
                    Text("全年龄", style = MaterialTheme.typography.labelLarge)

                    RadioButton(selected = vm.explicit == true, onClick = { vm.explicit = true })
                    Text("非全年龄", style = MaterialTheme.typography.labelLarge)
                }
            }

            Button(onClick = { vm.publish() }, enabled = !vm.isOperating) {
                Text("提交作品")
            }
        }
    }

    if (vm.showSuccessDialog) AlertDialog(
        onDismissRequest = { vm.closeDialog() },
        title = {
            Text("作品提交成功")
        },
        text = {
            Text("你的作品编号为：${vm.publishedSongId}。首次发布需要确认您是该作品的作者，请留意相关视频平台的私信。目前由原始贡献者人工审核，可能会很慢，请耐心等待或主动联系我们，感谢您的理解！")
        },
        confirmButton = {
            TextButton(onClick = { vm.closeDialog() }) {
                Text("确定")
            }
        }
    )

    AddStaffDialog(vm)
    AddExternalLinkDialog(vm)

    if (vm.showInitJmidDialog) InitJmidDialog(
        onDismissRequest = vm::cancelInitJmid,
        value = vm.initJmidInput,
        onValueChange = vm::updateInitJmidInput,
        valid = vm.initJmidValid,
        supportText = vm.initJmidSupportText,
        onConfirm = vm::confirmInitJmid
    )
}

@Composable
private fun AddStaffDialog(vm: PublishViewModel) {
    if (vm.showAddStaffDialog) AlertDialog(
        onDismissRequest = { vm.cancelAddStaff() },
        title = {
            Text("添加成员")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                var type by remember { mutableStateOf(0) }

                OutlinedTextField(
                    value = vm.addStaffRole,
                    onValueChange = { vm.addStaffRole = it.singleLined() },
                    label = { Text("角色") },
                    singleLine = true,
                    supportingText = {
                        Text("如：编曲、作词、混音、吉他等")
                    }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(selected = type == 0, onClick = { type = 0 })
                    Text(text = "站内用户")
                    RadioButton(selected = type == 1, onClick = {
                        vm.addStaffUid = ""
                        type = 1
                    })
                    Text(text = "站外艺术家")
                }

                if (type == 0) OutlinedTextField(
                    value = vm.addStaffUid,
                    onValueChange = { vm.addStaffUid = it.singleLined() },
                    label = { Text("UID") },
                    singleLine = true
                )

                if (type == 1) OutlinedTextField(
                    value = vm.addStaffName,
                    onValueChange = { vm.addStaffName = it.singleLined() },
                    label = { Text("名称") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { vm.confirmAddStaff() },
                enabled = !vm.addStaffOperating && (vm.addStaffRole.isNotBlank() && (vm.addStaffUid.isNotBlank() || vm.addStaffName.isNotBlank()))
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.cancelAddStaff() }) {
                Text("取消")
            }
        }
    )
}

private val presetPlatforms = listOf("bilibili", "niconico", "youtube", "douyin")

@Composable
private fun AddExternalLinkDialog(vm: PublishViewModel) {
    if (vm.showAddExternalLinkDialog) {
        var platform by remember { mutableStateOf<String?>(null) }
        var value by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { vm.closeAddExternalLinkDialog() },
            title = {
                Text("添加外部链接")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box {
                        var dropdown by remember { mutableStateOf(false) }
                        TextButton(onClick = { dropdown = true }, modifier = Modifier.width(120.dp)) {
                            Text(
                                platform?.let {
                                    translatePlatformLabel(it)
                                } ?: "选择平台"
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "ArrowDropDown")
                        }
                        fun changePlatform(v: String) {
                            platform = v
                            value = ""
                            dropdown = false
                        }
                        DropdownMenu(dropdown, onDismissRequest = { dropdown = false }) {
                            presetPlatforms.fastForEach {
                                DropdownMenuItem(
                                    text = { Text(translatePlatformLabel(it)) },
                                    onClick = { changePlatform(it) }
                                )
                            }
                        }
                    }
                    if (platform == "bilibili") {
                        OutlinedTextField(
                            modifier = Modifier,
                            value = value,
                            onValueChange = {
                                value = it.singleLined()
                            },
                            label = { Text("BV号") },
                            placeholder = { Text("BV...") },
                            singleLine = true,
                            supportingText = {
                                error?.let { error ->
                                    Text(error, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                    } else if (platform == "niconico") {
                        OutlinedTextField(
                            modifier = Modifier,
                            value = value,
                            onValueChange = { value = it.singleLined() },
                            label = { Text("sm号") },
                            placeholder = { Text("sm...") },
                            singleLine = true,
                            supportingText = {
                                error?.let { error ->
                                    Text(error, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                    } else OutlinedTextField(
                        modifier = Modifier,
                        value = value,
                        onValueChange = { value = it.singleLined() },
                        label = { Text("链接") },
                        placeholder = { Text("https://") },
                        singleLine = true,
                        supportingText = {
                            error?.let { error ->
                                Text(error, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val platform = platform
                        val value = value
                        var link = value

                        if (platform == null) {
                            error = "请选择平台"
                            return@TextButton
                        }

                        when (platform) {
                            "bilibili" -> {
                                if (link.matches("""BV[1-9A-HJ-NP-Za-km-z]{10}""".toRegex())) {
                                    link = "https://www.bilibili.com/video/$value"
                                } else {
                                    error = "请输入正确的 BV 号"
                                    return@TextButton
                                }
                            }

                            "niconico" -> {
                                if (link.matches("""sm\d+""".toRegex())) {
                                    link = "https://www.nicovideo.jp/watch/$value"
                                } else {
                                    error = "请输入正确的 sm 号"
                                    return@TextButton
                                }
                            }

                            else -> {
                                if (!link.startsWith("https://")) {
                                    error = "请使用 https:// 格式的链接"
                                    return@TextButton
                                }
                            }
                        }

                        vm.addLink(platform, link)
                        vm.closeAddExternalLinkDialog()
                    },
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.closeAddExternalLinkDialog() }) {
                    Text("取消")
                }
            }
        )
    }
}

@Stable
@Composable
fun translatePlatformLabel(label: String): String = when (label) {
    // TODO: i18n
    "bilibili" -> "哔哩哔哩"
    "douyin" -> "抖音"
    "youtube" -> "YouTube"
    "niconico" -> "niconico"
    else -> label
}