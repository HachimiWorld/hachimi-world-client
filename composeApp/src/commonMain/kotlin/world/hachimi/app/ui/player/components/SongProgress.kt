package world.hachimi.app.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import world.hachimi.app.util.formatSongDuration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SongProgress(
    durationMillis: Long,
    currentMillis: Long,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDragging by remember { mutableStateOf(false) }
    val playingProgress by derivedStateOf {
        (currentMillis.toDouble() / durationMillis).toFloat().coerceIn(0f, 1f)
    }
    var draggingProgress by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }


    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatSongDuration(currentMillis.milliseconds),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
        )
        Box(
            Modifier.weight(1f).height(6.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
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
                        isDragging = false
                        onProgressChange(draggingProgress)
                    }
                }
        ) {
            val progress = if (isDragging) draggingProgress else playingProgress
            Box(Modifier.fillMaxWidth(progress).height(6.dp).background(MaterialTheme.colorScheme.primary))
        }
        Text(
            text = formatSongDuration(durationMillis.milliseconds),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
        )
    }
}
