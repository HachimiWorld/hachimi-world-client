package world.hachimi.app.ui.square

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.moment_create_button
import hachimiworld.composeapp.generated.resources.moment_refresh_cd
import hachimiworld.composeapp.generated.resources.square_tab_announcements
import hachimiworld.composeapp.generated.resources.square_tab_moments
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.SquareIntro
import world.hachimi.app.model.SquareViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.TabBar
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.events.EventsScreen
import world.hachimi.app.ui.home.components.AdaptivePullToRefreshBox
import world.hachimi.app.ui.insets.multiplatformStatusBarsPadding
import world.hachimi.app.ui.moment.LoadMoreMomentsEffect
import world.hachimi.app.ui.moment.momentItems
import world.hachimi.app.ui.moment.rememberMomentActions
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.ui.util.fillMaxWidthIn
import world.hachimi.app.ui.util.listTailPadding
import world.hachimi.app.ui.util.listTailSpacerItem

/** The square's tabs, in order. @since 261106 */
enum class SquareTab { MOMENTS, ANNOUNCEMENTS }

/**
 * The square: everyone's moments, and the announcements that used to be a page of their own.
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
    val pagerState = rememberPagerState(initialPage = initialTab.ordinal) { SquareTab.entries.size }
    val scope = rememberCoroutineScope()
    val pager = vm.moments
    val listState = rememberLazyListState()

    Column(Modifier.fillMaxSize().multiplatformStatusBarsPadding()) {
        Row(
            Modifier.fillMaxWidthIn(maxWidth = MomentListMaxWidth).padding(
                top = AdaptiveScreenMargin,
                start = AdaptiveScreenMargin,
                end = AdaptiveScreenMargin,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabBar(
                tabs = listOf(
                    stringResource(Res.string.square_tab_moments),
                    stringResource(Res.string.square_tab_announcements),
                ),
                selectedIndex = pagerState.currentPage,
                onTabSelected = { scope.launch { pagerState.animateScrollToPage(it) } },
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Phones pull to refresh; wider screens get a button over the list it refreshes
            if (LocalWindowSize.current.width >= WindowSize.COMPACT && pagerState.currentPage == SquareTab.MOMENTS.ordinal) {
                val refreshing = pager.refreshing
                HachimiIconButton(
                    onClick = {
                        // New ones come in on top
                        scope.launch { listState.scrollToItem(0) }
                        pager.refresh()
                    },
                    enabled = !refreshing,
                ) {
                    if (refreshing) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, stringResource(Res.string.moment_refresh_cd))
                    }
                }
            }
        }
        HorizontalPager(pagerState, modifier = Modifier.fillMaxWidth().weight(1f)) { page ->
            when (SquareTab.entries[page]) {
                SquareTab.MOMENTS -> MomentsTab(vm, listState)
                SquareTab.ANNOUNCEMENTS -> EventsScreen()
            }
        }
    }
}

@Composable
private fun MomentsTab(
    vm: SquareViewModel,
    listState: LazyListState,
    global: GlobalStore = koinInject(),
) {
    val navigator = LocalNavigator.current
    val pager = vm.moments
    val actions = rememberMomentActions()
    LaunchedEffect(pager) { pager.ensureLoaded() }
    LoadMoreMomentsEffect(listState, pager)
    val screenWidth = LocalWindowSize.current.width

    Box(Modifier.fillMaxSize()) {
        AdaptivePullToRefreshBox(
            isRefreshing = pager.status == InitializeStatus.LOADED && pager.refreshing,
            onRefresh = pager::refresh,
            screenWidth = screenWidth,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().fillMaxWidthIn(maxWidth = MomentListMaxWidth),
                contentPadding = PaddingValues(horizontal = AdaptiveScreenMargin - 12.dp, vertical = 8.dp),
            ) {
                momentItems(pager, actions)
                listTailSpacerItem()
            }
        }
        Box(Modifier.align(Alignment.BottomEnd).listTailPadding().padding(AdaptiveScreenMargin)) {
            AccentButton(
                onClick = {
                    if (global.isLoggedIn) navigator.push(Route.Root.CreateMoment)
                    else navigator.push(Route.Auth(false))
                },
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.moment_create_button))
            }
        }
    }
}

/** How wide a list of moments gets, so lines of text stay readable. */
internal val MomentListMaxWidth = 680.dp
