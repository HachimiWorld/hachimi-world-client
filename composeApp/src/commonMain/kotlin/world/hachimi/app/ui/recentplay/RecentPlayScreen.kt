package world.hachimi.app.ui.recentplay

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.datetime.Instant
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.RecentPlayViewModel
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.formatTime

@Composable
fun RecentPlayScreen(
    vm: RecentPlayViewModel = koinViewModel()
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }

    val state = rememberLazyListState()
    LaunchedEffect(state.canScrollForward) {
        if (!vm.loading && !state.canScrollForward) {
            vm.loadMore()
        }
    }

    AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) {
        when (it) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 24.dp)
                ) {
                    item {
                        Text(
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
                            text ="最近播放", style = MaterialTheme.typography.titleLarge
                        )
                    }
                    itemsIndexed(vm.history, key = { _, item -> item.id }) { index, item ->
                        RecentPlayItem(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 24.dp),
                            coverUrl = item.songInfo.coverUrl,
                            title = item.songInfo.title,
                            artist = item.songInfo.uploaderName,
                            playTime = item.playTime,
                            onPlayClick = { vm.play(item) }
                        )
                    }
                }
                if (vm.loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun RecentPlayItem(
    coverUrl: String,
    title: String,
    artist: String,
    playTime: Instant,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.height(72.dp), onClick = onPlayClick) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = MaterialTheme.colorScheme.onSurface.copy(0.12f)) {
                AsyncImage(
                    modifier = Modifier.fillMaxHeight().aspectRatio(1f),
                    model = coverUrl,
                    contentDescription = "Cover Image",
                    contentScale = ContentScale.Crop
                )
            }
            Column(Modifier.weight(1f).padding(vertical = 8.dp, horizontal = 12.dp)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium)
                Text(text = artist, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                modifier = Modifier.padding(horizontal = 12.dp),
                text = formatTime(playTime, distance = true, precise = false),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        RecentPlayItem(
            coverUrl = "https://example.com/cover.jpg",
            title = "Test Title",
            artist = "Test Artist",
            playTime = Instant.parse("2023-04-01T00:00:00Z"),
            onPlayClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}