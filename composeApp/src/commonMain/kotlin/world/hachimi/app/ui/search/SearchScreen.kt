package world.hachimi.app.ui.search

import world.hachimi.app.ui.component.UserTileSkeleton
import world.hachimi.app.ui.component.MediaRowSkeleton
import world.hachimi.app.ui.component.ErrorPage
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_sort_cd
import hachimiworld.composeapp.generated.resources.search_no_results
import hachimiworld.composeapp.generated.resources.search_result_title
import hachimiworld.composeapp.generated.resources.search_tab_playlists
import hachimiworld.composeapp.generated.resources.search_tab_songs
import hachimiworld.composeapp.generated.resources.search_tab_users
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.SearchViewModel
import world.hachimi.app.model.fromSearchSongItem
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Navigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.component.TitleStyle
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.DropdownMenu
import world.hachimi.app.ui.design.components.DropdownMenuItem
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.TabBar
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.search.components.SearchPlaylistItem
import world.hachimi.app.ui.search.components.SearchSongItem
import world.hachimi.app.ui.search.components.SearchUserItem
import world.hachimi.app.ui.util.AdaptiveListSpacing
import world.hachimi.app.ui.util.contentPaddingForMaxWidth
import world.hachimi.app.ui.util.listTailSpacerItem

@Composable
fun SearchScreen(
    query: String,
    searchType: SearchViewModel.SearchType,
    vm: SearchViewModel = koinViewModel(),
) {
    val global = koinInject<GlobalStore>()
    val navigator = LocalNavigator.current
    DisposableEffect(vm, query, searchType) {
        vm.mounted(query, searchType)
        onDispose {
            vm.dispose()
        }
    }

    ScreenScaffold(

        titleStyle = TitleStyle.Compact,
        title = { Text(stringResource(Res.string.search_result_title), maxLines = 1) },
        subtitle = if (!vm.loading && !vm.failed) {
            { Text("${vm.searchProcessingTimeMs} ms", maxLines = 1) }
        } else null,
        showBack = true,
        onBack = navigator::back,
    ) {
        // The tabs stay put while a search runs; only the results below them turn into stand-ins
        Content(vm, global, navigator)
    }
}

@Composable
private fun Content(vm: SearchViewModel, global: GlobalStore, navigator: Navigator) {
    BoxWithConstraints {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = if (vm.searchType == SearchViewModel.SearchType.USER) GridCells.Adaptive(152.dp)
            else GridCells.Adaptive(minSize = 320.dp),
            contentPadding = contentPaddingForMaxWidth(PaddingValues(AdaptiveListSpacing), maxWidth),
            horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
            verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Tab(
                    searchType = vm.searchType,
                    onTypeChange = { vm.updateSearchType(it) },
                    sortMethod = vm.songSortMethod,
                    onSortMethodChange = { vm.updateSortMethod(it) },
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                )
            }

            when {
                vm.loading -> searchSkeleton(vm.searchType)
                vm.failed -> item(key = "failed", span = { GridItemSpan(maxLineSpan) }) {
                    ErrorPage(
                        onRetry = { vm.search() },
                        modifier = Modifier.fillMaxWidth().height(FailedHeight).animateItem(ResultFadeIn, null, ResultFadeOut),
                    )
                }
                else -> {
                    val showEmpty = when (vm.searchType) {
                        SearchViewModel.SearchType.SONG -> vm.songData.isEmpty()
                        SearchViewModel.SearchType.USER -> vm.userData.isEmpty()
                        SearchViewModel.SearchType.PLAYLIST -> vm.playlistData.isEmpty()
                        else -> false
                    }

                    if (showEmpty) item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            Modifier.fillMaxWidth().padding(vertical = 128.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(Res.string.search_no_results))
                        }
                    }

                    when (vm.searchType) {
                        SearchViewModel.SearchType.SONG -> items(
                            items = vm.songData,
                            key = { item -> item.info.id },
                            contentType = { _ -> "song" }
                        ) { item ->
                            SearchSongItem(
                                modifier = Modifier.fillMaxWidth().animateItem(ResultFadeIn, null, ResultFadeOut),
                                data = item,
                                onClick = {
                                    global.player.insertToQueue(
                                        GlobalStore.MusicQueueItem.fromSearchSongItem(item.info),
                                        true,
                                        false
                                    )
                                }
                            )
                        }
                        SearchViewModel.SearchType.USER -> items(
                            items = vm.userData,
                            key = { item -> item.uid },
                            contentType = { _ -> "user" }
                        ) { item ->
                            SearchUserItem(
                                modifier = Modifier.fillMaxWidth().animateItem(ResultFadeIn, null, ResultFadeOut),
                                name = item.username,
                                avatarUrl = item.avatarUrl,
                                onClick = { navigator.push(Route.Root.PublicUserSpace(item.uid)) },
                            )
                        }
                        SearchViewModel.SearchType.ALBUM -> {}
                        SearchViewModel.SearchType.PLAYLIST -> items(
                            items = vm.playlistData,
                            key = { it.id },
                            contentType = { _ -> "playlist" }
                        ) { item ->
                            SearchPlaylistItem(
                                modifier = Modifier.fillMaxWidth().animateItem(ResultFadeIn, null, ResultFadeOut),
                                title = item.name,
                                username = item.userName,
                                coverUrl = item.coverUrl,
                                avatarUrl = item.userAvatarUrl,
                                songCount = item.songsCount,
                                onClick = { navigator.push(Route.Root.PublicPlaylist(item.id)) },
                                description = item.description
                            )
                        }
                    }
                }
            }

            listTailSpacerItem()
        }
    }
}

private enum class Tabs(
    val type: SearchViewModel.SearchType,
    val label: StringResource
) {
    SONG(SearchViewModel.SearchType.SONG, Res.string.search_tab_songs),
    USER(SearchViewModel.SearchType.USER, Res.string.search_tab_users),
    PLAYLIST(SearchViewModel.SearchType.PLAYLIST, Res.string.search_tab_playlists)
}

@Composable
private fun Tab(
    searchType: SearchViewModel.SearchType,
    onTypeChange: (SearchViewModel.SearchType) -> Unit,
    sortMethod: SearchViewModel.SortMethod,
    onSortMethodChange: (SearchViewModel.SortMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = Tabs.entries.indexOfFirst { it.type == searchType }.coerceAtLeast(0)
    val tabLabels = Tabs.entries.map { stringResource(it.label) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabBar(
            tabs = tabLabels,
            selectedIndex = selectedIndex,
            onTabSelected = { index -> onTypeChange(Tabs.entries[index].type) },
            maxLines = 1,
        )

        if (searchType == SearchViewModel.SearchType.SONG) {
            var expanded by remember { mutableStateOf(false) }

            Box(Modifier.weight(1f).wrapContentWidth(align = Alignment.End)) {
                Button(onClick = { expanded = true }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = stringResource(Res.string.common_sort_cd))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(sortMethod.labelRes))
                }
                DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                    SearchViewModel.SortMethod.entries.fastForEach {
                        DropdownMenuItem(
                            text = { Text(stringResource(it.labelRes)) },
                            onClick = {
                                onSortMethodChange(it)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Stand-ins for the results of [type] while a search runs, in the shape of its rows or tiles. */
private fun LazyGridScope.searchSkeleton(type: SearchViewModel.SearchType) {
    if (type == SearchViewModel.SearchType.USER) {
        items(SkeletonCount, key = { "skeleton_$it" }, contentType = { "skeleton_user" }) {
            UserTileSkeleton(Modifier.fillMaxWidth().animateItem(ResultFadeIn, null, ResultFadeOut))
        }
    } else {
        items(SkeletonCount, key = { "skeleton_$it" }, contentType = { "skeleton_row" }) {
            MediaRowSkeleton(Modifier.animateItem(ResultFadeIn, null, ResultFadeOut), index = it)
        }
    }
}

// The fade through of the other loading pages, done per item since the tabs above don't change:
// the old rows go over the first 35% and the new ones come in over the rest
private val ResultFadeOut = tween<Float>(105, easing = FastOutLinearInEasing)
private val ResultFadeIn = tween<Float>(195, delayMillis = 105, easing = LinearOutSlowInEasing)
private val FailedHeight = 360.dp
private const val SkeletonCount = 10
