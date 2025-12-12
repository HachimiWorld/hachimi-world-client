package world.hachimi.app.ui.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import world.hachimi.app.ui.design.HachimiTheme

@Composable
fun ToggleButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(50),
    activeColor: Color = HachimiTheme.colorScheme.onSurface,
    activeContentColor: Color = HachimiTheme.colorScheme.onSurfaceReverse,
    inactiveColor: Color = Color.Transparent,
    inactiveContentColor: Color = LocalContentColor.current,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = if (selected) activeColor else inactiveColor,
        contentColor = if (selected) activeContentColor else inactiveContentColor
    ) {
        Box(Modifier.selectable(selected = selected, onClick = onClick, role = Role.Tab)) {
            content()
        }
    }
}