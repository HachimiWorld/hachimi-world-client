package world.hachimi.app.ui.design.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import world.hachimi.app.ui.design.HachimiTheme

@Composable
fun HachimiSlider(
    modifier: Modifier,
    progress: () -> Float,
    onProgressChange: (Float) -> Unit,
    applyMode: SliderChangeApplyMode = SliderChangeApplyMode.End,
    trackColor: Color = HachimiTheme.colorScheme.outline,
    barColor: Color = HachimiTheme.colorScheme.primary,
) {
    val animatedPlayingProgress by animateFloatAsState(
        targetValue = progress().coerceIn(0f..1f),
        tween(durationMillis = 100, easing = LinearEasing)
    )
    var draggingProgress by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }

    val progress = if (isDragging) draggingProgress else animatedPlayingProgress
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    Canvas(
        modifier = modifier
            .defaultMinSize(minHeight = 6.dp, minWidth = 100.dp)
            .hoverable(interactionSource)
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
                            if (applyMode == SliderChangeApplyMode.Immediate) {
                                onProgressChange(draggingProgress)
                            }
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
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(0f, 2.dp.toPx()),
            size = Size(size.width, 2.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )

        val x = size.width * progress

        drawRoundRect(
            color = barColor,
            topLeft = Offset(0f, 2.dp.toPx()),
            size = Size(width = x, height = 2.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )

        if (hovered || isDragging) {
            drawLine(
                color = barColor,
                start = Offset(x, 0f),
                end = Offset(x, 6.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

enum class SliderChangeApplyMode {
    Immediate, End
}