package world.hachimi.app.ui.creation.artwork

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.MyPRViewModel
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.component.ReviewItem
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.AdaptiveScreenMargin

@Composable
fun PRTabContent(
    vm: MyPRViewModel = koinViewModel(),
    global: GlobalStore = koinInject(),
    scrollState: LazyListState = rememberLazyListState()
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }
    LaunchedEffect(scrollState.canScrollForward) {
        if (!scrollState.canScrollForward && !vm.loading) {
            vm.loadMore()
        }
    }
    AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) {
        when (it) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(AdaptiveScreenMargin),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "全部提交 (${vm.total})",
                        style = MaterialTheme.typography.titleLarge
                    )
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
                            artist = item.artist,
                            submitTime = item.submitTime,
                            status = item.status,
                            type = item.type,
                            onClick = {
                                global.nav.push(Route.Root.CreationCenter.ReviewDetail(item.reviewId))
                            }
                        )
                    }
                    item {
                        LoadMoreItem(hasMore = !vm.noMoreData, isLoading = vm.loadingMore)
                    }
                    item {
                        Spacer(Modifier.navigationBarsPadding().padding(LocalContentInsets.current.asPaddingValues()))
                    }
                }
            }
        }
    }
}
