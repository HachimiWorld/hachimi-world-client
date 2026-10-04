package world.hachimi.app.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

val LocalNavigator = compositionLocalOf<Navigator> { error("not provided") }

/** App-level host that owns [world.hachimi.app.ui.root.RootScreen]. */
@Serializable
data object RootHostNavKey : NavKey

/**
 * Shell destination inside [rootBackStack]: CompactTopAppBar + nested primary NavDisplay.
 * Secondary routes sit as siblings on [rootBackStack] above this key (Activity-style).
 */
@Serializable
data object RootShellNavKey : NavKey

sealed interface NavigationRequest {
    data object Back : NavigationRequest

    data class Push(val route: NavKey) : NavigationRequest

    data class NavigateTo(val route: NavKey) : NavigationRequest

    data class Replace(val routes: List<NavKey>) : NavigationRequest
}

/**
 * Nested navigation model:
 * ```
 * topLevelBackStack: Auth | RootHostNavKey
 * rootBackStack:     RootShellNavKey | secondary Route.Root…   // outer NavDisplay
 * primaryBackStack:  SideNav destinations only                  // nested NavDisplay in shell
 * ```
 */
class Navigator(start: NavKey) {
    val topLevelBackStack: SnapshotStateList<NavKey> = mutableStateListOf()

    /** Outer stack under RootHost: shell + full-screen secondary routes. */
    val rootBackStack: SnapshotStateList<NavKey> = mutableStateListOf()

    /** Inner SideNav destinations, only active when shell is showing. */
    val primaryBackStack: SnapshotStateList<Route.Root> = mutableStateListOf()

    init {
        pushInternal(start)
    }

    val currentPrimary: Route.Root?
        get() = primaryBackStack.lastOrNull()

    val currentRoot: NavKey?
        get() = rootBackStack.lastOrNull()

    val isAtRootShell: Boolean
        get() = currentRoot == RootShellNavKey

    val backStack: List<NavKey>
        get() = buildList {
            addAll(primaryBackStack)
            addAll(rootBackStack.filterNot { it == RootShellNavKey })
            addAll(topLevelBackStack.filterNot { it == RootHostNavKey })
        }

    val canGoBack: Boolean
        get() = rootBackStack.size > 1 || topLevelBackStack.size > 1

    fun replace(vararg routes: NavKey) {
        Snapshot.withMutableSnapshot {
            topLevelBackStack.clear()
            rootBackStack.clear()
            primaryBackStack.clear()
            routes.forEach(::pushInternal)
        }
    }

    fun navigateTo(route: NavKey) {
        Snapshot.withMutableSnapshot {
            when (route) {
                is Route.Root -> {
                    ensureRootHost()
                    if (route.isSideNavDestination()) {
                        popToRootShell()
                        setPrimary(route)
                    } else {
                        ensureRootShellBase()
                        if (rootBackStack.lastOrNull() == RootShellNavKey) {
                            rootBackStack.add(route)
                        } else {
                            rootBackStack[rootBackStack.lastIndex] = route
                        }
                    }
                }

                else -> {
                    if (topLevelBackStack.isEmpty()) {
                        topLevelBackStack.add(route)
                    } else {
                        topLevelBackStack[topLevelBackStack.lastIndex] = route
                    }
                }
            }
        }
    }

    fun push(route: NavKey) {
        Snapshot.withMutableSnapshot {
            pushInternal(route)
        }
    }

    /** SideNav switch: pop to shell, replace primary destination. */
    fun switchPrimary(route: Route.Root) {
        require(route.isSideNavDestination()) {
            "switchPrimary only accepts side-nav destinations, got $route"
        }
        Snapshot.withMutableSnapshot {
            ensureRootHost()
            popToRootShell()
            setPrimary(route)
        }
    }

    fun back() {
        Snapshot.withMutableSnapshot {
            when (topLevelBackStack.lastOrNull()) {
                RootHostNavKey -> {
                    if (rootBackStack.size > 1) {
                        rootBackStack.removeLastOrNull()
                    } else if (topLevelBackStack.size > 1) {
                        topLevelBackStack.removeLastOrNull()
                    }
                }

                null -> Unit
                else -> if (topLevelBackStack.size > 1) {
                    topLevelBackStack.removeLastOrNull()
                }
            }
        }
    }

    private fun pushInternal(route: NavKey) {
        when (route) {
            is Route.Root -> {
                ensureRootHost()
                if (route.isSideNavDestination()) {
                    popToRootShell()
                    setPrimary(route)
                } else {
                    ensureRootShellBase()
                    rootBackStack.add(route)
                }
            }

            else -> topLevelBackStack.add(route)
        }
    }

    private fun ensureRootHost() {
        if (topLevelBackStack.lastOrNull() != RootHostNavKey) {
            topLevelBackStack.add(RootHostNavKey)
        }
        ensureRootShellBase()
        if (primaryBackStack.isEmpty()) {
            primaryBackStack.add(Route.Root.Default)
        }
    }

    private fun ensureRootShellBase() {
        if (rootBackStack.none { it == RootShellNavKey }) {
            rootBackStack.add(0, RootShellNavKey)
        } else if (rootBackStack.isEmpty()) {
            rootBackStack.add(RootShellNavKey)
        }
    }

    private fun popToRootShell() {
        ensureRootShellBase()
        while (rootBackStack.size > 1) {
            rootBackStack.removeLastOrNull()
        }
        if (rootBackStack.firstOrNull() != RootShellNavKey) {
            rootBackStack.clear()
            rootBackStack.add(RootShellNavKey)
        }
    }

    private fun setPrimary(route: Route.Root) {
        if (primaryBackStack.isEmpty()) {
            primaryBackStack.add(route)
        } else {
            primaryBackStack[primaryBackStack.lastIndex] = route
        }
    }
}

fun Navigator.handle(request: NavigationRequest) {
    when (request) {
        NavigationRequest.Back -> back()
        is NavigationRequest.Push -> push(request.route)
        is NavigationRequest.NavigateTo -> navigateTo(request.route)
        is NavigationRequest.Replace -> replace(*request.routes.toTypedArray())
    }
}

@Composable
fun HandleNavigationRequests(
    requests: Flow<NavigationRequest>,
    navigator: Navigator = LocalNavigator.current
) {
    LaunchedEffect(requests, navigator) {
        requests.collect(navigator::handle)
    }
}
