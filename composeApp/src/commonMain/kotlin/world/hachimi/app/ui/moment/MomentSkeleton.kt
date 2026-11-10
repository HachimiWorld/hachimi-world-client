package world.hachimi.app.ui.moment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.components.placeholder
import world.hachimi.app.ui.theme.PreviewTheme

/** How many stand-ins fill the first screen while the moments load. @since 261110 */
const val MomentSkeletonCount = 5

/**
 * Stands in for a list-mode [MomentCard] while the moments load: the same avatar, header, lines and
 * footer room, so the page doesn't jump when they arrive. [index] varies the shape from row to row.
 *
 * @since 261110
 */
@Composable
fun MomentSkeleton(
    index: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = MomentCardPadding,
) {
    val shape = Rhythm[index % Rhythm.size]
    Row(modifier.fillMaxWidth().padding(contentPadding)) {
        Box(Modifier.size(40.dp).placeholder(true, CircleShape))
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            // Header: name and time, on the 20dp line of the real one
            Row(Modifier.height(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Bar(shape.name)
                Spacer(Modifier.width(8.dp))
                Bar(40.dp)
            }
            // Text on 22dp lines, the last one cut short
            repeat(shape.lines) { line ->
                val last = line == shape.lines - 1
                Box(Modifier.height(22.dp), contentAlignment = Alignment.CenterStart) {
                    Box(
                        Modifier.fillMaxWidth(if (last) shape.lastLine else 1f).height(12.dp)
                            .placeholder(true, BarShape)
                    )
                }
            }
            if (shape.media) {
                Box(
                    Modifier.padding(top = 10.dp).widthIn(max = 280.dp).fillMaxWidth().aspectRatio(16f / 9f)
                        .placeholder(true, RoundedCornerShape(12.dp))
                )
            }
            // Room for the footer buttons, left blank so the stand-in stays quiet
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Bar(width: Dp) {
    Box(Modifier.width(width).height(12.dp).placeholder(true, BarShape))
}

private val BarShape = RoundedCornerShape(4.dp)

private class SkeletonShape(val name: Dp, val lines: Int, val lastLine: Float, val media: Boolean = false)

private val Rhythm = listOf(
    SkeletonShape(name = 88.dp, lines = 3, lastLine = 0.6f),
    SkeletonShape(name = 64.dp, lines = 1, lastLine = 0.75f, media = true),
    SkeletonShape(name = 104.dp, lines = 2, lastLine = 0.45f),
    SkeletonShape(name = 72.dp, lines = 4, lastLine = 0.8f),
    SkeletonShape(name = 96.dp, lines = 1, lastLine = 0.55f),
)

@Preview
@Composable
private fun PreviewMomentSkeleton() {
    PreviewTheme(background = true) {
        Column { repeat(MomentSkeletonCount) { MomentSkeleton(it) } }
    }
}
