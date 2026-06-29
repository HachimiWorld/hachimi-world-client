package world.hachimi.app.ui.userspace.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.user_space_user_avatar_cd
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun Avatar(avatarUrl: String?, size: Dp = 120.dp) {
    Surface(
        modifier = Modifier
            .size(size)
            .shadow(12.dp, CircleShape)
            .border(3.dp, Color.White, CircleShape),
        shape = CircleShape
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .httpHeaders(CoilHeaders)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(Res.string.user_space_user_avatar_cd),
            modifier = Modifier.fillMaxSize(),
            filterQuality = FilterQuality.High,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
@Preview
private fun Preview() {
    PreviewTheme(background = true) {
        Avatar(avatarUrl = null, size = 80.dp)
    }
}
