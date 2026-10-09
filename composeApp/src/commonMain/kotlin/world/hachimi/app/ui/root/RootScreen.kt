package world.hachimi.app.ui.root

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import soup.compose.material.motion.animation.materialSharedAxisY
import soup.compose.material.motion.animation.rememberSlideDistance
import world.hachimi.app.model.FollowListType
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.MessageCenterStore
import androidx.lifecycle.compose.LifecycleResumeEffect
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Navigator
import world.hachimi.app.nav.RootShellNavKey
import world.hachimi.app.nav.Route
import world.hachimi.app.nav.isSideNavDestination
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.LocalSharedTransitionScope
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.committee.CommitteeCenterScreen
import world.hachimi.app.ui.committee.CommitteeMembersScreen
import world.hachimi.app.ui.committee.ReportCaseScreen
import world.hachimi.app.ui.component.DevelopingPage
import world.hachimi.app.ui.report.ReportDialogHost
import world.hachimi.app.ui.component.LocalOpenNavigationDrawer
import world.hachimi.app.ui.component.Logo
import world.hachimi.app.ui.component.NeedLoginScreen
import world.hachimi.app.ui.contributor.ContributorEntryScreen
import world.hachimi.app.ui.contributor.CreatePostScreen
import world.hachimi.app.ui.contributor.ReviewDetailScreen
import world.hachimi.app.ui.contributor.ReviewHistoryScreen
import world.hachimi.app.ui.contributor.ReviewListScreen
import world.hachimi.app.ui.contributor.ReviewScreenSource
import world.hachimi.app.ui.creation.artwork.MyArtworkScreen
import world.hachimi.app.ui.creation.artworkdetail.ArtworkDetailScreen
import world.hachimi.app.ui.creation.publish.PublishScreen
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.ElevatedCard
import world.hachimi.app.ui.events.EventDetailScreen
import world.hachimi.app.ui.events.EventsScreen
import world.hachimi.app.ui.follow.FollowListScreen
import world.hachimi.app.ui.home.CategorySongsScreen
import world.hachimi.app.ui.home.HomeMainScreen
import world.hachimi.app.ui.home.RecentPublishScreen
import world.hachimi.app.ui.home.RecommendScreen
import world.hachimi.app.ui.home.WeeklyHotScreen
import world.hachimi.app.ui.insets.multiplatformStatusBars
import world.hachimi.app.ui.insets.multiplatformStatusBarsPadding
import world.hachimi.app.ui.insets.multiplatformSystemBarsPadding
import world.hachimi.app.ui.likes.RecentLikeScreen
import world.hachimi.app.ui.player.miniplayer.CompactFooterHeight
import world.hachimi.app.ui.player.miniplayer.CompactMiniPlayer
import world.hachimi.app.ui.player.miniplayer.ExpandedMiniPlayer
import world.hachimi.app.ui.playlist.PlaylistDetailScreen
import world.hachimi.app.ui.playlist.PlaylistScreen
import world.hachimi.app.ui.playlist.PublicPlaylistScreen
import world.hachimi.app.ui.recentplay.RecentPlayScreen
import world.hachimi.app.ui.root.component.ClientPromoState
import world.hachimi.app.ui.root.component.CompactClientPromoBanner
import world.hachimi.app.ui.root.component.CompactSideNavigation
import world.hachimi.app.ui.root.component.ExpandedClientPromoCard
import world.hachimi.app.ui.root.component.CompactTopAppBar
import world.hachimi.app.ui.root.component.CompactUpdateBanner
import world.hachimi.app.ui.root.component.ExpandedScaffoldLayout
import world.hachimi.app.ui.root.component.ExpandedSideNavigation
import world.hachimi.app.ui.root.component.ExpandedUpdateCard
import world.hachimi.app.ui.root.component.rememberClientPromoState
import world.hachimi.app.ui.search.SearchScreen
import world.hachimi.app.ui.settings.ChangelogScreen
import world.hachimi.app.ui.settings.DeviceManagementScreen
import world.hachimi.app.ui.notification.NotificationDetailScreen
import world.hachimi.app.ui.notification.NotificationInboxScreen
import world.hachimi.app.ui.message.MessageCenterScreen
import world.hachimi.app.ui.message.NewFollowersScreen
import world.hachimi.app.ui.message.ReceivedLikesScreen
import world.hachimi.app.ui.settings.SettingsScreen
import world.hachimi.app.ui.userspace.EditProfileScreen
import world.hachimi.app.ui.userspace.UserSpaceScreen
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.ui.util.fillMaxWidthIn

/**
 * Nested NavDisplay model (especially for Compact):
 * ```
 * NavDisplay(rootBackStack) {
 *   RootShell -> Column { AppBar; NavDisplay(primary) { Home, Settings, … } }
 *   RecentReleases -> RecentPublishScreen
 *   DailyPicks -> RecommendScreen
 *   …
 * }
 * ```
 */
@Composable
fun RootScreen() {
    val navigator = LocalNavigator.current
    val global = koinInject<GlobalStore>()
    val currentPrimary = navigator.currentPrimary ?: return
    val scope = rememberCoroutineScope()
    val clientPromo = rememberClientPromoState()
    val messageCenter = koinInject<MessageCenterStore>()
    LifecycleResumeEffect(messageCenter) {
        messageCenter.refreshSummary()
        onPauseOrDispose {}
    }

    if (LocalWindowSize.current.width < WindowSize.COMPACT) {
        CompactScreen(
            navigationContent = { drawerState ->
                CompactSideNavigation(
                    content = currentPrimary,
                    onChange = { route ->
                        scope.launch {
                            delay(120)
                            drawerState.close()
                            if (route.isSideNavDestination()) {
                                navigator.switchPrimary(route)
                            } else {
                                navigator.push(route)
                            }
                        }
                    },
                )
            },
            global = global,
            navigator = navigator,
            clientPromo = clientPromo,
        )
    } else {
        ExpandedScreen(
            navigationContent = {
                ExpandedSideNavigation(
                    content = currentPrimary,
                    onChange = navigator::switchPrimary,
                    promo = {
                        ExpandedClientPromoCard(clientPromo)
                        ExpandedUpdateCard(global.updates)
                    },
                )
            },
            global = global,
            navigator = navigator,
        )
    }
    ReportDialogHost()
}

// region Nested NavDisplays

/**
 * Outer NavDisplay: RootShell (with nested primary) + secondary full-screen routes.
 */
@Composable
private fun RootNavDisplay(
    global: GlobalStore,
    navigator: Navigator,
    shell: @Composable (primaryContent: @Composable () -> Unit) -> Unit,
) {
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
                RootShellNavKey -> NavEntry(key) {
                    shell {
                        PrimaryNavDisplay(global, navigator)
                    }
                }

                is Route.Root -> secondaryNavEntry(key, global)

                else -> error("Unknown root nav key: $key")
            }
        },
    )
}

/** Nested NavDisplay inside the shell: SideNav destinations only. */
@Composable
private fun PrimaryNavDisplay(global: GlobalStore, navigator: Navigator) {
    NavDisplay(
        backStack = navigator.primaryBackStack,
        onBack = navigator::back,
        sharedTransitionScope = LocalSharedTransitionScope.current,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        popTransitionSpec = { fadeIn() togetherWith fadeOut() },
        predictivePopTransitionSpec = { fadeIn() togetherWith fadeOut() },
        entryProvider = { key -> primaryNavEntry(key, global) },
    )
}

private fun primaryNavEntry(key: Route.Root, global: GlobalStore): NavEntry<Route.Root> =
    when (key) {
        Route.Root.Events.Feed -> NavEntry(key) { EventsScreen() }
        Route.Root.Home.Main -> NavEntry(key) { HomeMainScreen() }
        Route.Root.RecentPlay -> NavEntry(key) {
            if (global.isLoggedIn) RecentPlayScreen() else NeedLoginScreen()
        }
        Route.Root.RecentLike -> NavEntry(key) {
            if (global.isLoggedIn) RecentLikeScreen() else NeedLoginScreen()
        }
        Route.Root.MySubscribe -> NavEntry(key) {
            if (global.isLoggedIn) FollowListScreen(FollowListType.FOLLOWING) else NeedLoginScreen()
        }
        Route.Root.MyPlaylist.List -> NavEntry(key) {
            if (global.isLoggedIn) PlaylistScreen() else NeedLoginScreen()
        }
        Route.Root.CreationCenter.MyArtwork -> NavEntry(key) {
            if (global.isLoggedIn) MyArtworkScreen() else NeedLoginScreen()
        }
        Route.Root.CommitteeCenter -> NavEntry(key) {
            if (global.isLoggedIn) CommitteeCenterScreen() else NeedLoginScreen()
        }
        Route.Root.ContributorCenter.Entry -> NavEntry(key) {
            if (global.isLoggedIn) ContributorEntryScreen() else NeedLoginScreen()
        }
        else -> error("Not a primary SideNav destination: $key")
    }

private fun secondaryNavEntry(key: Route.Root, global: GlobalStore): NavEntry<NavKey> =
    when (key) {
        Route.Root.Home.Recent -> NavEntry(key) { RecentPublishScreen() }
        Route.Root.Home.Recommend -> NavEntry(key) { RecommendScreen() }
        Route.Root.Home.WeeklyHot -> NavEntry(key) { WeeklyHotScreen() }
        Route.Root.Home.HiddenGem -> NavEntry(key) { DevelopingPage() }
        is Route.Root.Home.Category -> NavEntry(key) { CategorySongsScreen(key.category) }

        is Route.Root.Events.Detail -> NavEntry(key) { EventDetailScreen(key.postId) }

        is Route.Root.Search -> NavEntry(key) { SearchScreen(key.query, key.type) }

        is Route.Root.MyPlaylist.Detail -> NavEntry(key) {
            if (global.isLoggedIn) PlaylistDetailScreen(key.playlistId) else NeedLoginScreen()
        }
        is Route.Root.PublicPlaylist -> NavEntry(key) {
            if (global.isLoggedIn) PublicPlaylistScreen(key.playlistId) else NeedLoginScreen()
        }

        Route.Root.CreationCenter.Publish -> NavEntry(key) {
            if (global.isLoggedIn) PublishScreen(null) else NeedLoginScreen()
        }
        is Route.Root.CreationCenter.Modify -> NavEntry(key) {
            if (global.isLoggedIn) PublishScreen(key.songId) else NeedLoginScreen()
        }
        is Route.Root.CreationCenter.ReviewDetail -> NavEntry(key) {
            if (global.isLoggedIn) {
                ReviewDetailScreen(key.reviewId, source = ReviewScreenSource.CREATION)
            } else NeedLoginScreen()
        }
        is Route.Root.CreationCenter.ReviewModify -> NavEntry(key) {
            if (global.isLoggedIn) {
                PublishScreen(songId = null, reviewId = key.reviewId)
            } else NeedLoginScreen()
        }
        is Route.Root.CreationCenter.ReviewHistory -> NavEntry(key) {
            if (global.isLoggedIn) ReviewHistoryScreen(key.reviewId) else NeedLoginScreen()
        }
        is Route.Root.CreationCenter.ArtworkDetail -> NavEntry(key) {
            if (global.isLoggedIn) ArtworkDetailScreen(key.songId) else NeedLoginScreen()
        }

        Route.Root.ContributorCenter.ReviewList -> NavEntry(key) {
            if (global.isLoggedIn) ReviewListScreen() else NeedLoginScreen()
        }
        is Route.Root.ContributorCenter.ReviewDetail -> NavEntry(key) {
            if (global.isLoggedIn) {
                ReviewDetailScreen(key.reviewId, source = ReviewScreenSource.CONTRIBUTOR)
            } else NeedLoginScreen()
        }
        is Route.Root.ContributorCenter.ReviewModify -> NavEntry(key) {
            if (global.isLoggedIn) {
                PublishScreen(songId = null, reviewId = key.reviewId)
            } else NeedLoginScreen()
        }
        is Route.Root.ContributorCenter.ReviewHistory -> NavEntry(key) {
            if (global.isLoggedIn) ReviewHistoryScreen(key.reviewId) else NeedLoginScreen()
        }
        Route.Root.ContributorCenter.CreatePost -> NavEntry(key) {
            if (global.isLoggedIn) CreatePostScreen() else NeedLoginScreen()
        }
        is Route.Root.ContributorCenter.EditPost -> NavEntry(key) { DevelopingPage() }
        Route.Root.ContributorCenter.PostCenter -> NavEntry(key) { DevelopingPage() }

        Route.Root.UserSpace -> NavEntry(key) { UserSpaceScreen(null) }
        is Route.Root.PublicUserSpace -> NavEntry(key) { UserSpaceScreen(key.userId) }
        Route.Root.EditProfile -> NavEntry(key) {
            if (global.isLoggedIn) EditProfileScreen() else NeedLoginScreen()
        }
        Route.Root.FollowingList -> NavEntry(key) {
            if (global.isLoggedIn) FollowListScreen(FollowListType.FOLLOWING) else NeedLoginScreen()
        }
        Route.Root.FollowersList -> NavEntry(key) {
            if (global.isLoggedIn) FollowListScreen(FollowListType.FOLLOWERS) else NeedLoginScreen()
        }
        Route.Root.Settings -> NavEntry(key) { SettingsScreen() }
        Route.Root.Changelog -> NavEntry(key) { ChangelogScreen() }
        Route.Root.DeviceManagement -> NavEntry(key) {
            if (global.isLoggedIn) DeviceManagementScreen() else NeedLoginScreen()
        }
        Route.Root.Notifications -> NavEntry(key) {
            if (global.isLoggedIn) NotificationInboxScreen() else NeedLoginScreen()
        }
        is Route.Root.NotificationDetail -> NavEntry(key) {
            if (global.isLoggedIn) NotificationDetailScreen(key.notificationId) else NeedLoginScreen()
        }
        Route.Root.Messages -> NavEntry(key) {
            if (global.isLoggedIn) MessageCenterScreen() else NeedLoginScreen()
        }
        Route.Root.ReceivedLikes -> NavEntry(key) {
            if (global.isLoggedIn) ReceivedLikesScreen() else NeedLoginScreen()
        }
        Route.Root.NewFollowers -> NavEntry(key) {
            if (global.isLoggedIn) NewFollowersScreen() else NeedLoginScreen()
        }
        is Route.Root.ReportCase -> NavEntry(key) {
            if (global.isLoggedIn) ReportCaseScreen(key.targetType, key.targetId) else NeedLoginScreen()
        }
        Route.Root.CommitteeMembers -> NavEntry(key) {
            if (global.isLoggedIn) CommitteeMembersScreen() else NeedLoginScreen()
        }

        else -> error("Not a secondary destination: $key")
    }

// endregion

@Composable
private fun CompactScreen(
    navigationContent: @Composable (drawerState: DrawerState) -> Unit,
    global: GlobalStore,
    navigator: Navigator,
    clientPromo: ClientPromoState,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openDrawer: () -> Unit = {
        scope.launch { drawerState.open() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = navigator.isAtRootShell,
        drawerContent = {
            ElevatedCard(
                modifier = Modifier.width(300.dp),
                color = HachimiTheme.colorScheme.surface.compositeOver(HachimiTheme.colorScheme.background),
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
            ) {
                Column(Modifier.multiplatformSystemBarsPadding()) {
                    Logo(Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp))
                    Box(Modifier.padding(12.dp)) {
                        navigationContent(drawerState)
                    }
                }
            }
        },
    ) {
        val hazeState = rememberHazeState()
        // hazeSource must be a sibling *under* the miniplayer (hazeEffect), not the parent
        // that also hosts the footer — otherwise blur samples nothing useful.
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
                    .background(HachimiTheme.colorScheme.background)
            ) {
                CompositionLocalProvider(
                    // LocalContentInsets = MiniPlayer bottom only.
                    // Status top: listHeadInsetsSpacerItem / multiplatformStatusBarsPadding
                    // (consumed under AppBar → 0).
                    LocalContentInsets provides WindowInsets(
                        bottom = CompactFooterHeight + 24.dp
                    ),
                    LocalOpenNavigationDrawer provides openDrawer,
                ) {
                    RootNavDisplay(
                        global = global,
                        navigator = navigator,
                        shell = { primaryContent ->
                            // Root shell: shared AppBar + nested primary NavDisplay
                            Column(Modifier.fillMaxSize()) {
                                CompactTopAppBar(
                                    modifier = Modifier.zIndex(2f).fillMaxWidth(),
                                    global = global,
                                    onExpandNavClick = openDrawer,
                                )
                                CompactClientPromoBanner(clientPromo)
                                CompactUpdateBanner(global.updates)
                                // consume for *descendants* (not AppBar siblings): body must not
                                // re-apply multiplatform top insets already handled by AppBar.
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .consumeWindowInsets(WindowInsets.multiplatformStatusBars)
                                ) {
                                    primaryContent()
                                }
                            }
                        },
                    )
                }
            }

            CompactMiniPlayer(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight(align = Alignment.Bottom)
                    .padding(24.dp)
                    .navigationBarsPadding(),
                hazeState = hazeState,
            )
        }
    }
}

@Composable
private fun ExpandedScreen(
    navigationContent: @Composable () -> Unit,
    global: GlobalStore,
    navigator: Navigator,
) {
    val hazeState = rememberHazeState()

    ExpandedScaffoldLayout(
        modifier = Modifier.fillMaxSize(),
        navigation = {
            Column(
                Modifier
                    .fillMaxHeight()
                    .multiplatformStatusBarsPadding()
                    .padding(start = 24.dp, top = 4.dp, bottom = 24.dp)
            ) {
                Logo()
                ElevatedCard(Modifier.width(180.dp).weight(1f)) {
                    Box(Modifier.padding(8.dp).fillMaxSize()) {
                        navigationContent()
                    }
                }
            }
        },
        footerPlayer = {
            ExpandedMiniPlayer(
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
                        start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                        end = contentPadding.calculateEndPadding(LocalLayoutDirection.current)
                    )
            ) {
                // LocalContentInsets = MiniPlayer bottom only.
                // Status top: listHeadInsetsSpacerItem / multiplatformStatusBarsPadding
                // (Compact AppBar consumes → 0; Expanded primary still sees remaining insets).
                val footerBottom = contentPadding.calculateBottomPadding()
                CompositionLocalProvider(
                    LocalContentInsets provides WindowInsets(
                        bottom = footerBottom,
                    )
                ) {
                    RootNavDisplay(
                        global = global,
                        navigator = navigator,
                        shell = { primaryContent ->
                            // Full-bleed; lists use listHeadInsetsSpacerItem for safe top.
                            primaryContent()
                        },
                    )
                }
            }
        }
    )
}
