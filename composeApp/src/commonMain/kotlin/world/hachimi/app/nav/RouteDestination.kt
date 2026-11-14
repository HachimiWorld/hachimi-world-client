package world.hachimi.app.nav

/**
 * Top-level destinations listed in Expanded/Compact side navigation.
 *
 * These live on [Navigator.primaryBackStack] inside [RootShellNavKey]
 * (shared CompactTopAppBar + nested NavDisplay).
 *
 * All other [Route.Root] values are secondary routes on [Navigator.rootBackStack]
 * as siblings of [RootShellNavKey] — full-screen, Activity-style.
 */
fun Route.Root.isSideNavDestination(): Boolean = when (this) {
    Route.Root.Square,
    Route.Root.Events.Feed,
    Route.Root.Home.Main,
    Route.Root.Me,
    Route.Root.RecentPlay,
    Route.Root.RecentLike,
    Route.Root.MySubscribe,
    is Route.Root.MyPlaylist.List,
    is Route.Root.CreationCenter.MyArtwork,
    Route.Root.CommitteeCenter,
    is Route.Root.ContributorCenter.Entry,
    -> true

    else -> false
}

/**
 * The three tabs of the phone's bottom bar.
 *
 * @since 261110
 */
enum class BottomTab(val route: Route.Root) {
    HOME(Route.Root.Home.Main),
    SQUARE(Route.Root.Square),
    ME(Route.Root.Me),
}

/**
 * The tab a primary destination belongs to. Everything that is neither the home page nor the
 * square is reached from [BottomTab.ME].
 *
 * @since 261110
 */
fun Route.Root.bottomTab(): BottomTab = when (this) {
    Route.Root.Home.Main -> BottomTab.HOME
    Route.Root.Square, Route.Root.Events.Feed -> BottomTab.SQUARE
    else -> BottomTab.ME
}
