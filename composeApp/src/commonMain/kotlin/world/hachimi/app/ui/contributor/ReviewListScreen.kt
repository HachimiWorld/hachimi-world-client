package world.hachimi.app.ui.contributor

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.ReviewViewModel
import world.hachimi.app.ui.component.Pagination
import world.hachimi.app.ui.component.ReloadPage
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
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp), Arrangement.spacedBy(16.dp)) {
        Text("审核作品 (${vm.total})", style = MaterialTheme.typography.titleLarge)

        Box(Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn {
                itemsIndexed(vm.items, key = { _, item -> item.reviewId }) { index, item ->
                    Item(
                        modifier = Modifier.fillMaxWidth(),
                        coverUrl = item.coverUrl,
                        title = item.title,
                        subtitle = item.subtitle,
                        artist = item.artist,
                        submitTime = item.submitTime,
                        status = item.status,
                        onClick = { vm.detail(item) }
                    )
                }
            }
            if (vm.loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
        }

        // Pagination
        Pagination(
            modifier = Modifier.fillMaxWidth(),
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
private fun Item(
    coverUrl: String,
    title: String,
    subtitle: String,
    artist: String,
    submitTime: Instant,
    status: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(Modifier.size(42.dp), MaterialTheme.shapes.small, LocalContentColor.current.copy(0.12f)) {
            AsyncImage(
                modifier = Modifier.size(42.dp).clip(MaterialTheme.shapes.small),
                model = coverUrl,
                contentDescription = "Cover Image",
                contentScale = ContentScale.Crop
            )
        }

        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "提交时间", style = MaterialTheme.typography.bodySmall)
            Text(
                text = formatTime(submitTime, distance = true, precise = false, fullFormat = LocalDateTime.Formats.YMD),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "作者", style = MaterialTheme.typography.bodySmall)
            Text(text = artist, style = MaterialTheme.typography.bodySmall)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "状态", style = MaterialTheme.typography.bodySmall)
            Text(
                text = when (status) {
                    PublishModule.SongPublishReviewBrief.STATUS_PENDING -> "待审核"
                    PublishModule.SongPublishReviewBrief.STATUS_APPROVED -> "通过"
                    PublishModule.SongPublishReviewBrief.STATUS_REJECTED -> "退回"
                    else -> "未知"
                }, style = MaterialTheme.typography.bodySmall
            )
        }

        /*Box {
            var expanded by remember { mutableStateOf(false) }
            TextButton(onClick = { expanded = true }) {
                Text("操作")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("编辑") }, onClick = onEditClick)
            }
        }*/
    }
}