package world.hachimi.app.ui.root

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import soup.compose.material.motion.animation.materialSharedAxisY
import soup.compose.material.motion.animation.rememberSlideDistance
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.component.DevelopingPage
import world.hachimi.app.ui.component.Logo
import world.hachimi.app.ui.component.NeedLoginScreen
import world.hachimi.app.ui.contributor.ContributorCenterScreen
import world.hachimi.app.ui.creation.CreationCenterScreen
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.home.HomeScreen
import world.hachimi.app.ui.insets.currentSafeAreaInsets
import world.hachimi.app.ui.player.footer.CompactFooterHeight
import world.hachimi.app.ui.player.footer.CompactFooterPlayer2
import world.hachimi.app.ui.player.footer.ExpandedFooterHeight
import world.hachimi.app.ui.player.footer.ExpandedFooterPlayer2
import world.hachimi.app.ui.playlist.PlaylistRouteScreen
import world.hachimi.app.ui.playlist.PublicPlaylistScreen
import world.hachimi.app.ui.recentplay.RecentPlayScreen
import world.hachimi.app.ui.root.component.CompactTopAppBar
import world.hachimi.app.ui.root.component.ExpandedTopAppBar
import world.hachimi.app.ui.root.component.SideNavigation
import world.hachimi.app.ui.search.SearchScreen
import world.hachimi.app.ui.settings.SettingsScreen
import world.hachimi.app.ui.userspace.UserSpaceScreen
import world.hachimi.app.util.WindowSize
import world.hachimi.app.util.fillMaxWidthIn

@Composable
fun RootScreen(routeContent: Route.Root) {
    val global = koinInject<GlobalStore>()
    AdaptiveScreen(
        navigationContent = { onChange ->
            SideNavigation(
                content = routeContent,
                onChange = { onChange(it) }
            )
        },
        content = {
            val slideDistance = rememberSlideDistance()

            AnimatedContent(
                targetState = routeContent,
                transitionSpec = { materialSharedAxisY(true, slideDistance) }
            ) { routeContent ->
                when (routeContent) {
                    is Route.Root.Home -> HomeScreen(routeContent)
                    is Route.Root.Search -> SearchScreen(routeContent.query, routeContent.type)
                    Route.Root.RecentLike -> if (global.isLoggedIn) DevelopingPage() else NeedLoginScreen()
                    Route.Root.RecentPlay -> if (global.isLoggedIn) RecentPlayScreen() else NeedLoginScreen()
                    is Route.Root.MyPlaylist -> if (global.isLoggedIn) PlaylistRouteScreen(routeContent) else NeedLoginScreen()
                    Route.Root.MySubscribe -> if (global.isLoggedIn) DevelopingPage() else NeedLoginScreen()
                    is Route.Root.CreationCenter -> if (global.isLoggedIn) CreationCenterScreen(routeContent) else NeedLoginScreen()
                    Route.Root.CommitteeCenter -> if (global.isLoggedIn) DevelopingPage() else NeedLoginScreen()
                    is Route.Root.ContributorCenter -> if (global.isLoggedIn) ContributorCenterScreen(routeContent) else NeedLoginScreen()
                    Route.Root.UserSpace -> UserSpaceScreen(null)
                    Route.Root.Settings -> SettingsScreen()
                    is Route.Root.PublicUserSpace -> UserSpaceScreen(routeContent.userId)
                    is Route.Root.PublicPlaylist -> if (global.isLoggedIn) PublicPlaylistScreen(routeContent.playlistId) else NeedLoginScreen()
                }
            }
        }
    )

}

@Composable
private fun AdaptiveScreen(
    navigationContent: @Composable (onChange: (Route) -> Unit) -> Unit,
    global: GlobalStore = koinInject(),
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    if (LocalWindowSize.current.width < WindowSize.COMPACT) {
        CompactScreen({ state ->
            navigationContent {
                scope.launch {
                    delay(120)
                    state.close()
                    global.nav.push(it)
                }
            }
        }, content)
    } else {
        ExpandedScreen({
            navigationContent({
                global.nav.push(it)
            })
        }, content)
    }
}

@Composable
private fun CompactScreen(
    navigationContent: @Composable (drawerState: DrawerState) -> Unit,
    content: @Composable () -> Unit,
    global: GlobalStore = koinInject()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Card(
                modifier = Modifier.width(300.dp),
                color = HachimiTheme.colorScheme.surface.compositeOver(HachimiTheme.colorScheme.background),
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Column(
                    Modifier.padding(top = currentSafeAreaInsets().top)
                        .padding(bottom = currentSafeAreaInsets().bottom)
////                        .consumeWindowInsets(WindowInsets.statusBars)
//                        .navigationBarsPadding()
                ) {
                    Logo(Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp))
                    Box(Modifier.padding(12.dp)) {
                        navigationContent(drawerState)
                    }
                }
            }
        }
    ) {
        val hazeState = rememberHazeState()
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().hazeSource(hazeState).background(HachimiTheme.colorScheme.background)) {
                CompactTopAppBar(
                    modifier = Modifier.zIndex(2f).fillMaxWidth(),
                    global = global,
                    onExpandNavClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
                Box(Modifier.weight(1f)) {
                    CompositionLocalProvider(
                        LocalContentInsets provides WindowInsets(
                            bottom = CompactFooterHeight + 24.dp // Bottom padding
                        )
                    ) {
                        content()
                    }
                }
            }
            CompactFooterPlayer2(
                modifier = Modifier.fillMaxSize().wrapContentHeight(align = Alignment.Bottom).padding(24.dp)
                    .padding(bottom = currentSafeAreaInsets().bottom),
                hazeState = hazeState
            )
        }
    }
}

@Composable
private fun ExpandedScreen(
    navigationContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
    global: GlobalStore = koinInject()
) {
    Column(Modifier.fillMaxSize()) {
        ExpandedTopAppBar(
            modifier = Modifier.zIndex(2f).fillMaxWidth(),
            global = global,
        )

        Row(Modifier.weight(1f).fillMaxWidth()) {
            Card(Modifier.padding(start = 24.dp, top = 24.dp, bottom = 24.dp).width(180.dp)) {
                Box(Modifier.padding(8.dp)) {
                    navigationContent()
                }
            }
            Spacer(Modifier.width(24.dp))

            val hazeState = rememberHazeState()

            Box(Modifier.weight(1f).fillMaxHeight()) {
                Box(Modifier.fillMaxSize().hazeSource(hazeState).background(HachimiTheme.colorScheme.background)) {
                    CompositionLocalProvider(
                        LocalContentInsets provides WindowInsets(
                            bottom = ExpandedFooterHeight + 24.dp // Bottom padding
                        )
                    ) {
                        content()
                    }
                }
                ExpandedFooterPlayer2(
                    Modifier
                        .fillMaxSize()
                        .wrapContentHeight(Alignment.Bottom)
                        .padding(24.dp)
                        .fillMaxWidthIn(),
                    hazeState
                )
            }
        }
    }
}