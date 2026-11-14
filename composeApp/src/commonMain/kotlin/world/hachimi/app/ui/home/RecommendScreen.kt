package world.hachimi.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_empty
import hachimiworld.composeapp.generated.resources.home_recommend_subtitle
import hachimiworld.composeapp.generated.resources.home_recommend_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.RecommendViewModel
import world.hachimi.app.model.fromPublicDetail
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.ui.component.PageHeading
import world.hachimi.app.ui.component.PageTitleAction
import world.hachimi.app.ui.component.PlayAllCovers
import world.hachimi.app.ui.component.headingListPadding
import world.hachimi.app.ui.component.titleActionPlaced
import world.hachimi.app.ui.util.loadStateTransition
import world.hachimi.app.ui.home.components.SongCardGridSkeleton
import world.hachimi.app.ui.component.ErrorPage
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.ui.util.AdaptiveListSpacing
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.calculateGridColumns
import world.hachimi.app.ui.util.contentPaddingForMaxWidth
import world.hachimi.app.ui.util.listHeadInsetsSpacerItem
import world.hachimi.app.ui.util.listTailSpacerItem

@Composable
fun RecommendScreen(
    vm: RecommendViewModel = koinViewModel(),
    global: GlobalStore = koinInject(),
) {
    val navigator = LocalNavigator.current

    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.unmount() }
    }
    ScreenScaffold(
        title = { Text(stringResource(Res.string.home_recommend_title), maxLines = 1) },
        showBack = true,
        onBack = navigator::back,
        headingInPageOnWide = true,
    ) {
        AnimatedContent(
            vm.initializeStatus,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = { loadStateTransition() },
        ) {
            when (it) {
                InitializeStatus.INIT -> SongCardGridSkeleton(playAll = !titleActionPlaced)
                InitializeStatus.FAILED -> ErrorPage(onRetry = { vm.retry() })
                InitializeStatus.LOADED -> Content(vm, global)
            }
        }
    }
}

@Composable
private fun Content(vm: RecommendViewModel, global: GlobalStore) {
    // The fan of covers plays them all: by the title on phones, in the header row on wide screens
    val covers = remember(vm.songs) { vm.songs.take(3).map { it.coverUrl } }
    val playAll: () -> Unit = { global.player.playAll(vm.songs.map { GlobalStore.MusicQueueItem.fromPublicDetail(it) }) }
    val headingInScaffold = titleActionPlaced
    val playAllInTitle = vm.songs.isNotEmpty() && PageTitleAction { folded ->
        PlayAllCovers(covers = covers, folded = folded, onClick = playAll)
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (vm.songs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(Res.string.common_empty))
        } else LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = calculateGridColumns(maxWidth),
            contentPadding = contentPaddingForMaxWidth(headingListPadding(headingInPage = !headingInScaffold), maxWidth),
            horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing)
        ) {
            listHeadInsetsSpacerItem()
            item(span = { GridItemSpan(maxLineSpan) }) {
                PageHeading(
                    // On phones the scaffold shows the title large, and the fan by it
                    title = if (headingInScaffold) null else stringResource(Res.string.home_recommend_title),
                    caption = stringResource(Res.string.home_recommend_subtitle),
                    action = if (playAllInTitle) null else {
                        { PlayAllCovers(covers = covers, folded = false, onClick = playAll) }
                    },
                )
            }
            items(vm.songs, key = { item -> item.id }) { item ->
                SongCard(
                    modifier = Modifier.fillMaxWidth(),
                    item = item,
                    onClick = {
                        global.player.insertToQueue(
                            GlobalStore.MusicQueueItem.fromPublicDetail(item),
                            true,
                            false
                        )
                    },
                )
            }
            listTailSpacerItem()
        }
    }
}