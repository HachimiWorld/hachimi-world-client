package world.hachimi.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_empty
import hachimiworld.composeapp.generated.resources.home_category_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.CategorySongsViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.fromSearchSongItem
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.ui.component.PageHeading
import world.hachimi.app.ui.component.PageTitleAction
import world.hachimi.app.ui.component.PlayAllCovers
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.component.titleActionPlaced
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.ui.util.AdaptiveListSpacing
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.InitStatusScaffold
import world.hachimi.app.ui.home.components.SongCardGridSkeleton
import world.hachimi.app.ui.util.ListTailSpacer
import world.hachimi.app.ui.util.calculateGridColumns
import world.hachimi.app.ui.util.contentPaddingForMaxWidth

@Composable
fun CategorySongsScreen(
    category: String,
    vm: CategorySongsViewModel = koinViewModel(),
    global: GlobalStore = koinInject(),
) {
    val navigator = LocalNavigator.current

    DisposableEffect(category, vm) {
        vm.mounted(category)
        onDispose { vm.unmount() }
    }
    ScreenScaffold(
        title = { Text(category, maxLines = 1) },
        showBack = true,
        onBack = navigator::back,
        headingInPageOnWide = true
    ) {
        InitStatusScaffold(
            initializeStatus = vm.initializeStatus,
            isLoading = vm.loading,
            onRetryClick = { vm.retry() },
            initPage = { SongCardGridSkeleton(playAll = !titleActionPlaced) },
        ) {
            Content(category, vm, global)
        }
    }
}

@Composable
private fun Content(category: String, vm: CategorySongsViewModel, global: GlobalStore) {
    // The fan of covers plays them all: by the title on phones, in the header row on wide screens
    val covers = remember(vm.songs) { vm.songs.take(3).map { it.coverArtUrl } }
    val playAll: () -> Unit = { global.player.playAll(vm.songs.map { GlobalStore.MusicQueueItem.fromSearchSongItem(it) }) }
    val playAllInTitle = vm.songs.isNotEmpty() && PageTitleAction { folded ->
        PlayAllCovers(covers = covers, folded = folded, onClick = playAll)
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (vm.songs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(Res.string.common_empty))
        } else LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = calculateGridColumns(maxWidth),
            contentPadding = contentPaddingForMaxWidth(PaddingValues(AdaptiveScreenMargin), maxWidth),
            horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PageHeading(
                    // On phones the scaffold shows the title large, and the fan by it
                    title = if (titleActionPlaced) null else category,
                    caption = stringResource(Res.string.home_category_title),
                    action = if (playAllInTitle) null else {
                        { PlayAllCovers(covers = covers, folded = false, onClick = playAll) }
                    },
                )
            }
            items(vm.songs, key = { item -> item.id }) { item ->
                SongCard(
                    modifier = Modifier.width(width = 180.dp),
                    coverUrl = item.coverArtUrl,
                    title = item.title,
                    subtitle = item.subtitle,
                    author = item.uploaderName,
                    tags = remember { emptyList<String>() },
                    playCount = item.playCount,
                    likeCount = item.likeCount,
                    explicit = item.explicit,
                    onClick = {
                        global.player.insertToQueue(
                            GlobalStore.MusicQueueItem.fromSearchSongItem(item),
                            true,
                            false
                        )
                    },
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                ListTailSpacer()
            }
        }
    }
}
