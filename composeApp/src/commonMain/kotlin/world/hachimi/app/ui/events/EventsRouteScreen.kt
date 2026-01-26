package world.hachimi.app.ui.events

import androidx.compose.runtime.Composable
import world.hachimi.app.nav.Route

@Composable
fun EventsRouteScreen(routeContent: Route.Root.Events) {
    when (routeContent) {
        is Route.Root.Events.Detail -> EventDetailScreen(routeContent.postId)
        Route.Root.Events.Feed -> EventsScreen()
    }
}