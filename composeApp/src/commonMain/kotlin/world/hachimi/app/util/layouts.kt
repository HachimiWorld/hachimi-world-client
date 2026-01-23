package world.hachimi.app.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.LocalWindowSize

fun Modifier.fillMaxWithLimit(maxWidth: Dp = 1000.dp): Modifier =
    this.fillMaxSize().wrapContentWidth().widthIn(max = maxWidth)

fun Modifier.fillMaxWidthIn(maxWidth: Dp = WindowSize.EXPANDED, contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally): Modifier =
    this.fillMaxWidth().wrapContentWidth(align = contentAlignment).widthIn(max = maxWidth)

object WindowSize {
    val COMPACT = 600.dp
    val MEDIUM = 840.dp
    val EXPANDED = 1200.dp
    val LARGE = 1600.dp
    val EXTRA_LARGE = 1920.dp
}

class VerticalWithLastArrangement(
    val lastItemArrangement: Arrangement.Vertical = Arrangement.Center
) : Arrangement.Vertical {
    override fun Density.arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) {
        var current = 0
        sizes.forEachIndexed { index, it ->
            if (index == sizes.lastIndex && current + it < totalSize) {
                // Arrange the last item with `lastItemArrangement`
                val lastItemSize = intArrayOf(it)
                val lastItemPosition = IntArray(1)
                with(lastItemArrangement) {
                    arrange(totalSize - current, lastItemSize, lastItemPosition)
                }
                outPositions[index] = current + lastItemPosition[0]
            } else {
                outPositions[index] = current
                current += it
            }
        }
    }
}

class StartWithLastArrangement(
    val lastItemArrangement: Arrangement.Horizontal = Arrangement.Center
) : Arrangement.Horizontal {
    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        layoutDirection: LayoutDirection,
        outPositions: IntArray
    ) {
        if (layoutDirection == LayoutDirection.Ltr) {
            var current = 0
            sizes.forEachIndexed { index, it ->
                if (index == sizes.lastIndex && current + it < totalSize) {
                    // Arrange the last item with `lastItemArrangement`
                    val lastItemSize = intArrayOf(it)
                    val lastItemPosition = IntArray(1)
                    with(lastItemArrangement) {
                        arrange(totalSize - current, lastItemSize, layoutDirection, lastItemPosition)
                    }
                    outPositions[index] = current + lastItemPosition[0]
                } else {
                    outPositions[index] = current
                    current += it
                }
            }
        } else {
            val consumedSize = sizes.fold(0) { a, b -> a + b }
            var current = totalSize - consumedSize
            for (index in (sizes.size - 1) downTo 0) {
                val it = sizes[index]
                if (index == 0 && current + it < totalSize) {
                    // Arrange the last item with `lastItemArrangement`
                    val lastItemSize = intArrayOf(it)
                    val lastItemPosition = IntArray(1)
                    with(lastItemArrangement) {
                        arrange(totalSize - current, lastItemSize, layoutDirection, lastItemPosition)
                    }
                    outPositions[index] = current + lastItemPosition[0]
                } else {
                    outPositions[index] = current
                    current += it
                }
            }
        }
    }
}

val AdaptiveListSpacing: Dp
    @Composable @Stable get() = if (LocalWindowSize.current.width < WindowSize.COMPACT) 16.dp else 24.dp

/**
 * Decide the number of columns based on the screen width. Especially for grid layout.
 */
fun calculateGridColumns(maxWidth: Dp): GridCells = when {
    // 2 ~ 6 columns
    maxWidth < 420.dp -> GridCells.Adaptive(minSize = 120.dp)
    maxWidth < WindowSize.COMPACT -> GridCells.Adaptive(minSize = 160.dp)
    maxWidth < WindowSize.MEDIUM -> GridCells.Fixed(3)
    maxWidth < WindowSize.EXPANDED -> GridCells.Fixed(4)
    maxWidth < WindowSize.LARGE -> GridCells.Fixed(5)
    maxWidth < WindowSize.EXTRA_LARGE -> GridCells.Fixed(6)
    else -> GridCells.Fixed(6)
}