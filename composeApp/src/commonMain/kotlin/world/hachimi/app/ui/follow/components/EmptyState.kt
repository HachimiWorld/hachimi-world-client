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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.follow_discover
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Text

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    showDiscoverButton: Boolean,
    onDiscoverClick: (() -> Unit)? = null,
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
                modifier = Modifier.size(80.dp)
                    .clip(CircleShape)
                    .background(HachimiTheme.colorScheme.primary.copy(0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showDiscoverButton) "♫" else "♥",
                    fontSize = 32.sp,
                    color = HachimiTheme.colorScheme.primary.copy(0.45f)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = HachimiTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            if (subtitle.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    color = HachimiTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            }
            if (showDiscoverButton && onDiscoverClick != null) {
                Spacer(Modifier.height(16.dp))
                AccentButton(onClick = onDiscoverClick) {
                    Text(stringResource(Res.string.follow_discover))
                }
            }
        }
    }
}
