package world.hachimi.app.ui.creation.artwork

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.datetime.LocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.MyArtworkViewModel
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime
import kotlin.time.Instant

@Composable
fun MyArtworkScreen(
    vm: MyArtworkViewModel = koinViewModel()
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }

    val global = koinInject<GlobalStore>()
    val scrollState = rememberLazyListState()

    LaunchedEffect(scrollState.canScrollForward) {
        if (!scrollState.canScrollForward && !vm.loading) {
            vm.loadMore()
        }
    }

    AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) {
        when(it) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry()} )
            InitializeStatus.LOADED -> Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "我的作品 (${vm.total})",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Button(onClick = {
                        global.nav.push(Route.Root.CreationCenter.Publish)
                    }) {
                        Text("发布作品")
                    }
                }

                if (vm.items.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("什么都没有，快发布你的第一个作品吧！")
                    }
                } else LazyColumn(
                    state = scrollState,
                ) {
                    items(vm.items, key = { item -> item.reviewId }) { item ->
                        ReviewItem(
                            modifier = Modifier.fillMaxWidth(),
                            id = item.reviewId,
                            coverUrl = item.coverUrl,
                            title = item.title,
                            subtitle = item.subtitle,
                            submitTime = item.submitTime,
                            status = when (item.status) {
                                PublishModule.SongPublishReviewBrief.STATUS_PENDING -> "待审核"
                                PublishModule.SongPublishReviewBrief.STATUS_APPROVED -> "通过"
                                PublishModule.SongPublishReviewBrief.STATUS_REJECTED -> "退回"
                                else -> "未知"
                            },
                            onClick = {
                                global.nav.push(Route.Root.CreationCenter.ReviewDetail(item.reviewId))
                            }
                        )
                    }
                    item {
                        if (vm.noMoreData) Box(Modifier.fillMaxWidth(), Alignment.Center) {
                            Text(
                                text = "没有更多了",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    item {
                        if (vm.loading) Box(Modifier.fillMaxWidth(), Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewItem(
    id: Long,
    coverUrl: String,
    title: String,
    subtitle: String,
    submitTime: Instant,
    status: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(Modifier.size(42.dp), MaterialTheme.shapes.small, LocalContentColor.current.copy(0.12f)) {
            AsyncImage(
                modifier = Modifier.size(42.dp).clip(MaterialTheme.shapes.small),
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
            Text(text = "提交时间", style = MaterialTheme.typography.bodySmall)
            Text(text = formatTime(submitTime, distance = true, precise = false, fullFormat = LocalDateTime.Formats.YMD), style = MaterialTheme.typography.bodySmall)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "状态", style = MaterialTheme.typography.bodySmall)
            Text(text = status, style = MaterialTheme.typography.bodySmall)
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