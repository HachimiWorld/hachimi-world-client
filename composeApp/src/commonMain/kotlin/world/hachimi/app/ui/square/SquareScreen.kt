package world.hachimi.app.ui.square

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.events_empty
import hachimiworld.composeapp.generated.resources.moment_create_button
import hachimiworld.composeapp.generated.resources.moment_input_placeholder
import hachimiworld.composeapp.generated.resources.moment_refresh_cd
import hachimiworld.composeapp.generated.resources.nav_square
import hachimiworld.composeapp.generated.resources.notification_retry
import hachimiworld.composeapp.generated.resources.square_tab_announcements
import hachimiworld.composeapp.generated.resources.square_tab_moments
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.PostModule
import world.hachimi.app.model.EventsListViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.SquareIntro
import world.hachimi.app.model.SquareViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.placeholder
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.HeadlineTabRow
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.events.EventsScreen
import world.hachimi.app.ui.home.components.AdaptivePullToRefreshBox
import world.hachimi.app.ui.insets.multiplatformStatusBarsPadding
import world.hachimi.app.ui.message.components.RemoteImage
import world.hachimi.app.ui.moment.LoadMoreMomentsEffect
import world.hachimi.app.ui.moment.momentItems
import world.hachimi.app.ui.moment.rememberMomentActions
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.ui.util.listTailPadding
import world.hachimi.app.ui.util.listTailSpacerItem
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime

/** The square's tabs, in order. @since 261106 */
enum class SquareTab { MOMENTS, ANNOUNCEMENTS }

/**
 * The square: everyone's moments, and the announcements that used to be a page of their own.
 *
 * The moments lie flat down one column, a line between them. Wide, the announcements sit beside
 * them; narrower, the two are tabs.
 *
 * @since 261106
 */
@Composable
fun SquareScreen(
    initialTab: SquareTab = SquareTab.MOMENTS,
    vm: SquareViewModel = koinViewModel(),
    squareIntro: SquareIntro = koinInject(),
) {
    // Found the square, so the home page needn't point here any more
    LaunchedEffect(Unit) { squareIntro.markSeen() }
    val listState = rememberLazyListState()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= SquareWideMinWidth) {
            WideSquare(vm, listState, maxWidth)
        } else {
            TabbedSquare(vm, listState, maxWidth, initialTab)
        }
    }
}

@Composable
private fun WideSquare(vm: SquareViewModel, listState: LazyListState, width: Dp) {
    val scope = rememberCoroutineScope()
    val side = max(AdaptiveScreenMargin, (width - SquareMaxWidth - PanelGap - PanelWidth) / 2)
    Row(Modifier.fillMaxSize().multiplatformStatusBarsPadding().padding(horizontal = side)) {
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Row(
                Modifier.fillMaxWidth().height(56.dp).padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(Res.string.nav_square),
                    modifier = Modifier.weight(1f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                )
                RefreshButton(vm, onRefresh = { scope.launch { listState.scrollToItem(0) } })
            }
            Hairline()
            MomentsFeed(vm, listState, PaddingValues(0.dp), Modifier.fillMaxWidth().weight(1f), composer = true)
        }
        Spacer(Modifier.width(PanelGap))
        // Its heading sits level with the square's title
        Column(Modifier.width(PanelWidth).fillMaxHeight().padding(top = 6.dp).listTailPadding()) {
            AnnouncementsPanel(Modifier.weight(1f, fill = false))
        }
    }
}

@Composable
private fun TabbedSquare(vm: SquareViewModel, listState: LazyListState, width: Dp, initialTab: SquareTab) {
    val pagerState = rememberPagerState(initialPage = initialTab.ordinal) { SquareTab.entries.size }
    val scope = rememberCoroutineScope()
    // Phones pull to refresh, find the editor in a floating button, and run every row to the edges
    val phone = LocalWindowSize.current.width < WindowSize.COMPACT
    val side = max(0.dp, (width - SquareMaxWidth) / 2)
    Column(Modifier.fillMaxSize().multiplatformStatusBarsPadding()) {
        Box(Modifier.fillMaxWidth().padding(horizontal = side), contentAlignment = Alignment.CenterEnd) {
            // No bar above, so the tabs are the page's title
            HeadlineTabRow(
                tabs = listOf(
                    stringResource(Res.string.square_tab_moments),
                    stringResource(Res.string.square_tab_announcements),
                ),
                position = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
                onTabSelected = { scope.launch { pagerState.animateScrollToPage(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            if (!phone && pagerState.currentPage == SquareTab.MOMENTS.ordinal) {
                Box(Modifier.padding(end = 8.dp)) {
                    RefreshButton(vm, onRefresh = { scope.launch { listState.scrollToItem(0) } })
                }
            }
        }
        HorizontalPager(pagerState, modifier = Modifier.fillMaxWidth().weight(1f)) { page ->
            when (SquareTab.entries[page]) {
                SquareTab.MOMENTS -> MomentsFeed(
                    vm, listState, PaddingValues(horizontal = side), Modifier.fillMaxSize(), composer = !phone,
                )
                SquareTab.ANNOUNCEMENTS -> EventsScreen(maxWidth = SquareMaxWidth, headInsets = false)
            }
        }
    }
}

@Composable
private fun Hairline(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(HachimiTheme.colorScheme.outline))
}

@Composable
private fun RefreshButton(vm: SquareViewModel, onRefresh: () -> Unit) {
    val pager = vm.moments
    val refreshing = pager.refreshing
    HachimiIconButton(
        onClick = {
            // New ones come in on top
            onRefresh()
            pager.refresh()
        },
        modifier = Modifier.size(40.dp),
        enabled = !refreshing,
    ) {
        if (refreshing) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                Icons.Default.Refresh,
                stringResource(Res.string.moment_refresh_cd),
                tint = HachimiTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * @param composer Starts the list with the [Composer]; without it, a floating button opens the editor.
 */
@Composable
private fun MomentsFeed(
    vm: SquareViewModel,
    listState: LazyListState,
    padding: PaddingValues,
    modifier: Modifier,
    composer: Boolean,
    global: GlobalStore = koinInject(),
) {
    val navigator = LocalNavigator.current
    val pager = vm.moments
    val actions = rememberMomentActions()
    LaunchedEffect(pager) { pager.ensureLoaded() }
    LoadMoreMomentsEffect(listState, pager)
    val create = {
        if (global.isLoggedIn) navigator.push(Route.Root.CreateMoment)
        else navigator.push(Route.Auth(false))
    }

    Box(modifier) {
        AdaptivePullToRefreshBox(
            isRefreshing = pager.status == InitializeStatus.LOADED && pager.refreshing,
            onRefresh = pager::refresh,
            screenWidth = LocalWindowSize.current.width,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding,
            ) {
                if (composer) {
                    item(key = "composer") {
                        Composer(avatarUrl = global.userInfo?.avatarUrl, loggedIn = global.isLoggedIn, onClick = create)
                        Hairline()
                    }
                }
                momentItems(pager, actions)
                listTailSpacerItem()
            }
        }
        if (!composer) {
            AccentButton(
                onClick = create,
                modifier = Modifier.align(Alignment.BottomEnd).listTailPadding().padding(end = 16.dp, bottom = 16.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(16.dp),
            ) {
                Icon(Icons.Default.Edit, stringResource(Res.string.moment_create_button), Modifier.size(24.dp))
            }
        }
    }
}

/**
 * Where a moment starts, on top of the list: the user's avatar, a prompt, and the button that
 * opens the editor (or sign-in). Flat, like the moments under it.
 *
 * @since 261110
 */
@Composable
private fun Composer(avatarUrl: String?, loggedIn: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = HachimiTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loggedIn) {
            RemoteImage(avatarUrl, Modifier.size(40.dp).clip(CircleShape))
        } else {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(colors.onSurface.copy(0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Person, null, Modifier.size(22.dp), tint = colors.onSurfaceVariant)
            }
        }
        Text(
            stringResource(Res.string.moment_input_placeholder),
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            fontSize = 16.sp,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        AccentButton(
            onClick = onClick,
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
        ) {
            Text(stringResource(Res.string.moment_create_button), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/**
 * The announcements beside the moments on wide screens: the title and the date, with a small
 * cover. A filled panel, with no outline or shadow.
 *
 * @since 261110
 */
@Composable
private fun AnnouncementsPanel(modifier: Modifier = Modifier, vm: EventsListViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    val colors = HachimiTheme.colorScheme
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }
    val listState = rememberLazyListState()
    LaunchedEffect(listState.canScrollForward, vm.items.size) {
        if (vm.items.isNotEmpty() && !listState.canScrollForward && !vm.noMoreData && !vm.loadingMore) vm.loadMore()
    }

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(bottom = 6.dp),
    ) {
        Text(
            stringResource(Res.string.square_tab_announcements),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
        )
        when {
            vm.initializeStatus == InitializeStatus.FAILED -> Box(
                Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                TextButton(onClick = vm::retry) { Text(stringResource(Res.string.notification_retry)) }
            }
            vm.initializeStatus == InitializeStatus.INIT -> repeat(3) { AnnouncementRowSkeleton() }
            vm.items.isEmpty() -> Text(
                stringResource(Res.string.events_empty),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                fontSize = 13.sp,
                color = colors.onSurfaceVariant,
            )
            else -> LazyColumn(state = listState, modifier = Modifier.weight(1f, fill = false)) {
                items(vm.items, key = { it.id }) { item ->
                    AnnouncementRow(item, onClick = { navigator.push(Route.Root.Events.Detail(item.id)) })
                }
            }
        }
    }
}

@Composable
private fun AnnouncementRow(item: PostModule.PostDetail, onClick: () -> Unit) {
    val colors = HachimiTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                formatTime(item.createTime, distance = true, precise = false, fullFormat = LocalDateTime.Formats.YMD),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = colors.onSurfaceVariant,
                maxLines = 1,
            )
        }
        RemoteImage(item.coverUrl, Modifier.width(80.dp).height(45.dp).clip(RoundedCornerShape(8.dp)))
    }
}

/** Stands in for [AnnouncementRow] while the announcements load. */
@Composable
private fun AnnouncementRowSkeleton() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth(0.85f).height(12.dp).placeholder(true))
            Box(Modifier.width(56.dp).height(10.dp).placeholder(true))
        }
        Box(Modifier.width(80.dp).height(45.dp).placeholder(true, RoundedCornerShape(8.dp)))
    }
}

/** How wide the moments get, so lines of text stay readable; the moment editor keeps to it too. */
internal val SquareMaxWidth = 640.dp

/** From this width the announcements move from a tab to a panel beside the moments. */
private val SquareWideMinWidth = 960.dp
private val PanelWidth = 320.dp
private val PanelGap = 32.dp
