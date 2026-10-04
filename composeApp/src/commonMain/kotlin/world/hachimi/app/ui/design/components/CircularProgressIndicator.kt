package world.hachimi.app.ui.design.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator as MDCircularProgressIndicator

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    strokeWidth: Dp = 4.dp
) {
    MDCircularProgressIndicator(
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    strokeWidth: Dp = 4.dp,
    trackColor: Color = ProgressIndicatorDefaults.circularDeterminateTrackColor,
    gapSize: Dp = ProgressIndicatorDefaults.CircularIndicatorTrackGapSize,
    progress: () -> Float
) {
    MDCircularProgressIndicator(
        modifier = modifier,
        color = color,
        progress = progress,
        strokeWidth = strokeWidth,
        trackColor = trackColor,
        gapSize = gapSize,
    )
}