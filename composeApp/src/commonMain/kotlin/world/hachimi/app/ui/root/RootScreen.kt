package world.hachimi.app.ui.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import soup.compose.material.motion.animation.materialSharedAxisY
import soup.compose.material.motion.animation.rememberSlideDistance
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Navigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.LocalSharedTransitionScope
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.component.DevelopingPage
import world.hachimi.app.ui.component.Logo
import world.hachimi.app.ui.component.NeedLoginScreen
import world.hachimi.app.ui.contributor.ContributorCenterScreen
import world.hachimi.app.ui.creation.CreationCenterScreen
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.ElevatedCard
import world.hachimi.app.ui.events.EventsRouteScreen
import world.hachimi.app.ui.home.HomeScreen
import world.hachimi.app.ui.insets.currentSafeAreaInsets
import world.hachimi.app.ui.likes.RecentLikeScreen
import world.hachimi.app.ui.player.footer.CompactFooterHeight
import world.hachimi.app.ui.player.footer.CompactFooterPlayer2
import world.hachimi.app.ui.player.footer.ExpandedFooterPlayer2
import world.hachimi.app.ui.playlist.PlaylistRouteScreen
import world.hachimi.app.ui.playlist.PublicPlaylistScreen
import world.hachimi.app.ui.recentplay.RecentPlayScreen
import world.hachimi.app.ui.root.component.CompactTopAppBar
import world.hachimi.app.ui.root.component.ExpandedScaffoldLayout
import world.hachimi.app.ui.root.component.ExpandedTopAppBar
import world.hachimi.app.ui.root.component.SideNavigation
import world.hachimi.app.ui.search.SearchScreen
import world.hachimi.app.ui.settings.ChangelogScreen
import world.hachimi.app.ui.settings.SettingsScreen
import world.hachimi.app.ui.userspace.EditProfileScreen
import world.hachimi.app.ui.userspace.UserSpaceScreen
import world.hachimi.app.util.WindowSize
import world.hachimi.app.util.fillMaxWidthIn

@Composable
fun RootScreen() {
    val navigator = LocalNavigator.current
    val global = koinInject<GlobalStore>()
    val currentRoot = navigator.rootBackStack.lastOrNull() ?: return

    AdaptiveScreen(
        navigationContent = { onChange ->
            SideNavigation(
                content = currentRoot,
                onChange = { onChange(it) }
            )
        },
        content = {
            RootNavHost(global, navigator)
        }
    )

}

@Composable
private fun RootNavHost(global: GlobalStore, navigator: Navigator) {
    val slideDistance = rememberSlideDistance()

    NavDisplay(
        backStack = navigator.rootBackStack,
        onBack = navigator::back,
        sharedTransitionScope = LocalSharedTransitionScope.current,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = { materialSharedAxisY(true, slideDistance) },
        popTransitionSpec = { materialSharedAxisY(false, slideDistance) },
        predictivePopTransitionSpec = { materialSharedAxisY(false, slideDistance) },
        entryProvider = { key ->
            when (key) {
                is Route.Root.Events -> NavEntry(key) { EventsRouteScreen(key) }
                is Route.Root.Home -> NavEntry(key) { HomeScreen(key) }
                is Route.Root.Search -> NavEntry(key) { SearchScreen(key.query, key.type) }
                Route.Root.RecentLike -> NavEntry(key) {
                    if (global.isLoggedIn) RecentLikeScreen() else NeedLoginScreen()
                }

                Route.Root.RecentPlay -> NavEntry(key) {
                    if (global.isLoggedIn) RecentPlayScreen() else NeedLoginScreen()
                }

                is Route.Root.MyPlaylist -> NavEntry(key) {
                    if (global.isLoggedIn) PlaylistRouteScreen(key) else NeedLoginScreen()
                }

                Route.Root.MySubscribe -> NavEntry(key) {
                    if (global.isLoggedIn) DevelopingPage() else NeedLoginScreen()
                }

                is Route.Root.CreationCenter -> NavEntry(key) {
                    if (global.isLoggedIn) CreationCenterScreen(key) else NeedLoginScreen()
                }

                Route.Root.CommitteeCenter -> NavEntry(key) {
                    if (global.isLoggedIn) DevelopingPage() else NeedLoginScreen()
                }

                is Route.Root.ContributorCenter -> NavEntry(key) {
                    if (global.isLoggedIn) ContributorCenterScreen(key) else NeedLoginScreen()
                }

                Route.Root.UserSpace -> NavEntry(key) { UserSpaceScreen(null) }
                Route.Root.Settings -> NavEntry(key) { SettingsScreen() }
                Route.Root.Changelog -> NavEntry(key) { ChangelogScreen() }
                is Route.Root.PublicUserSpace -> NavEntry(key) { UserSpaceScreen(key.userId) }
                is Route.Root.PublicPlaylist -> NavEntry(key) {
                    if (global.isLoggedIn) PublicPlaylistScreen(key.playlistId) else NeedLoginScreen()
                }

                Route.Root.EditProfile -> NavEntry(key) {
                    if (global.isLoggedIn) EditProfileScreen() else NeedLoginScreen()
                }
            }
        }
    )
}

@Composable
private fun AdaptiveScreen(
    navigationContent: @Composable (onChange: (Route) -> Unit) -> Unit,
    content: @Composable () -> Unit
) {
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()
    if (LocalWindowSize.current.width < WindowSize.COMPACT) {
        CompactScreen({ state ->
            navigationContent {
                scope.launch {
                    delay(120)
                    state.close()
                    navigator.push(it)
                }
            }
        }, content)
    } else {
        ExpandedScreen({
            navigationContent({
                navigator.push(it)
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
            ElevatedCard(
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
            Column(
                Modifier.fillMaxSize().hazeSource(hazeState)
                    .background(HachimiTheme.colorScheme.background)
            ) {
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
                modifier = Modifier.fillMaxSize().wrapContentHeight(align = Alignment.Bottom)
                    .padding(24.dp)
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
    val hazeState = rememberHazeState()

    ExpandedScaffoldLayout(
        modifier = Modifier.fillMaxSize(),
        appbar = {
            ExpandedTopAppBar(
                modifier = Modifier.zIndex(2f).fillMaxWidth(),
                global = global,
            )
        },
        navigation = {
            ElevatedCard(Modifier.padding(start = 24.dp, top = 24.dp, bottom = 24.dp).width(180.dp)) {
                Box(Modifier.padding(8.dp)) {
                    navigationContent()
                }
            }
        },
        footerPlayer = {
            ExpandedFooterPlayer2(
                Modifier.wrapContentHeight(Alignment.Bottom).padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 24.dp
                ).fillMaxWidthIn(),
                hazeState
            )
        },
        content = { contentPadding ->
            Box(
                Modifier.fillMaxSize().hazeSource(hazeState)
                    .background(HachimiTheme.colorScheme.background)
                    .padding(
                        top = contentPadding.calculateTopPadding(),
                        start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                        end = contentPadding.calculateEndPadding(LocalLayoutDirection.current)
                    )
            ) {
                CompositionLocalProvider(
                    LocalContentInsets provides WindowInsets(
                        bottom = contentPadding.calculateBottomPadding()
                    )
                ) {
                    content()
                }
            }
        }
    )
}