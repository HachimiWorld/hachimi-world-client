package world.hachimi.app.ui.player.footer.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import world.hachimi.app.ui.design.components.AccentButton

@Composable
fun PlayPauseButton(
    modifier: Modifier,
    playing: Boolean,
    onClick: () -> Unit
) {
    AccentButton(
        modifier = modifier,
        onClick = onClick
    ) {
        if (playing) Icon(
            Icons.Default.Pause,
            contentDescription = "Pause"
        )
        else Icon(
            Icons.Default.PlayArrow,
            contentDescription = "Play"
        )
    }
}
