package world.hachimi.app.ui.committee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import world.hachimi.app.api.module.ReportModule
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.message.components.RemoteImage
import world.hachimi.app.ui.report.reasonLabel

/** A small rounded label. [accent] tints it with the primary color for what needs attention. */
@Composable
fun Pill(text: String, modifier: Modifier = Modifier, accent: Boolean = false) {
    val colors = HachimiTheme.colorScheme
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (accent) colors.primary.copy(alpha = 0.14f) else colors.onSurface.copy(alpha = 0.07f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (accent) FontWeight.SemiBold else FontWeight.Normal,
        color = if (accent) colors.primary else colors.onSurfaceVariant,
        maxLines = 1,
    )
}

/** Pending reports per reason, most first. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReasonPills(reasons: List<ReportModule.ReasonCount>, modifier: Modifier = Modifier) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        reasons.forEach { Pill("${reasonLabel(it.reason)} ${it.count}") }
    }
}

/** The target's cover, round for users, with a placeholder icon when there is none. */
@Composable
fun TargetCover(targetType: String, url: String?, size: Dp = 48.dp) {
    val isUser = targetType == ReportModule.TARGET_USER
    val shape = if (isUser) CircleShape else RoundedCornerShape(size / 6)
    Box(
        Modifier.size(size).clip(shape).background(HachimiTheme.colorScheme.onSurface.copy(alpha = 0.07f)),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isNullOrEmpty()) {
            Icon(
                imageVector = when (targetType) {
                    ReportModule.TARGET_USER -> Icons.Default.Person
                    ReportModule.TARGET_PLAYLIST -> Icons.AutoMirrored.Filled.QueueMusic
                    else -> Icons.Default.MusicNote
                },
                contentDescription = null,
                modifier = Modifier.size(size * 0.45f),
                tint = HachimiTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        } else {
            RemoteImage(url, Modifier.size(size))
        }
    }
}

@Composable
fun Avatar(url: String?, size: Dp) {
    TargetCover(ReportModule.TARGET_USER, url, size)
}

/** Section title above a card. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.padding(start = 4.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        if (trailing != null) {
            Text(
                trailing,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                color = HachimiTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
