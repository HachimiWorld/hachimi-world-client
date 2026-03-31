package world.hachimi.app.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.changelog_history_empty
import hachimiworld.composeapp.generated.resources.changelog_history_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.module.VersionModule
import world.hachimi.app.model.ChangelogViewModel
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.fillMaxWidthIn

@Composable
fun ChangelogScreen(
    vm: ChangelogViewModel = koinViewModel()
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(listState.canScrollForward, vm.loading, vm.loadingMore, vm.noMoreData) {
        if (!vm.loading && !vm.loadingMore && !vm.noMoreData && !listState.canScrollForward) {
            vm.loadMore()
        }
    }

    AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) {
        when (it) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> {
                if (vm.items.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(stringResource(Res.string.changelog_history_empty))
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().fillMaxWidthIn(),
                        contentPadding = PaddingValues(vertical = 24.dp)
                    ) {
                        item {
                            Text(
                                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                                text = stringResource(Res.string.changelog_history_title),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        items(vm.items, key = { "${it.variant}-${it.versionNumber}" }) { version ->
                            ChangelogItem(version)
                        }
                        item {
                            LoadMoreItem(hasMore = !vm.noMoreData, isLoading = vm.loadingMore)
                        }
                        item {
                            Spacer(
                                Modifier
                                    .navigationBarsPadding()
                                    .padding(LocalContentInsets.current.asPaddingValues())
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangelogItem(version: VersionModule.LatestVersionResp) {
    val isCurrent = version.versionNumber == BuildKonfig.VERSION_CODE
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                version.versionName,
                style = MaterialTheme.typography.titleMedium,
                color = if (isCurrent) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            if (isCurrent) {
                Text(
                    "← current",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            version.releaseTime.toString().substringBefore('T'),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            version.changelog,
            style = MaterialTheme.typography.bodyMedium
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
}

