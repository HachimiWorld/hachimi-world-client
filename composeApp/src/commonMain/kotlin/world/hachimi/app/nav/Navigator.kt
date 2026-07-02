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

@Serializable
data object RootHostNavKey : NavKey

sealed interface NavigationRequest {
    data object Back : NavigationRequest

    data class Push(val route: NavKey) : NavigationRequest

    data class NavigateTo(val route: NavKey) : NavigationRequest

    data class Replace(val routes: List<NavKey>) : NavigationRequest
}

class Navigator(start: NavKey) {
    val topLevelBackStack: SnapshotStateList<NavKey> = mutableStateListOf()
    val rootBackStack: SnapshotStateList<Route.Root> = mutableStateListOf()

    init {
        pushInternal(start)
    }

    val backStack: List<NavKey>
        get() = buildList {
            addAll(rootBackStack)
            addAll(topLevelBackStack.filterNot { it == RootHostNavKey })
        }

    val canGoBack: Boolean
        get() = backStack.size > 1

    fun replace(vararg routes: NavKey) {
        Snapshot.withMutableSnapshot {
            topLevelBackStack.clear()
            rootBackStack.clear()
            routes.forEach(::pushInternal)
        }
    }

    fun navigateTo(route: NavKey) {
        Snapshot.withMutableSnapshot {
            removeCurrentInternal()
            pushInternal(route)
        }
    }

    fun push(route: NavKey) {
        Snapshot.withMutableSnapshot {
            pushInternal(route)
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
                if (topLevelBackStack.lastOrNull() != RootHostNavKey) {
                    topLevelBackStack.add(RootHostNavKey)
                }
                rootBackStack.add(route)
            }

            else -> topLevelBackStack.add(route)
        }
    }

    private fun removeCurrentInternal() {
        when (topLevelBackStack.lastOrNull()) {
            RootHostNavKey -> {
                if (rootBackStack.isNotEmpty()) {
                    rootBackStack.removeLastOrNull()
                    if (rootBackStack.isEmpty()) {
                        topLevelBackStack.removeLastOrNull()
                    }
                } else {
                    topLevelBackStack.removeLastOrNull()
                }
            }

            null -> Unit
            else -> topLevelBackStack.removeLastOrNull()
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