package world.hachimi.app.ui.recentplay

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.recent_play_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.RecentPlayViewModel
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.formatTime
import kotlin.time.Instant

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
                            text = stringResource(Res.string.recent_play_title), style = MaterialTheme.typography.titleLarge
                        )
                    }
                    items(vm.history, key = { item -> item.id }) { item ->
                        RecentPlayItem(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 24.dp),
                            coverUrl = item.songInfo.coverUrl,
                            title = item.songInfo.title,
                            artist = item.songInfo.uploaderName,
                            playTime = item.playTime,
                            onPlayClick = { vm.play(item) }
                        )
                    }
                    item {
                        Spacer(Modifier.navigationBarsPadding().padding(LocalContentInsets.current.asPaddingValues()))
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onPlayClick).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.onSurface.copy(0.12f),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    modifier = Modifier.size(48.dp),
                    model = coverUrl,
                    contentDescription = "Cover Image",
                    contentScale = ContentScale.Crop
                )
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
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