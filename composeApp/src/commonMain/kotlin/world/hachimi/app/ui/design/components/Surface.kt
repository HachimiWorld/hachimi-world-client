package world.hachimi.app.ui.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import world.hachimi.app.ui.design.HachimiTheme

@Composable
fun Surface(
    modifier: Modifier = Modifier,
    shape: Shape,
    color: Color = HachimiTheme.colorScheme.surface,
    contentColor: Color = HachimiTheme.colorScheme.onSurface,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.background(color, shape).clip(shape)
            .semantics { isTraversalGroup = true }
            .pointerInput(Unit) {},
        propagateMinConstraints = true
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}