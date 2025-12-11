package world.hachimi.app.ui.player.footer.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Icon

@Composable
fun PlayPauseButton(
    modifier: Modifier,
    playing: Boolean,
    onClick: () -> Unit
) {
    AccentButton(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues.Zero
    ) {
        if (playing) Icon(
            Icons.Default.Pause,
            contentDescription = "Pause",
            modifier = Modifier.size(24.dp),
        )
        else Icon(
            Icons.Default.PlayArrow,
            contentDescription = "Play",
            modifier = Modifier.size(24.dp),
        )
    }
}
