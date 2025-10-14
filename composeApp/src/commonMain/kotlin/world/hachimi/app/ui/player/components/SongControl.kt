package world.hachimi.app.ui.player.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SongControl(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    isLoading: Boolean,
    shuffle: Boolean,
    onShuffleModeChange: (Boolean) -> Unit,
    repeat: Boolean,
    onRepeatModeChange: (Boolean) -> Unit,
    loadingProgress: () -> Float,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        IconButton(onClick = {
            onShuffleModeChange(!shuffle)
        }) {
            if (shuffle) Icon(Icons.Default.ShuffleOn, "Shuffle On")
            else Icon(Icons.Default.Shuffle, "Shuffle Off")
        }
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.Default.SkipPrevious, "Skip Previous")
        }
        IconButton(onClick = onPlayPauseClick, colors = IconButtonDefaults.filledIconButtonColors()) {
            if (isLoading) {
                val animatedProgress by animateFloatAsState(targetValue = loadingProgress())
                val showProgress = animatedProgress > 0f && animatedProgress < 1f

                Crossfade(showProgress) {
                    if (it) CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = LocalContentColor.current,
                        trackColor = LocalContentColor.current.copy(alpha = 0.12f),
                        strokeWidth = 2.dp,
                        progress = { animatedProgress }
                    ) else CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = LocalContentColor.current,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                if (isPlaying) {
                    Icon(Icons.Default.Pause, "Pause")
                } else {
                    Icon(Icons.Default.PlayArrow, "Play")
                }
            }
        }
        IconButton(onClick = onNextClick) {
            Icon(Icons.Default.SkipNext, "Skip Next")
        }
        IconButton(onClick = {
            onRepeatModeChange(!repeat)
        }) {
            if (repeat) Icon(Icons.Default.RepeatOn, "Repeat On")
            else Icon(Icons.Default.Repeat, "Repeat Off")
        }
    }
}