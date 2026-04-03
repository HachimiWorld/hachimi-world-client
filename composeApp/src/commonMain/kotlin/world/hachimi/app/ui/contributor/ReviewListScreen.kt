package world.hachimi.app.ui.contributor

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.ReviewViewModel
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.Pagination
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.component.ReviewItem
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.AdaptiveListSpacing
import world.hachimi.app.util.AdaptiveScreenMargin
import world.hachimi.app.util.fillMaxWidthIn

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
            modifier = Modifier.padding(horizontal = AdaptiveListSpacing).fillMaxWidthIn(),
            text = "审核作品 (${vm.total})",
            style = MaterialTheme.typography.titleLarge
        )

        Box(Modifier.weight(1f)) {
            LazyColumn() {
                items(vm.items, key = { item -> item.reviewId }) { item ->
                    ReviewItem(
                        modifier = Modifier.fillMaxWidthIn(),
                        coverUrl = item.coverUrl,
                        title = item.title,
                        subtitle = item.subtitle,
                        artist = item.artist,
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
            modifier = Modifier.padding(horizontal = AdaptiveScreenMargin).fillMaxWidthIn(),
            total = vm.total.toInt(),
            currentPage = vm.currentPage,
            pageSize = vm.pageSize,
            pageSizes = remember { listOf(10, 20, 30) },
            pageSizeChange = { vm.updatePageSize(it) },
            pageChange = { vm.goToPage(it) }
        )
    }
}

