package world.hachimi.app.ui.player.footer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.LocalAnimatedVisibilityScope
import world.hachimi.app.ui.LocalSharedTransitionScope
import world.hachimi.app.ui.SharedTransitionKeys
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.player.footer.components.Author
import world.hachimi.app.ui.player.footer.components.Title
import world.hachimi.app.ui.theme.PreviewTheme

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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Cover(uiState.displayedCover)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Title(uiState.displayedTitle)
                        Author(uiState.displayedAuthor)
                    }
                    Spacer(Modifier.width(8.dp))
                    PlayPauseButton(
                        modifier = Modifier.size(48.dp),
                        playing = uiState.isPlaying,
                        onClick = {
                            global.player.playOrPause()
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(modifier = Modifier.size(48.dp), onClick = { global.player.next() }) {
                        Icon(
                            modifier = Modifier,
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
    model: Any?,
    modifier: Modifier = Modifier
) {
    with(LocalSharedTransitionScope.current) {
        Box(
            modifier
                .sharedElement(
                    rememberSharedContentState(SharedTransitionKeys.Cover),
                    LocalAnimatedVisibilityScope.current
                )
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray)
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = model,
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