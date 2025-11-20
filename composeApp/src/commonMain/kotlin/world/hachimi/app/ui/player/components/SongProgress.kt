package world.hachimi.app.ui.player.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import world.hachimi.app.util.formatSongDuration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SongProgress(
    durationMillis: Long,
    currentMillis: Long,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    bufferedProgress: Float = 0f,
) {
    var isDragging by remember { mutableStateOf(false) }
    val playingProgress = (currentMillis.toDouble() / durationMillis).toFloat().coerceIn(0f, 1f)
    val animatedPlayingProgress by animateFloatAsState(targetValue = playingProgress, tween(durationMillis = 100, easing = LinearEasing))
    var draggingProgress by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier.widthIn(max = 1000.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatSongDuration(currentMillis.milliseconds),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
        )
        Box(
            Modifier.weight(1f).height(6.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        offsetX = down.position.x
                        draggingProgress = (down.position.x / size.width).coerceIn(0f, 1f)
                        isDragging = true

                        while (true) {
                            val change = awaitDragOrCancellation(down.id)
                            if (change != null && change.pressed) {
                                val summed = offsetX + change.positionChange().x
                                change.consume()
                                offsetX = summed
                                draggingProgress = (summed / size.width).coerceIn(0f, 1f)
                            } else {
                                break
                            }
                        }
                        onProgressChange(draggingProgress)
                        scope.launch {
                            delay(200)
                            isDragging = false
                        }
                    }
                }
        ) {
            Box(
                Modifier.fillMaxWidth(bufferedProgress).height(6.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), CircleShape)
            )
            val progress = if (isDragging) draggingProgress else animatedPlayingProgress
            Box(Modifier.fillMaxWidth(progress).height(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
        }
        Text(
            text = formatSongDuration(durationMillis.milliseconds),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
        )
    }
}
