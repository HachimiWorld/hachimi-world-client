package world.hachimi.app.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.LocalWindowSize

fun Modifier.fillMaxWidthIn(
    maxWidth: Dp = 1280.dp,
    contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
): Modifier =
    this.fillMaxWidth().wrapContentWidth(align = contentAlignment)
        .widthIn(max = maxWidth).fillMaxWidth()

object WindowSize {
    val COMPACT = 600.dp
    val MEDIUM = 840.dp
    val EXPANDED = 1200.dp
    val LARGE = 1600.dp
    val EXTRA_LARGE = 1920.dp
}

object TopWithFooter : Arrangement.Vertical {
    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        outPositions: IntArray
    ) {
        var y = 0
        sizes.forEachIndexed { index, size ->
            outPositions[index] = y
            y += size
        }
        if (y < totalSize) {
            val lastIndex = outPositions.lastIndex
            outPositions[lastIndex] = totalSize - sizes.last()
        }
    }
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
                        arrange(
                            totalSize - current,
                            lastItemSize,
                            layoutDirection,
                            lastItemPosition
                        )
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
                        arrange(
                            totalSize - current,
                            lastItemSize,
                            layoutDirection,
                            lastItemPosition
                        )
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
val AdaptiveScreenMargin: Dp
    @Composable @Stable get() = if (LocalWindowSize.current.width < WindowSize.COMPACT) 16.dp else 24.dp

/**
 * Decide the number of columns based on the screen width. Especially for grid layout.
 */
@Stable
fun calculateGridColumns(maxWidth: Dp): GridCells = when {
    // 2 ~ 6 columns
    maxWidth < 420.dp -> GridCells.Adaptive(minSize = 120.dp)
    else -> GridCells.Adaptive(minSize = 160.dp)
}

/**
 * Use `contentPadding` to limit the max width.
 * This is usually used in LazyColumn, because we need the scrolling detect area to be the full width.
 * If we use `Modifier.widthIn(max = maxWidth)`, the scrolling detect area will be limited to the max width, which is not what we want.
 */
@Composable
fun contentPaddingForMaxWidth(
    padding: PaddingValues,
    currentWidth: Dp,
    maxWidth: Dp = 1280.dp
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return if (currentWidth > maxWidth) {
        PaddingValues(
            start = padding.calculateStartPadding(layoutDirection) + (currentWidth - maxWidth) / 2,
            end = padding.calculateEndPadding(layoutDirection) + (currentWidth - maxWidth) / 2,
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding()
        )
    } else {
        padding
    }
}