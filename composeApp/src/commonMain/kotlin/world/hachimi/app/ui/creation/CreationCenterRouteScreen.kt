package world.hachimi.app.ui.creation

import androidx.compose.runtime.Composable
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.contributor.ReviewDetailScreen
import world.hachimi.app.ui.contributor.ReviewHistoryScreen
import world.hachimi.app.ui.contributor.ReviewScreenSource
import world.hachimi.app.ui.creation.artwork.MyArtworkScreen
import world.hachimi.app.ui.creation.artworkdetail.ArtworkDetailScreen
import world.hachimi.app.ui.creation.publish.PublishScreen

@Composable
fun CreationCenterScreen(
    child: Route.Root.CreationCenter
) {
    when (child) {
        Route.Root.CreationCenter.MyArtwork -> MyArtworkScreen()
        Route.Root.CreationCenter.Publish -> PublishScreen(null)
        is Route.Root.CreationCenter.Modify -> PublishScreen(child.songId)
        is Route.Root.CreationCenter.ReviewDetail -> ReviewDetailScreen(child.reviewId, source = ReviewScreenSource.CREATION)
        is Route.Root.CreationCenter.ReviewModify -> PublishScreen(songId = null, reviewId = child.reviewId)
        is Route.Root.CreationCenter.ReviewHistory -> ReviewHistoryScreen(child.reviewId)
        is Route.Root.CreationCenter.ArtworkDetail -> ArtworkDetailScreen(child.songId)
    }
}