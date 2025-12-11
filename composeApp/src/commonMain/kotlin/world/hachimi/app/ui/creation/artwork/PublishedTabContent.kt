package world.hachimi.app.ui.creation.artwork

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.datetime.LocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.PublishedTabViewModel
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.Pagination
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime
import kotlin.time.Instant

@Composable
fun PublishedTabContent(
    vm: PublishedTabViewModel = koinViewModel(),
    global: GlobalStore = koinInject()
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }
    AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) {
        when (it) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> Box(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.fillMaxSize()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                    }
                    items(vm.songs, { it.id }) { item ->
                        ArtworkItem(
                            id = item.id,
                            jmid = item.displayId,
                            coverUrl = item.coverUrl,
                            title = item.title,
                            subtitle = item.subtitle,
                            createTime = item.createTime,
                            onClick = { global.nav.push(Route.Root.CreationCenter.ArtworkDetail(item.id)) }
                        )
                    }
                    item {
                        Pagination(
                            Modifier.fillMaxWidth().padding(24.dp),
                            vm.total.toInt(),
                            vm.currentPage,
                            vm.pageSize,
                            pageSizeChange = { vm.setPage(it, vm.currentPage) },
                            pageChange = { vm.setPage(vm.pageSize, it) }
                        )
                    }
                    item {
                        Spacer(Modifier.navigationBarsPadding().windowInsetsBottomHeight(LocalContentInsets.current))
                    }
                }
                if (vm.loading) Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}


@Composable
private fun ArtworkItem(
    id: Long,
    jmid: String,
    coverUrl: String,
    title: String,
    subtitle: String,
    createTime: Instant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = jmid, style = MaterialTheme.typography.labelSmall)
            Row(
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Surface(Modifier.size(42.dp), RoundedCornerShape(8.dp), LocalContentColor.current.copy(0.12f)) {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = coverUrl,
                        contentDescription = "Cover Image",
                        contentScale = ContentScale.Crop
                    )
                }

                Column(Modifier.padding(start = 16.dp)) {
                    Text(text = title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "发布时间", style = MaterialTheme.typography.bodySmall)
            Text(
                text = formatTime(
                    createTime,
                    distance = true,
                    precise = false,
                    fullFormat = LocalDateTime.Formats.YMD
                ), style = MaterialTheme.typography.bodySmall
            )
        }
    }

}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        ArtworkItem(
            id = 0,
            jmid = "JM-ABCD-001",
            coverUrl = "",
            title = "Title",
            subtitle = "Subtitle",
            createTime = remember { Instant.parse("2023-01-01T00:00:00Z") },
            onClick = {}
        )
    }
}