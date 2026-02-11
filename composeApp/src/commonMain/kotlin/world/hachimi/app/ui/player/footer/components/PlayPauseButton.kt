package world.hachimi.app.ui.player.footer.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import soup.compose.material.motion.animation.materialFadeIn
import soup.compose.material.motion.animation.materialFadeOut
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.Icon

enum class PlayPauseStatus {
    Playing, Paused, Fetching
}

@Composable
fun PlayPauseButton(
    status: PlayPauseStatus,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AccentButton(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues.Zero
    ) {
        AnimatedContent(
            targetState = status,
            transitionSpec = { materialFadeIn() togetherWith materialFadeOut() }
        ) { status ->
            when (status) {
                PlayPauseStatus.Playing -> Icon(
                    Icons.Default.Pause,
                    contentDescription = "Pause",
                    modifier = Modifier.size(24.dp),
                )

                PlayPauseStatus.Paused -> Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(24.dp),
                )

                PlayPauseStatus.Fetching -> CircularProgressIndicator(
                    Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
