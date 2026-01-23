package world.hachimi.app.ui.contributor

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.datetime.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.ReviewViewModel
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.Pagination
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime
import kotlin.time.Instant

@Composable
fun ReviewListScreen(
    vm: ReviewViewModel = koinViewModel()
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose {
            vm.dispose()
        }
    }

    AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) {
        when (it) {
            InitializeStatus.INIT -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }

            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> Box(Modifier.fillMaxSize()) {
                if (vm.isContributor) Content(vm)
                else NotContributor()
            }
        }
    }

}

@Preview
@Composable
private fun NotContributor() {
    Text(
        modifier = Modifier.fillMaxSize().wrapContentSize(),
        text = "你还不是贡献者，为社区作出贡献，解锁更多功能吧"
    )
}

@Composable
private fun Content(vm: ReviewViewModel) {
    Column(
        Modifier.fillMaxSize()
            .navigationBarsPadding()
            .padding(LocalContentInsets.current.asPaddingValues())
            .padding(vertical = 24.dp),
        Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = "审核作品 (${vm.total})",
            style = MaterialTheme.typography.titleLarge
        )

        Box(Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn {
                items(vm.items, key = { item -> item.reviewId }) { item ->
                    ReviewItem(
                        modifier = Modifier.fillMaxWidth(),
                        coverUrl = item.coverUrl,
                        title = item.title,
                        subtitle = item.subtitle,
                        submitTime = item.submitTime,
                        status = item.status,
                        onClick = { vm.detail(item) },
                        id = item.reviewId,
                        type = item.type
                    )
                }
            }
            if (vm.loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
        }

        // Pagination
        Pagination(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            total = vm.total.toInt(),
            currentPage = vm.currentPage,
            pageSize = vm.pageSize,
            pageSizes = remember { listOf(10, 20, 30) },
            pageSizeChange = { vm.updatePageSize(it) },
            pageChange = { vm.goToPage(it) }
        )
    }
}

@Composable
private fun ReviewItem(
    id: Long,
    coverUrl: String,
    title: String,
    subtitle: String,
    submitTime: Instant,
    status: Int,
    type: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(Modifier.size(42.dp), RoundedCornerShape(8.dp), LocalContentColor.current.copy(0.12f)) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = coverUrl,
                contentDescription = "Cover Image",
                contentScale = ContentScale.Crop
            )
        }

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Spacer(Modifier.width(8.dp))
                Text(text = "#$id", style = MaterialTheme.typography.labelSmall)
            }
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "类型", style = MaterialTheme.typography.bodySmall)
            Text(
                text = when (type) {
                    PublishModule.SongPublishReviewBrief.TYPE_CREATION -> "发布"
                    PublishModule.SongPublishReviewBrief.TYPE_MODIFICATION -> "编辑"
                    else -> "未知"
                }, style = MaterialTheme.typography.bodySmall
            )

        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "提交时间", style = MaterialTheme.typography.bodySmall)
            Text(
                text = formatTime(
                    submitTime,
                    distance = true,
                    precise = false,
                    fullFormat = LocalDateTime.Formats.YMD
                ), style = MaterialTheme.typography.bodySmall
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "状态", style = MaterialTheme.typography.bodySmall)
            Text(
                text = when (status) {
                    PublishModule.SongPublishReviewBrief.STATUS_PENDING -> "待审核"
                    PublishModule.SongPublishReviewBrief.STATUS_APPROVED -> "通过"
                    PublishModule.SongPublishReviewBrief.STATUS_REJECTED -> "退回"
                    else -> "未知"
                }, style = MaterialTheme.typography.bodySmall,
                color = when (status) {
                    PublishModule.SongPublishReviewBrief.STATUS_PENDING -> Color.Yellow.copy(alpha = 0.7f)
                    PublishModule.SongPublishReviewBrief.STATUS_REJECTED -> MaterialTheme.colorScheme.error
                    else -> LocalContentColor.current
                }
            )
        }
    }
}