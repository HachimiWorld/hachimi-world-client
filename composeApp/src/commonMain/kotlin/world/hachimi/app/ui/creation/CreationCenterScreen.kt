package world.hachimi.app.ui.creation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.creation.artwork.MyArtworkScreen
import world.hachimi.app.ui.creation.publish.PublishScreen

@Composable
fun CreationCenterScreen(
    child: Route.Root.CreationCenter
) {
    AnimatedContent(child, modifier = Modifier.fillMaxSize()) { child ->
        when (child) {
            Route.Root.CreationCenter.MyArtwork -> MyArtworkScreen()
            Route.Root.CreationCenter.Publish -> PublishScreen()
        }
    }
}