package world.hachimi.app.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.fillMaxWithLimit(maxWidth: Dp = 1000.dp): Modifier =
    this.fillMaxSize().wrapContentWidth().widthIn(max = maxWidth)

object WindowSize {
    val COMPACT = 600.dp
    val MEDIUM = 840.dp
    val EXPANDED = 1200.dp
    val LARGE = 1600.dp
    val EXTRA_LARGE = 1920.dp
}

class VerticalWithLastArrangement(
    val lastItemArrangement: Arrangement.Vertical = Arrangement.Center
): Arrangement.Vertical {
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