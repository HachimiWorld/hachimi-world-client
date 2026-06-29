package world.hachimi.app.ui.follow.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.follow_retry
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.SubtleButton
import world.hachimi.app.ui.design.components.Text

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp)
                    .clip(CircleShape)
                    .background(HachimiTheme.colorScheme.onSurface.copy(0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    fontSize = 32.sp,
                    color = HachimiTheme.colorScheme.onSurfaceVariant.copy(0.45f)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                color = HachimiTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            SubtleButton(onClick = onRetry) {
                Text(stringResource(Res.string.follow_retry))
            }
        }
    }
}
