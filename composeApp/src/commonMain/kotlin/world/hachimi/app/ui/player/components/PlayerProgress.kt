package world.hachimi.app.ui.player.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.HachimiSlider
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.formatSongDuration
import kotlin.time.Duration.Companion.milliseconds

private val labelTypography = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
)

@Composable
fun PlayerProgress(
    durationMillis: Long,
    currentMillis: Long,
    bufferingProgress: Float = 0f,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    trackColor: Color = HachimiTheme.colorScheme.outline,
    barColor: Color = HachimiTheme.colorScheme.primary,
) {
    val playingProgress = (currentMillis.toDouble() / durationMillis).toFloat().coerceIn(0f, 1f)

    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatSongDuration(currentMillis.milliseconds),
                style = labelTypography,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = formatSongDuration(durationMillis.milliseconds),
                style = labelTypography
            )
        }

        Spacer(Modifier.height(2.dp))

        HachimiSlider(
            modifier = Modifier.fillMaxWidth().height(6.dp),
            progress = { playingProgress },
            onProgressChange = onProgressChange,
            trackProgress = { bufferingProgress },
            trackColor = trackColor,
            barColor = barColor
        )
    }
}