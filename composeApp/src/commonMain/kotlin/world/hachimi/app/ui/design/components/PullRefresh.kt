package world.hachimi.app.ui.design.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.theme.PreviewTheme

/**
 * Pull to refresh, in the app's own colors: [content] with [PullRefreshIndicator] coming down
 * over it.
 *
 * @since 261111
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    content: @Composable BoxScope.() -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            PullRefreshIndicator(state, isRefreshing, Modifier.align(Alignment.TopCenter))
        },
        content = content,
    )
}

/**
 * A flat disc with a thin outline and an arc in the brand color. While pulled, the arc grows and
 * turns with the pull; once refreshing, it spins and breathes.
 *
 * @since 261111
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = HachimiTheme.colorScheme
    val arcColor = colors.primary
    val spin = rememberInfiniteTransition()
    val turn = spin.animateFloat(0f, 360f, infiniteRepeatable(tween(1000, easing = LinearEasing)))
    PullToRefreshDefaults.IndicatorBox(
        state = state,
        isRefreshing = isRefreshing,
        modifier = modifier,
        shape = CircleShape,
        containerColor = colors.surface.compositeOver(colors.background),
        elevation = 0.dp,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .border(1.dp, colors.outline, CircleShape)
                .drawBehind {
                    val stroke = 2.dp.toPx()
                    val diameter = 16.dp.toPx()
                    val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                    val pulled = state.distanceFraction.coerceIn(0f, 1f)
                    // The arc grows with the pull to three quarters of a circle, then keeps that length and
                    // turns at an even pace from where the pull left it
                    val (start, sweep) = if (isRefreshing) {
                        (PulledStart + turn.value) to ArcSweep
                    } else {
                        (pulled * (PulledStart + 90f) - 90f) to (pulled * ArcSweep)
                    }
                    if (sweep > 0f) rotate(start) {
                        drawArc(
                            color = arcColor,
                            startAngle = 0f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(stroke, cap = StrokeCap.Round),
                        )
                    }
                },
        )
    }
}

private const val ArcSweep = 270f
private const val PulledStart = 180f

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewPullRefreshIndicator() {
    PreviewTheme(background = true) {
        Box(Modifier.size(80.dp)) {
            PullRefreshIndicator(rememberPullToRefreshState(), isRefreshing = true, Modifier.align(Alignment.TopCenter))
        }
    }
}
