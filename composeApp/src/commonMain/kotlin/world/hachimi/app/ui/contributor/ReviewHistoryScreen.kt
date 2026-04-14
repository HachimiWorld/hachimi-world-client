package world.hachimi.app.ui.contributor

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.review_history_empty
import hachimiworld.composeapp.generated.resources.review_history_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.ReviewHistoryViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.Pagination
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.AdaptiveScreenMargin
import world.hachimi.app.util.fillMaxWidthIn
import world.hachimi.app.util.formatTime

@Composable
fun ReviewHistoryScreen(
    reviewId: Long,
    vm: ReviewHistoryViewModel = koinViewModel(),
    global: GlobalStore = koinInject(),
) {
    val navigator = LocalNavigator.current

    DisposableEffect(vm, reviewId) {
        vm.mounted(reviewId)
        onDispose { vm.dispose() }
    }

    AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) { status ->
        when (status) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> Content(vm = vm, global = global, navigator = navigator)
        }
    }
}

@Composable
private fun Content(
    vm: ReviewHistoryViewModel,
    global: GlobalStore,
    navigator: world.hachimi.app.nav.Navigator,
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .navigationBarsPadding()
            .padding(LocalContentInsets.current.asPaddingValues())
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = AdaptiveScreenMargin).fillMaxWidthIn(),
            text = stringResource(Res.string.review_history_title),
            style = MaterialTheme.typography.titleLarge
        )

        Box(Modifier.weight(1f)) {
            if (vm.items.isEmpty() && !vm.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.review_history_empty))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vm.items, key = { it.id }) { item ->
                        ReviewHistoryItemCard(
                            modifier = Modifier.padding(horizontal = AdaptiveScreenMargin).fillMaxWidthIn(),
                            item = item,
                            onNavToUserClick = { uid -> navigator.push(Route.Root.PublicUserSpace(uid)) }
                        )
                    }
                }
            }

            if (vm.loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }

        if (vm.total > vm.pageSize) {
            Pagination(
                modifier = Modifier.padding(horizontal = AdaptiveScreenMargin).fillMaxWidthIn(),
                total = vm.total.toInt(),
                currentPage = vm.currentPage,
                pageSize = vm.pageSize,
                pageSizes = remember { listOf(10, 20, 30, 50) },
                pageSizeChange = { vm.updatePageSize(it) },
                pageChange = { vm.goToPage(it) }
            )
        }
    }
}

@Composable
private fun ReviewHistoryItemCard(
    item: PublishModule.ReviewHistoryItem,
    onNavToUserClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = item.note ?: "无备注",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = formatTime(item.createTime, distance = true, precise = false, thresholdDay = 3),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}