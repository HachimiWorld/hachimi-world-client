package world.hachimi.app.ui.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
fun Surface(
    modifier: Modifier = Modifier,
    shape: Shape,
    color: Color,
    contentColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.background(color, shape).clip(shape),
        propagateMinConstraints = true
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}