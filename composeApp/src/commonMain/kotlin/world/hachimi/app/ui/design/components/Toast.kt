package world.hachimi.app.ui.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.HachimiTheme

/**
 * Shows the messages of [hostState] one at a time as a [Toast].
 * @since 261114
 */
@Composable
fun ToastHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    androidx.compose.material3.SnackbarHost(hostState, modifier) { Toast(it) }
}

/**
 * A short message floating over the page, in the opposite tone to it. Tapping it puts it away.
 *
 * One line of text makes a pill; longer text wraps into a rounded block no wider than 480dp.
 * @since 261114
 */
@Composable
fun Toast(data: SnackbarData, modifier: Modifier = Modifier) {
    val colors = HachimiTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .widthIn(max = 480.dp)
            .shadow(12.dp, shape, ambientColor = ToastShadow, spotColor = ToastShadow)
            .clip(shape)
            .background(colors.inverseSurface)
            .clickable { data.dismiss() }
            .defaultMinSize(minHeight = 40.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            data.visuals.message,
            modifier = Modifier.weight(1f, fill = false),
            color = colors.onSurfaceReverse,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
        data.visuals.actionLabel?.let { label ->
            Text(
                label,
                modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { data.performAction() },
                color = colors.primary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private val ToastShadow = Color.Black.copy(alpha = 0.16f)
