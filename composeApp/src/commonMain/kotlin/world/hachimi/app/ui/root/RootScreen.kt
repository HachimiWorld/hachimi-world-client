package world.hachimi.app.ui.root

import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.component.LocalPageTitleShown
import org.jetbrains.compose.resources.StringResource
import hachimiworld.composeapp.generated.resources.nav_recent_play
import hachimiworld.composeapp.generated.resources.nav_recent_like
import hachimiworld.composeapp.generated.resources.nav_my_subscribe
import hachimiworld.composeapp.generated.resources.nav_my_playlist
import hachimiworld.composeapp.generated.resources.nav_creation_center
import hachimiworld.composeapp.generated.resources.nav_committee_center
import hachimiworld.composeapp.generated.resources.nav_contributor_center
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_back_cd
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.nav.BottomTab
import world.hachimi.app.nav.bottomTab
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.me.MeScreen
import world.hachimi.app.ui.root.component.CompactBottomBar
import world.hachimi.app.ui.root.component.CompactBottomBarHeight
import world.hachimi.app.util.PlatformBackHandler
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
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
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.LocalSharedTransitionScope
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.committee.CommitteeCenterScreen
import world.hachimi.app.ui.committee.CommitteeMembersScreen
import world.hachimi.app.ui.committee.ReportCaseScreen
import world.hachimi.app.ui.component.DevelopingPage
import world.hachimi.app.ui.report.ReportDialogHost
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
import world.hachimi.app.ui.moment.CreateMomentScreen
import world.hachimi.app.ui.moment.MomentDetailScreen
import world.hachimi.app.ui.square.SquareScreen
import world.hachimi.app.ui.square.SquareTab
import world.hachimi.app.ui.follow.FollowListScreen
import world.hachimi.app.ui.home.CategorySongsScreen
import world.hachimi.app.ui.home.HomeMainScreen
import world.hachimi.app.ui.home.RecentPublishScreen
import world.hachimi.app.ui.home.RecommendScreen
import world.hachimi.app.ui.home.WeeklyHotScreen
import world.hachimi.app.ui.insets.multiplatformStatusBars
import world.hachimi.app.ui.insets.multiplatformStatusBarsPadding
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
import world.hachimi.app.ui.root.component.ExpandedClientPromoCard
import world.hachimi.app.ui.root.component.CompactUpdateBanner
import world.hachimi.app.ui.root.component.ExpandedScaffoldLayout
import world.hachimi.app.ui.root.component.ExpandedSideNavigation
import world.hachimi.app.ui.root.component.ExpandedUpdateCard
import world.hachimi.app.ui.root.component.rememberClientPromoState
import world.hachimi.app.ui.search.SearchScreen
import world.hachimi.app.ui.settings.ChangelogScreen
import world.hachimi.app.ui.settings.ChangeEmailScreen
import world.hachimi.app.ui.settings.DeviceManagementScreen
import world.hachimi.app.ui.notification.NotificationDetailScreen
import world.hachimi.app.ui.notification.NotificationInboxScreen
import world.hachimi.app.ui.comment.SongCommentsScreen
import world.hachimi.app.ui.message.MessageCenterScreen
import world.hachimi.app.ui.message.NewFollowersScreen
import world.hachimi.app.ui.message.ReceivedLikesScreen
import world.hachimi.app.ui.message.ReceivedRepliesScreen
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
    val clientPromo = rememberClientPromoState()
    val messageCenter = koinInject<MessageCenterStore>()
    LifecycleResumeEffect(messageCenter) {
        messageCenter.refreshSummary()
        onPauseOrDispose {}
    }

    if (LocalWindowSize.current.width < WindowSize.COMPACT) {
        CompactScreen(
            global = global,
            navigator = navigator,
            clientPromo = clientPromo,
            meUnread = (messageCenter.totalUnread ?: 0) > 0,
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
        Route.Root.Square -> NavEntry(key) { SquareScreen() }
        Route.Root.Events.Feed -> NavEntry(key) { SquareScreen(initialTab = SquareTab.ANNOUNCEMENTS) }
        Route.Root.Home.Main -> NavEntry(key) { HomeMainScreen() }
        Route.Root.Me -> NavEntry(key) { MeScreen() }
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

        is Route.Root.Events.Detail -> NavEntry(key) { EventDetailScreen(key.postId, key.atComments) }

        is Route.Root.MomentDetail -> NavEntry(key) { MomentDetailScreen(key.momentId, key.atComments) }
        Route.Root.CreateMoment -> NavEntry(key) {
            if (global.isLoggedIn) CreateMomentScreen() else NeedLoginScreen()
        }

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
        Route.Root.ChangeEmail -> NavEntry(key) {
            if (global.isLoggedIn) ChangeEmailScreen() else NeedLoginScreen()
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
        Route.Root.ReceivedReplies -> NavEntry(key) {
            if (global.isLoggedIn) ReceivedRepliesScreen() else NeedLoginScreen()
        }
        is Route.Root.SongComments -> NavEntry(key) { SongCommentsScreen(key.songDisplayId) }
        is Route.Root.ReportCase -> NavEntry(key) {
            if (global.isLoggedIn) ReportCaseScreen(key.targetType, key.targetId) else NeedLoginScreen()
        }
        Route.Root.CommitteeMembers -> NavEntry(key) {
            if (global.isLoggedIn) CommitteeMembersScreen() else NeedLoginScreen()
        }

        else -> error("Not a secondary destination: $key")
    }

// endregion

/**
 * The phone's shell: pages run to the top of the screen with no bar over them, and the mini
 * player floats above a bottom bar of three tabs.
 */
@Composable
private fun CompactScreen(
    global: GlobalStore,
    navigator: Navigator,
    clientPromo: ClientPromoState,
    meUnread: Boolean,
) {
    val currentPrimary = navigator.currentPrimary ?: return
    val barVisible = navigator.isAtRootShell || (navigator.currentRoot as? Route.Root)?.keepsBottomBar() == true
    // What pages leave free at the bottom: the mini player, the gaps around it, and the bar
    val footer by animateDpAsState(
        MiniPlayerGapAbove + CompactFooterHeight + MiniPlayerGapBelow + if (barVisible) CompactBottomBarHeight else 8.dp
    )

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
                // LocalContentInsets = what the footer covers, without the system bar.
                // Status top: listHeadInsetsSpacerItem / multiplatformStatusBarsPadding
                // (consumed by the shell → 0).
                LocalContentInsets provides WindowInsets(bottom = footer),
            ) {
                RootNavDisplay(
                    global = global,
                    navigator = navigator,
                    shell = { primaryContent ->
                        // Root shell: the status bar's room, banners, then the nested primary NavDisplay
                        Column(Modifier.fillMaxSize()) {
                            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.multiplatformStatusBars))
                            CompactClientPromoBanner(clientPromo)
                            CompactUpdateBanner(global.updates)
                            // consume for *descendants*: the body must not re-apply the top
                            // inset the spacer above already took.
                            Column(
                                Modifier
                                    .weight(1f)
                                    .consumeWindowInsets(WindowInsets.multiplatformStatusBars)
                            ) {
                                val tab = currentPrimary.bottomTab()
                                val underMe = tab == BottomTab.ME && currentPrimary != Route.Root.Me
                                // Back leads up: from a page under Me to Me, from another tab to home
                                PlatformBackHandler(enabled = navigator.isAtRootShell && currentPrimary != Route.Root.Default) {
                                    navigator.switchPrimary(if (underMe) Route.Root.Me else Route.Root.Default)
                                }
                                val underMeTitle = if (underMe) currentPrimary.meSubpageTitle() else null
                                if (underMeTitle != null) {
                                    // Pages kept under Me read as pages of their own: a bar with the way
                                    // back to Me, and their title large at the top, folding into it
                                    ScreenScaffold(
                                        title = { Text(stringResource(underMeTitle), maxLines = 1) },
                                        showBack = true,
                                        onBack = { navigator.switchPrimary(Route.Root.Me) },
                                    ) {
                                        CompositionLocalProvider(LocalPageTitleShown provides true) {
                                            Box(Modifier.weight(1f)) { primaryContent() }
                                        }
                                    }
                                } else {
                                    Box(Modifier.weight(1f)) { primaryContent() }
                                }
                            }
                        }
                    },
                )
            }
        }

        val background = HachimiTheme.colorScheme.background
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().drawBehind {
                // Behind the mini player the page fades out, and is gone by the gap above the
                // bar, so the bar needs no line over it
                if (barVisible) {
                    val solidFrom = CompactFooterHeight.toPx()
                    val fadeFrom = solidFrom - 40.dp.toPx()
                    drawRect(
                        Brush.verticalGradient(listOf(Color.Transparent, background), fadeFrom, solidFrom),
                        topLeft = Offset(0f, fadeFrom),
                        size = Size(size.width, size.height - fadeFrom),
                    )
                }
            }
        ) {
            CompactMiniPlayer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = MiniPlayerGapBelow),
                hazeState = hazeState,
            )
            AnimatedVisibility(
                visible = barVisible,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top),
            ) {
                CompactBottomBar(
                    selected = currentPrimary.bottomTab(),
                    onSelect = { navigator.switchPrimary(it.route) },
                    avatarUrl = global.userInfo?.avatarUrl,
                    meUnread = meUnread,
                )
            }
            // With the bar away, the player still clears the system bar
            if (!barVisible) Spacer(Modifier.navigationBarsPadding().height(8.dp))
        }
    }
}

private val MiniPlayerGapAbove = 12.dp
private val MiniPlayerGapBelow = 8.dp

/**
 * Secondary pages that are for browsing keep the bottom bar; the ones for a task of their own
 * (writing, editing, reading a thread) take the whole screen.
 */
private fun Route.Root.keepsBottomBar(): Boolean = when (this) {
    is Route.Root.Home,
    is Route.Root.Search,
    is Route.Root.MyPlaylist.Detail,
    is Route.Root.PublicPlaylist,
    Route.Root.UserSpace,
    is Route.Root.PublicUserSpace,
    -> true

    else -> false
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

/** The title a page reached from Me shows over itself on a phone; null for pages that bring their own bar. */
private fun Route.Root.meSubpageTitle(): StringResource? = when (this) {
    Route.Root.RecentPlay -> Res.string.nav_recent_play
    Route.Root.RecentLike -> Res.string.nav_recent_like
    Route.Root.MySubscribe -> Res.string.nav_my_subscribe
    Route.Root.MyPlaylist.List -> Res.string.nav_my_playlist
    Route.Root.CreationCenter.MyArtwork -> Res.string.nav_creation_center
    Route.Root.CommitteeCenter -> Res.string.nav_committee_center
    Route.Root.ContributorCenter.Entry -> Res.string.nav_contributor_center
    else -> null
}
