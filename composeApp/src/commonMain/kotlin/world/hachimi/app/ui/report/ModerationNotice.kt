package world.hachimi.app.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.moderation_reason
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text

/** Tells the owner their song or playlist is hidden, and why. */
@Composable
fun ModerationNotice(
    title: String,
    body: String,
    reason: String?,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = HachimiTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.primary.copy(alpha = 0.10f))
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(Icons.Default.VisibilityOff, contentDescription = null, modifier = Modifier.size(20.dp), tint = colors.primary)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(body, modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            if (!reason.isNullOrBlank()) {
                Text(
                    stringResource(Res.string.moderation_reason, reason),
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (action != null) {
            Row(Modifier.padding(start = 12.dp).align(Alignment.CenterVertically)) { action() }
        }
    }
}
