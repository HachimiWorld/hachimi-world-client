package world.hachimi.app.ui.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.HachimiTheme

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier.clip(RoundedCornerShape(12.dp))
            .background(HachimiTheme.colorScheme.background)
            .clickable(onClick = onClick),
        Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides HachimiTheme.colorScheme.onSurface) {
            content()
        }
    }
}

@Composable
fun AccentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier.clip(RoundedCornerShape(12.dp))
            .background(HachimiTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides HachimiTheme.colorScheme.onSurfaceReverse) {
            content()
        }
    }
}