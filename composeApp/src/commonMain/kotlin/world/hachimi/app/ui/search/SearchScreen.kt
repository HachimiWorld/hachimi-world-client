package world.hachimi.app.ui.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.SearchViewModel
import world.hachimi.app.model.fromSearchSongItem
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.search.components.SearchSongItem
import world.hachimi.app.ui.search.components.SearchUserItem

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

    AnimatedContent(vm.loading) {loading ->
        if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        } else Content(vm, global)
    }
}

@Composable
private fun Content(vm: SearchViewModel, global: GlobalStore) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = if (vm.searchType == SearchViewModel.SearchType.SONG) GridCells.Adaptive(minSize = 320.dp)
        else GridCells.Adaptive(152.dp),
        contentPadding = PaddingValues(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "搜索结果",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${vm.searchProcessingTimeMs} ms",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SingleChoiceSegmentedButtonRow(Modifier.wrapContentWidth(align = Alignment.Start)) {
                SegmentedButton(
                    selected = vm.searchType == SearchViewModel.SearchType.SONG,
                    onClick = { vm.updateSearchType(SearchViewModel.SearchType.SONG) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    label = { Text("歌曲") }
                )
                SegmentedButton(
                    selected = vm.searchType == SearchViewModel.SearchType.USER,
                    onClick = { vm.updateSearchType(SearchViewModel.SearchType.USER) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    label = { Text("神人") }
                )
            }
        }

        val data = if (vm.searchType == SearchViewModel.SearchType.SONG) vm.songData else vm.userData
        if (data.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
            Box(
                Modifier.fillMaxWidth().padding(vertical = 128.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("什么也没有找到")
            }
        }

        if (vm.searchType == SearchViewModel.SearchType.SONG) items(
            items = vm.songData,
            key = { item -> item.id },
            contentType = { _ -> "song" }
        ) {item ->
            SearchSongItem(
                modifier = Modifier.fillMaxWidth(),
                data = item,
                onClick = {
                    global.player.insertToQueue(
                        GlobalStore.MusicQueueItem.fromSearchSongItem(item),
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
    }
}