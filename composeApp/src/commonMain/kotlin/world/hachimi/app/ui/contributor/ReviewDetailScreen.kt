package world.hachimi.app.ui.contributor

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.api.module.PublishModule.SongPublishReviewBrief.Companion.STATUS_PENDING
import world.hachimi.app.getPlatform
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.ReviewDetailViewModel
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.util.formatSongDuration
import world.hachimi.app.util.formatTime
import kotlin.time.Duration.Companion.seconds

@Composable
fun ReviewDetailScreen(
    reviewId: Long,
    vm: ReviewDetailViewModel = koinViewModel()
) {
    DisposableEffect(vm, reviewId) {
        vm.mounted(reviewId)
        onDispose { vm.dispose() }
    }

    AnimatedContent(vm.initializeStatus) {
        when (it) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.refresh() })
            InitializeStatus.LOADED -> Content(vm)
        }
    }
}

@Composable
private fun Content(
    vm: ReviewDetailViewModel,
    global: GlobalStore = koinInject()
) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp), Arrangement.spacedBy(16.dp)) {
        vm.data?.let { data ->
            Text("Review 详情", style = MaterialTheme.typography.titleLarge)
            PropertyItem(label = {
                Text("投稿人")
            }) {
                Text(
                    modifier = Modifier.clickable {
                        global.nav.push(Route.Root.PublicUserSpace(data.uploaderUid))
                    },
                    text = "${data.uploaderName} ${data.uploaderUid}"
                )
            }
            PropertyItem("提交时间", formatTime(data.submitTime, distance = true, precise = false, thresholdDay = 3))
            PropertyItem(
                "状态", when (data.status) {
                    PublishModule.SongPublishReviewBrief.STATUS_PENDING -> "待审核"
                    PublishModule.SongPublishReviewBrief.STATUS_APPROVED -> "通过"
                    PublishModule.SongPublishReviewBrief.STATUS_REJECTED -> "退回"
                    else -> error("unreachable")
                }
            )

            PropertyItem("审核意见", data.reviewComment ?: "null")
            PropertyItem(
                "审核时间",
                data.reviewTime?.let { formatTime(it, distance = true, precise = false, thresholdDay = 3) } ?: "null")

            if (data.status == STATUS_PENDING) Column(Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp)) {
                TextField(
                    value = vm.commentInput,
                    onValueChange = { vm.commentInput = it },
                )
                Row {
                    Button(onClick = { vm.approve() }, enabled = !vm.operating) {
                        Text("通过")
                    }
                    Spacer(Modifier.width(12.dp))
                    TextButton(onClick = { vm.reject() }, enabled = !vm.operating) {
                        Text("退回")
                    }
                }
            }

            Text("作品详情", style = MaterialTheme.typography.titleLarge)

            PropertyItem("基米ID", data.displayId)
            PropertyItem({ Text("音频") }) {
                Button(onClick = { vm.download() }) {
                    Text("点击下载", style = MaterialTheme.typography.labelLarge)
                }
            }
            PropertyItem({ Text("封面") }) {
                Surface(Modifier.size(120.dp)) {
                    AsyncImage(data.coverUrl, null, Modifier.size(120.dp))
                }
            }
            PropertyItem("标题", data.title)
            PropertyItem("副标题", data.subtitle)
            PropertyItem("简介", data.description)
            PropertyItem("时长", formatSongDuration(data.durationSeconds.seconds))
            PropertyItem(
                "创作类型", when (data.creationType) {
                    PublishModule.SongPublishReviewData.CREATION_TYPE_ORIGINAL -> "原创"
                    PublishModule.SongPublishReviewData.CREATION_TYPE_DERIVATION -> "二创"
                    PublishModule.SongPublishReviewData.CREATION_TYPE_DERIVATION_OF_DERIVATION -> "三创"
                    else -> "未知"
                }
            )
            PropertyItem(
                "含有露骨内容", when (data.explicit) {
                    true -> "是"
                    false -> "否"
                    null -> "未标记"
                }
            )
            data.originInfos.fastForEach {
                PropertyItem(
                    "原作类型", when (it.originType) {
                        PublishModule.SongPublishReviewData.CREATION_TYPE_ORIGINAL -> "原作"
                        PublishModule.SongPublishReviewData.CREATION_TYPE_DERIVATION -> "二作"
                        else -> "未知"
                    }
                )
                PropertyItem("原作标题", it.title ?: "空")
                PropertyItem("原作艺术家", it.artist ?: "空")
                PropertyItem({ Text("原作链接") }) {
                    Text(
                        modifier = Modifier.clickable {
                            it.url?.let {
                                getPlatform().openUrl(it)
                            }
                        },
                        text = it.url ?: "空",
                        textDecoration = if (it.url != null) TextDecoration.Underline else null
                    )
                }
            }

            PropertyItem({ Text("标签") }) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    data.tags.fastForEach {
                        Text(it.name)
                    }
                }
            }

            PropertyItem({ Text("制作团队") }) {
                data.productionCrew.fastForEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(it.role, style = MaterialTheme.typography.labelMedium)
                        Text(it.uid.toString())
                        Text(it.personName.toString())
                    }
                }
            }

            PropertyItem("歌词", data.lyrics)

            PropertyItem( { Text("外部链接") }) {
                data.externalLink.fastForEach {
                    Text(it.platform, style = MaterialTheme.typography.labelMedium)

                    SelectionContainer {
                        Text(it.url, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyItem(
    label: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
            label()
        }
        Spacer(Modifier.height(8.dp))
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall.copy(color = LocalContentColor.current.copy(0.7f))) {
            content()
        }
    }
}

@Composable
private fun PropertyItem(
    label: String,
    content: String
) {
    PropertyItem(label = { Text(label) }, content = { Text(content) })
}