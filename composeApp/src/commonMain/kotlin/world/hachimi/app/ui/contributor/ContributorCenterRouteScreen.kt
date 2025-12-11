package world.hachimi.app.ui.contributor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import world.hachimi.app.nav.Route

@Composable
fun ContributorCenterScreen(child: Route.Root.ContributorCenter) {
    Box(Modifier.fillMaxSize()) {
        when (child) {
            Route.Root.ContributorCenter.ReviewList -> ReviewListScreen()
            is Route.Root.ContributorCenter.ReviewDetail -> ReviewDetailScreen(child.reviewId)
        }
    }
}