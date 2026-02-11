package world.hachimi.app.nav

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

class Navigator(start: NavKey) {
    val backStack: SnapshotStateList<NavKey> = mutableStateListOf(start)

    fun replace(vararg routes: NavKey) {
        Snapshot.withMutableSnapshot {
            backStack.clear()
            backStack.addAll(routes)
        }
    }

    fun navigateTo(route: NavKey) {
        Snapshot.withMutableSnapshot {
            backStack.removeLastOrNull()
            backStack.add(route)
        }
    }

    fun push(route: NavKey) {
        backStack.add(route)
    }

    fun back() {
        Snapshot.withMutableSnapshot {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        }
    }
}