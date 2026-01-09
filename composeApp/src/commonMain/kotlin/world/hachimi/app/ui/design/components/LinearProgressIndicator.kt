package world.hachimi.app.ui.design.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material3.LinearProgressIndicator as MDLinearProgressIndicator

@Composable
fun LinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    strokeCap: StrokeCap = StrokeCap.Round
) {
    // TODO(design): Use our own design
    MDLinearProgressIndicator(
        modifier = modifier,
        color = color,
        trackColor = Color.Transparent,
        strokeCap = strokeCap
    )
}