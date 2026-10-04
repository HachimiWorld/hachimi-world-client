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
    Route.Root.Events.Feed,
    Route.Root.Home.Main,
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
