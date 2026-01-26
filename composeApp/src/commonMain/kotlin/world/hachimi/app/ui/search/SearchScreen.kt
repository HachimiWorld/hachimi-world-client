package world.hachimi.app.ui.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
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
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.search_no_results
import hachimiworld.composeapp.generated.resources.search_result_title
import hachimiworld.composeapp.generated.resources.search_tab_playlists
import hachimiworld.composeapp.generated.resources.search_tab_songs
import hachimiworld.composeapp.generated.resources.search_tab_users
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import soup.compose.material.motion.animation.materialFadeThrough
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.SearchViewModel
import world.hachimi.app.model.fromSearchSongItem
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.search.components.SearchPlaylistItem
import world.hachimi.app.ui.search.components.SearchSongItem
import world.hachimi.app.ui.search.components.SearchUserItem
import world.hachimi.app.util.AdaptiveListSpacing

@Composable
fun SearchScreen(
    query: String,
    searchType: SearchViewModel.SearchType,
    vm: SearchViewModel = koinViewModel(),
) {
    val global = koinInject<GlobalStore>()
    DisposableEffect(vm, query, searchType) {
        vm.mounted(query, searchType)
        onDispose {
            vm.dispose()
        }
    }

    AnimatedContent(
        targetState = vm.loading,
        transitionSpec = { materialFadeThrough() }
    ) { loading ->
        if (loading) LoadingPage() else Content(vm, global)
    }
}

@Composable
private fun Content(vm: SearchViewModel, global: GlobalStore) {

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = if (vm.searchType == SearchViewModel.SearchType.USER) GridCells.Adaptive(152.dp)
        else GridCells.Adaptive(minSize = 320.dp),
        contentPadding = PaddingValues(24.dp),
        horizontalArrangement = Arrangement.spacedBy(AdaptiveListSpacing),
        verticalArrangement = Arrangement.spacedBy(AdaptiveListSpacing)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Header(vm.searchProcessingTimeMs)
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Tab(
                searchType = vm.searchType,
                onTypeChange = { vm.updateSearchType(it) },
                sortMethod = vm.songSortMethod,
                onSortMethodChange = { vm.updateSortMethod(it) }
            )
        }

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

        if (vm.searchType == SearchViewModel.SearchType.SONG) items(
            items = vm.songData,
            key = { item -> item.info.id },
            contentType = { _ -> "song" }
        ) { item ->
            SearchSongItem(
                modifier = Modifier.fillMaxWidth(),
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

        if (vm.searchType == SearchViewModel.SearchType.USER) items(
            items = vm.userData,
            key = { item -> item.uid },
            contentType = { _ -> "user" }
        ) { item ->
            SearchUserItem(
                modifier = Modifier.fillMaxWidth(),
                name = item.username,
                avatarUrl = item.avatarUrl,
                onClick = { global.nav.push(Route.Root.PublicUserSpace(item.uid)) },
            )
        }

        if (vm.searchType == SearchViewModel.SearchType.PLAYLIST) items(
            items = vm.playlistData,
            key = { it.id },
            contentType = { _ -> "playlist" }
        ) { item ->
            SearchPlaylistItem(
                modifier = Modifier.fillMaxWidth(),
                title = item.name,
                username = item.userName,
                coverUrl = item.coverUrl,
                avatarUrl = item.userAvatarUrl,
                songCount = item.songsCount,
                onClick = { global.nav.push(Route.Root.PublicPlaylist(item.id)) },
                description = item.description
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(
                Modifier.navigationBarsPadding()
                    .padding(LocalContentInsets.current.asPaddingValues())
            )
        }
    }
}

@Composable
private fun Header(processTimeMillis: Long) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(Res.string.search_result_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$processTimeMillis ms",
            style = MaterialTheme.typography.bodySmall,
        )
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
    onSortMethodChange: (SearchViewModel.SortMethod) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Tabs.entries.fastForEach {
            if (searchType == it.type) AccentButton(onClick = {}) {
                Text(stringResource(it.label))
            } else Button(onClick = { onTypeChange(it.type) }) {
                Text(stringResource(it.label))
            }
        }

        if (searchType == SearchViewModel.SearchType.SONG) {
            var expanded by remember { mutableStateOf(false) }

            Box(Modifier.weight(1f), Alignment.CenterEnd) {
                Button(onClick = { expanded = true }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
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