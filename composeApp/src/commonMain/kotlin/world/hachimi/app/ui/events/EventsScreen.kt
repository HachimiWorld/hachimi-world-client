package world.hachimi.app.ui.events

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.datetime.LocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.PostModule
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.model.EventsListViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.player.fullscreen.components.AmbientUserChip
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.WindowSize
import world.hachimi.app.util.YMD
import world.hachimi.app.util.fillMaxWidthIn
import world.hachimi.app.util.formatTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

@Composable
fun EventsScreen(
    global: GlobalStore = koinInject(),
    vm: EventsListViewModel = koinViewModel(),
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }

    AnimatedContent(vm.initializeStatus) {
        when (it) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> EventsContent(global, vm)
        }
    }
}

@Composable
private fun EventsContent(global: GlobalStore, vm: EventsListViewModel) {
    BoxWithConstraints {
        val screenWidth = maxWidth
        if (screenWidth < WindowSize.MEDIUM) {
            EventsListCompact(global, vm)
        } else {
            EventsGridExpanded(global, vm, screenWidth)
        }
    }
}

@Composable
private fun EventsListCompact(
    global: GlobalStore,
    vm: EventsListViewModel
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState.canScrollForward) {
        if (vm.items.isNotEmpty() && !listState.canScrollForward) {
            vm.loadMore()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidthIn(maxWidth = 380.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(24.dp)
        ) {
            item {
                Text("活动公告", style = MaterialTheme.typography.titleLarge)
            }

            if (vm.items.isEmpty() && !vm.loading) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无活动")
                    }
                }
            } else {
                items(vm.items, key = { it.id }) { item ->
                    EventCardVertical(item, {
                        global.nav.push(Route.Root.Events.Detail(item.id))
                    })
                }

                EventsFooter(vm = vm)
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

@Composable
private fun EventsGridExpanded(
    global: GlobalStore,
    vm: EventsListViewModel,
    maxWidth: Dp
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState.canScrollForward) {
        if (vm.items.isNotEmpty() && !gridState.canScrollForward && !vm.loading && !vm.loadingMore) {
            vm.loadMore()
        }
    }

    val spacing = 24.dp

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            state = gridState,
            modifier = Modifier.fillMaxSize().fillMaxWidthIn(
                maxWidth = if (maxWidth >= 1040.dp) 1040.dp else WindowSize.MEDIUM
            ),
            columns = if (maxWidth >= 1040.dp) GridCells.Fixed(3) else GridCells.Fixed(2),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            val maxLineSpan = if (maxWidth >= 1040.dp) 3 else 2

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text("活动公告", style = MaterialTheme.typography.titleLarge)
            }

            if (vm.items.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无活动")
                    }
                }
            } else {
                // First line
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val item = vm.items.first()
                    EventCardFeatured(
                        item = item,
                        onClick = { global.nav.push(Route.Root.Events.Detail(item.id)) },
                        size = if (maxWidth >= 1040.dp) CardSize.LARGE else CardSize.MEDIUM
                    )
                }

                val rest = vm.items.drop(1)
                val twoColumnCount = minOf(4, rest.size)
                items(rest.take(twoColumnCount), key = { it.id }) { item ->
                    EventCardVertical(
                        item,
                        onClick = { global.nav.push(Route.Root.Events.Detail(item.id)) }
                    )
                }

                items(rest.drop(twoColumnCount), key = { it.id }) { item ->
                    EventCardVertical(
                        item,
                        onClick = { global.nav.push(Route.Root.Events.Detail(item.id)) }
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    EventsFooterInline(vm)
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
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

@Suppress("FunctionName")
private fun LazyListScope.EventsFooter(vm: EventsListViewModel) {
    if (vm.loadingMore) {
        item {
            Box(
                Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("加载中…")
            }
        }
    } else if (vm.noMoreData) {
        item {
            Box(
                Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("没有更多了")
            }
        }
    }
}

@Composable
private fun EventsFooterInline(vm: EventsListViewModel) {
    Box(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            vm.loadingMore -> Text("加载中…")
            vm.noMoreData -> Text("没有更多了")
            else -> Spacer(Modifier.size(0.dp))
        }
    }
}


enum class CardSize {
    MEDIUM, LARGE
}
@Composable
private fun EventCardFeatured(
    item: PostModule.PostDetail,
    modifier: Modifier = Modifier,
    size: CardSize,
    onClick: () -> Unit
) {
    val context = LocalPlatformContext.current

    Card(modifier = modifier) {
        Row(
            modifier = Modifier.clickable(onClick = onClick).fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                Modifier
                    .height(if (size == CardSize.MEDIUM) 256.dp else 360.dp)
                    .aspectRatio(16f / 9f),
                shape = RoundedCornerShape(16.dp),
                color = LocalContentColor.current.copy(0.12f)
            ) {
                if (item.coverUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Event cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AmbientUserChip(
                    onClick = {},
                    avatar = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalPlatformContext.current)
                            .data(item.author.avatarUrl)
                            .crossfade(true)
                            .build()
                    ),
                    name = item.author.username,
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatTime(
                        item.createTime,
                        distance = true,
                        precise = false,
                        fullFormat = LocalDateTime.Formats.YMD
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    color = LocalContentColor.current.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun EventCardVertical(
    item: PostModule.PostDetail,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalPlatformContext.current

    Card(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
            Surface(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                color = LocalContentColor.current.copy(0.12f)
            ) {
                if (item.coverUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Event cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AmbientUserChip(
                        onClick = {},
                        avatar = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalPlatformContext.current)
                                .data(item.author.avatarUrl)
                                .crossfade(true)
                                .build()
                        ),
                        name = item.author.username,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = formatTime(
                            item.createTime,
                            distance = true,
                            precise = false,
                            fullFormat = LocalDateTime.Formats.YMD
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        color = LocalContentColor.current.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

private fun previewPostDetail(coverUrl: String?): PostModule.PostDetail =
    PostModule.PostDetail(
        id = 1L,
        author = UserModule.PublicUserProfile(
            uid = 114514L,
            username = "Hachimi",
            avatarUrl = null,
            bio = "This is a preview user.",
            gender = null,
            isBanned = false
        ),
        title = "活动标题：这里会有 2-3 行的标题以便测试截断",
        content = "这里是活动内容摘要。我们会展示几行简介文本，超过最大行数应当自动截断。\n\n第二段文字用于测试多段落内容在预览中的表现。",
        contentType = "markdown",
        coverUrl = coverUrl,
        createTime = Clock.System.now().minus(1.hours),
        updateTime = Clock.System.now().minus(1.hours),
    )

@Preview(widthDp = 840)
@Composable
private fun PreviewEventCardFeatured() {
    PreviewTheme(background = true) {
        Box(Modifier.padding(24.dp)) {
            EventCardFeatured(
                item = previewPostDetail(
                    coverUrl = "https://picsum.photos/seed/hachimi-featured/1200/675"
                ),
                size = CardSize.MEDIUM,
                onClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun PreviewEventCardVertical() {
    PreviewTheme(background = true) {
        Box(Modifier.padding(24.dp).widthIn(max = 420.dp)) {
            EventCardVertical(
                item = previewPostDetail(
                    coverUrl = "https://picsum.photos/seed/hachimi-vertical/1200/675"
                ),
                onClick = {}
            )
        }
    }
}