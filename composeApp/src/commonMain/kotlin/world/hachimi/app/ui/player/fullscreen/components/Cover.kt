package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import world.hachimi.app.ui.LocalAnimatedVisibilityScope
import world.hachimi.app.ui.LocalSharedTransitionScope
import world.hachimi.app.ui.SharedTransitionKeys

@Composable
fun Cover(
    model: String?,
    modifier: Modifier = Modifier
) {
    with(LocalSharedTransitionScope.current) {
        Box(
            modifier
                .sharedElement(
                    rememberSharedContentState(SharedTransitionKeys.Cover),
                    LocalAnimatedVisibilityScope.current,
                    // Ensure the cover displayed on the background, because the background zIndex == 1f
                    zIndexInOverlay = 2f
                )
                .size(256.dp)
                .shadow(16.dp, RoundedCornerShape(8.dp))
                .background(Color(0xFFDEDEDE), RoundedCornerShape(8.dp))
                // FIXME: The dropShadow causes memory leak
                /*.dropShadow(
                    RoundedCornerShape(8.dp), Shadow(
                        radius = 24.dp, color = Color.Black.copy(0.17f),
                        offset = DpOffset(0.dp, 2.dp)
                    )
                )*/
                .clip(RoundedCornerShape(8.dp))
        ) {
            Crossfade(model) { model ->
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(model)
                        .crossfade(true)
                        .placeholderMemoryCacheKey(model)
                        .memoryCacheKey(model)
                        .build(),
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
