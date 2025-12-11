package world.hachimi.app.ui.home

import androidx.compose.runtime.Composable
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.DevelopingPage

@Composable
fun HomeScreen(content: Route.Root.Home) {
    when (content) {
        Route.Root.Home.Main -> HomeMainScreen()
        Route.Root.Home.Recent -> RecentPublishScreen()
        Route.Root.Home.Recommend -> RecommendScreen()
        Route.Root.Home.WeeklyHot -> WeeklyHotScreen()
        is Route.Root.Home.Category -> CategorySongsScreen(content.category)
        else -> DevelopingPage()
    }
}