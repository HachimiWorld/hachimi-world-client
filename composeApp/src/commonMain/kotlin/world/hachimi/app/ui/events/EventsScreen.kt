package world.hachimi.app.ui.events

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.events_cover_cd
import hachimiworld.composeapp.generated.resources.events_empty
import org.jetbrains.compose.resources.stringResource
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.datetime.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.api.module.PostModule
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.model.EventsListViewModel
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.placeholder
import world.hachimi.app.ui.player.fullscreen.components.AmbientUserChip
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.ui.util.listHeadInsetsSpacerItem
import world.hachimi.app.ui.util.listTailSpacerItem
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Announcements: a list on phones; wider, a grid led by the latest one.
 *
 * @param maxWidth How wide the content gets. @since 261110
 * @param headInsets Leaves room for the status bar and keeps a wide margin; off inside a page that
 * already has a header, such as the square. @since 261110
 */
@Composable
fun EventsScreen(
    maxWidth: Dp = 1040.dp,
    headInsets: Boolean = true,
    vm: EventsListViewModel = koinViewModel(),
) {
    val navigator = LocalNavigator.current

    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }

    AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) {
        when (it) {
            // While loading, the same layout with stand-ins in place of the cards
            InitializeStatus.INIT -> EventsContent(navigator, vm, maxWidth, headInsets, skeleton = true)
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> EventsContent(navigator, vm, maxWidth, headInsets)
        }
    }
}

@Composable
private fun EventsContent(
    navigator: world.hachimi.app.nav.Navigator,
    vm: EventsListViewModel,
    maxWidth: Dp,
    headInsets: Boolean,
    skeleton: Boolean = false,
) {
    BoxWithConstraints {
        val screenWidth = this.maxWidth
        val margin = if (headInsets) 24.dp else AdaptiveScreenMargin
        // Pads each side so the content is at most maxWidth wide, centered, and keeps the margin from the edge
        val side = max(margin, (screenWidth - maxWidth) / 2)
        val padding = PaddingValues(start = side, end = side, top = if (headInsets) 24.dp else 16.dp, bottom = 24.dp)
        if (screenWidth < WindowSize.COMPACT) {
            EventsListCompact(navigator, vm, padding, headInsets, skeleton)
        } else {
            EventsGridExpanded(navigator, vm, padding, screenWidth - side * 2, headInsets, skeleton)
        }
    }
}

@Composable
private fun EventsListCompact(
    navigator: world.hachimi.app.nav.Navigator,
    vm: EventsListViewModel,
    padding: PaddingValues,
    headInsets: Boolean,
    skeleton: Boolean,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState.canScrollForward) {
        if (vm.items.isNotEmpty() && !listState.canScrollForward) {
            vm.loadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = padding,
        userScrollEnabled = !skeleton,
    ) {
        if (headInsets) listHeadInsetsSpacerItem()
        if (skeleton) {
            items(3) { EventCardVerticalSkeleton() }
        } else if (vm.items.isEmpty() && !vm.loading) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(Res.string.events_empty))
                }
            }
        } else {
            items(vm.items, key = { it.id }) { item ->
                EventCardVertical(item, {
                    navigator.push(Route.Root.Events.Detail(item.id))
                })
            }

            item {
                LoadMoreItem(hasMore = !vm.noMoreData, isLoading = vm.loadingMore)
            }
        }

        listTailSpacerItem()
    }
}

@Composable
private fun EventsGridExpanded(
    navigator: world.hachimi.app.nav.Navigator,
    vm: EventsListViewModel,
    padding: PaddingValues,
    contentWidth: Dp,
    headInsets: Boolean,
    skeleton: Boolean,
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState.canScrollForward) {
        if (vm.items.isNotEmpty() && !gridState.canScrollForward && !vm.loading && !vm.loadingMore) {
            vm.loadMore()
        }
    }

    val columns = if (contentWidth >= 960.dp) 3 else 2
    val spacing = if (headInsets) 24.dp else 16.dp

    LazyVerticalGrid(
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(columns),
        contentPadding = padding,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        userScrollEnabled = !skeleton,
    ) {
        if (headInsets) listHeadInsetsSpacerItem()

        if (skeleton) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                if (contentWidth >= WindowSize.MEDIUM) {
                    EventCardFeaturedSkeleton(if (contentWidth >= 960.dp) CardSize.LARGE else CardSize.MEDIUM)
                } else {
                    EventCardVerticalSkeleton()
                }
            }
            items(columns * 2) { EventCardVerticalSkeleton() }
        } else if (vm.items.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(Res.string.events_empty))
                }
            }
        } else {
            // The latest leads over the whole row: beside its cover where there's room, under it where not
            item(span = { GridItemSpan(maxLineSpan) }) {
                val item = vm.items.first()
                val open = { navigator.push(Route.Root.Events.Detail(item.id)) }
                if (contentWidth >= WindowSize.MEDIUM) {
                    EventCardFeatured(
                        item = item,
                        onClick = open,
                        size = if (contentWidth >= 960.dp) CardSize.LARGE else CardSize.MEDIUM
                    )
                } else {
                    EventCardVertical(item, onClick = open)
                }
            }

            items(vm.items.drop(1), key = { it.id }) { item ->
                EventCardVertical(
                    item,
                    onClick = { navigator.push(Route.Root.Events.Detail(item.id)) }
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                LoadMoreItem(hasMore = !vm.noMoreData, isLoading = vm.loadingMore)
            }
            listTailSpacerItem()
        }
    }
}



enum class CardSize {
    MEDIUM, LARGE
}

/** Stands in for [EventCardFeatured] while the announcements load. */
@Composable
private fun EventCardFeaturedSkeleton(size: CardSize) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.height(if (size == CardSize.MEDIUM) 256.dp else 360.dp).aspectRatio(16f / 9f)
                .placeholder(true, RoundedCornerShape(16.dp))
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp).placeholder(true, CircleShape))
                Box(Modifier.padding(start = 8.dp).width(72.dp).height(12.dp).placeholder(true))
            }
            Box(Modifier.fillMaxWidth(0.9f).height(24.dp).placeholder(true, RoundedCornerShape(6.dp)))
            Box(Modifier.fillMaxWidth(0.6f).height(24.dp).placeholder(true, RoundedCornerShape(6.dp)))
            Box(Modifier.width(80.dp).height(12.dp).placeholder(true))
        }
    }
}

/** Stands in for [EventCardVertical] while the announcements load. */
@Composable
private fun EventCardVerticalSkeleton() {
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).placeholder(true, RoundedCornerShape(12.dp)))
        Column(
            Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.height(24.dp), contentAlignment = Alignment.CenterStart) {
                Box(Modifier.fillMaxWidth(0.7f).height(14.dp).placeholder(true))
            }
            Row(Modifier.fillMaxWidth().height(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(20.dp).placeholder(true, CircleShape))
                Box(Modifier.padding(start = 8.dp).width(64.dp).height(12.dp).placeholder(true))
                Spacer(Modifier.weight(1f))
                Box(Modifier.width(56.dp).height(12.dp).placeholder(true))
            }
        }
    }
}
@Composable
private fun EventCardFeatured(
    item: PostModule.PostDetail,
    modifier: Modifier = Modifier,
    size: CardSize,
    onClick: () -> Unit
) {
    val context = LocalPlatformContext.current

    // Flat on the page: the cover and the type carry it, with no outline or shadow
    Box(modifier = modifier.clip(RoundedCornerShape(16.dp))) {
        Row(
            modifier = Modifier.clickable(onClick = onClick).fillMaxWidth(),
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
                            .httpHeaders(CoilHeaders)
                            .data(item.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(Res.string.events_cover_cd),
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
                            .httpHeaders(CoilHeaders)
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

    // Flat on the page: the cover and the type carry it, with no outline or shadow
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp))) {
        Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
            Surface(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                shape = RoundedCornerShape(12.dp),
                color = LocalContentColor.current.copy(0.12f)
            ) {
                if (item.coverUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .httpHeaders(CoilHeaders)
                            .data(item.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(Res.string.events_cover_cd),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AmbientUserChip(
                        onClick = {},
                        avatar = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalPlatformContext.current)
                                .httpHeaders(CoilHeaders)
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
            isBanned = false,
            connectedAccounts = listOf()
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