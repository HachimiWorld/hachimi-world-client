package world.hachimi.app.ui.design.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import world.hachimi.app.ui.design.HachimiTheme

@Composable
fun TagBadge(
    hazeState: HazeState, tag: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clip(CircleShape).hazeEffect(
            hazeState, style = HazeStyle(
                backgroundColor = HachimiTheme.colorScheme.surface,
                blurRadius = 12.dp,
                tint = HazeTint(color = HachimiTheme.colorScheme.surface)
            )
        ),
        color = Color.Transparent,
        shape = CircleShape,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            text = tag,
            style = TextStyle(fontSize = 12.sp),
            color = HachimiTheme.colorScheme.onSurface,
        )
    }
}