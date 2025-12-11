package world.hachimi.app.ui.util

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

fun Modifier.fadingEdges(
    topEdgeHeight: Dp = 72.dp,
    bottomEdgeHeight: Dp = 72.dp
): Modifier = this.graphicsLayer {
    compositingStrategy = CompositingStrategy.Offscreen
}
    .drawWithCache {
        val topColors = listOf(Color.Transparent, Color.Black)
        val topBrush = Brush.verticalGradient(
            colors = topColors,
            startY = 0f,
            endY = topEdgeHeight.toPx()
        )

        val bottomColors = listOf(Color.Black, Color.Transparent)
        val bottomBrush = Brush.verticalGradient(
            colors = bottomColors,
            startY = size.height - bottomEdgeHeight.toPx(),
            endY = size.height
        )

        onDrawWithContent {
            drawContent()
            drawRect(
                brush = topBrush,
                blendMode = BlendMode.DstIn
            )
            drawRect(
                brush = bottomBrush,
                blendMode = BlendMode.DstIn
            )
        }
    }

fun Modifier.horizontalFadingEdges(
    startEdgeWidth: Dp = 72.dp,
    endEdgeWidth: Dp = 72.dp
): Modifier = this.graphicsLayer {
    compositingStrategy = CompositingStrategy.Offscreen
}
    .drawWithCache {
        val startColors = listOf(Color.Transparent, Color.Black)
        val startBrush = Brush.horizontalGradient(
            colors = startColors,
            startX = 0f,
            endX = startEdgeWidth.toPx()
        )

        val endColors = listOf(Color.Black, Color.Transparent)
        val endBrush = Brush.horizontalGradient(
            colors = endColors,
            startX = size.height - endEdgeWidth.toPx(),
            endX = size.height
        )

        onDrawWithContent {
            drawContent()
            drawRect(
                brush = startBrush,
                blendMode = BlendMode.DstIn
            )
            drawRect(
                brush = endBrush,
                blendMode = BlendMode.DstIn
            )
        }
    }

fun Modifier.fadingEdges(
    scrollState: ScrollState,
    topEdgeHeight: Dp = 72.dp,
    bottomEdgeHeight: Dp = 72.dp
): Modifier = this.then(
    Modifier
        // adding layer fixes issue with blending gradient and content
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
//            alpha = 0.99F
        }
        .drawWithContent {
            drawContent()

            val topColors = listOf(Color.Transparent, Color.Black)
            val topStartY = scrollState.value.toFloat()
            val topGradientHeight = min(topEdgeHeight.toPx(), topStartY)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = topColors,
                    startY = topStartY,
                    endY = topStartY + topGradientHeight
                ),
                blendMode = BlendMode.DstIn
            )

            val bottomColors = listOf(Color.Black, Color.Transparent)
            val bottomEndY = size.height - scrollState.maxValue + scrollState.value
            val bottomGradientHeight = min(bottomEdgeHeight.toPx(), scrollState.maxValue.toFloat() - scrollState.value)
            if (bottomGradientHeight != 0f) drawRect(
                brush = Brush.verticalGradient(
                    colors = bottomColors,
                    startY = bottomEndY - bottomGradientHeight,
                    endY = bottomEndY
                ),
                blendMode = BlendMode.DstIn
            )
        }
)