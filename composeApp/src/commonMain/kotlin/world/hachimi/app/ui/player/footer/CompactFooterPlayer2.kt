package world.hachimi.app.ui.player.footer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.chrisbanes.haze.HazeState
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.player_not_playing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.LocalAnimatedVisibilityScope
import world.hachimi.app.ui.LocalSharedTransitionScope
import world.hachimi.app.ui.SharedTransitionKeys
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.player.footer.components.Author
import world.hachimi.app.ui.player.footer.components.Container
import world.hachimi.app.ui.player.footer.components.PlayPauseButton
import world.hachimi.app.ui.player.footer.components.PlayPauseStatus
import world.hachimi.app.ui.player.footer.components.Title
import world.hachimi.app.ui.player.fullscreen.components.FullScreenCoverCornerRadius
import world.hachimi.app.ui.theme.PreviewTheme


val CompactFooterHeight = 48.dp + 16.dp

@Composable
fun CompactFooterPlayer2(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    global: GlobalStore = koinInject(),
) {
    val uiState = global.player.playerState
    AnimatedVisibility(visible = !global.playerExpanded) {
        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@AnimatedVisibility) {
            Container(
                modifier = modifier,
                hazeState = hazeState,
                onClick = { global.expandPlayer() }
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Cover(uiState.displayedCover)

                    Column(Modifier.weight(1f)) {
                        val title =
                            uiState.displayedTitle.ifBlank { stringResource(Res.string.player_not_playing) }
                        val author =
                            uiState.displayedAuthor.ifBlank { stringResource(Res.string.player_not_playing) }
                        Title(title)
                        Author(author)
                    }

                    PlayPauseButton(
                        modifier = Modifier.size(48.dp),
                        status = when {
                            uiState.fetchingMetadata -> PlayPauseStatus.Fetching
                            uiState.isPlaying -> PlayPauseStatus.Playing
                            else -> PlayPauseStatus.Paused
                        },
                        onClick = {
                            global.player.playOrPause()
                        }
                    )

                    Button(
                        modifier = Modifier.size(48.dp),
                        onClick = { global.player.next() },
                        contentPadding = PaddingValues.Zero
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip Next",
                            tint = HachimiTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Cover(
    model: String?,
    modifier: Modifier = Modifier
) {
    val animatedVisibility = LocalAnimatedVisibilityScope.current

    val cornerRadius by animatedVisibility.transition.animateDp(label = "rounded corner") { enterExitState ->
        when (enterExitState) {
            EnterExitState.PreEnter -> FullScreenCoverCornerRadius
            EnterExitState.Visible -> FooterPlayerCoverCornerRadius
            EnterExitState.PostExit -> FullScreenCoverCornerRadius
        }
    }

    with(LocalSharedTransitionScope.current) {
        Box(
            modifier
                .sharedElement(
                    rememberSharedContentState(SharedTransitionKeys.Cover),
                    LocalAnimatedVisibilityScope.current,
                    zIndexInOverlay = 2f
                )
                .size(48.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color.Gray)
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .httpHeaders(CoilHeaders)
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

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = false) {

    }
}