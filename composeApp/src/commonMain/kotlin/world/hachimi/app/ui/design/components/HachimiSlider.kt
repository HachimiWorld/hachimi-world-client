package world.hachimi.app.ui.design.components

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import world.hachimi.app.ui.design.HachimiTheme

@Composable
fun HachimiSlider(
    modifier: Modifier,
    progress: () -> Float,
    onProgressChange: (Float) -> Unit,
    trackProgress: () -> Float = { 1f },
    applyMode: SliderChangeApplyMode = SliderChangeApplyMode.End,
    trackColor: Color = HachimiTheme.colorScheme.outline,
    barColor: Color = HachimiTheme.colorScheme.primary,
    thickness: Dp = 2.dp
) {
    val coercedProgress = { progress().fastCoerceIn(0f, 1f) }
    var draggingProgress by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val progress = { if (isDragging) draggingProgress else coercedProgress() }

    Canvas(
        modifier = modifier
            .defaultMinSize(minHeight = 6.dp, minWidth = 100.dp)
            .hoverable(interactionSource)
            .pointerInput(Unit) {
                coroutineScope {
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
            }
    ) {
        val thicknessPx = thickness.toPx()

        val y = size.height / 2 - thicknessPx / 2

        drawRoundRect(
            color = trackColor,
            topLeft = Offset(0f, y),
            size = Size(size.width * trackProgress(), thicknessPx),
            cornerRadius = CornerRadius(thicknessPx)
        )

        val x = size.width * progress()

        drawRoundRect(
            color = barColor,
            topLeft = Offset(0f, y),
            size = Size(width = x, height = thicknessPx),
            cornerRadius = CornerRadius(thicknessPx)
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