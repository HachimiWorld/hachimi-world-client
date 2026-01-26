package world.hachimi.app.ui.design.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.CircularProgressIndicator as MDCircularProgressIndicator

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    MDCircularProgressIndicator(
        modifier = modifier,
        color = color,
    )
}

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    progress: () -> Float
) {
    MDCircularProgressIndicator(
        modifier = modifier,
        color = color,
        progress = progress
    )
}
