package world.hachimi.app.ui.design.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.HachimiTheme

@Composable
fun HachimiIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.size(28.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(20.dp)) {
            content()
        }
    }
}

@Composable
fun HachimiIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.size(28.dp)
            .clip(CircleShape)
            .toggleable(checked, onValueChange = onCheckedChange),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides if (checked) HachimiTheme.colorScheme.primary else LocalContentColor.current) {
            Box(Modifier.size(20.dp)) {
                content()
            }
        }
    }
}