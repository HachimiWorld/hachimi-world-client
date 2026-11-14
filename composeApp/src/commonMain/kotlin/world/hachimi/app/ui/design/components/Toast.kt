package world.hachimi.app.ui.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.theme.PreviewTheme

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
    Toast(
        message = data.visuals.message,
        actionLabel = data.visuals.actionLabel,
        onAction = data::performAction,
        onDismiss = data::dismiss,
        modifier = modifier,
    )
}

/**
 * The [Toast] as it looks, apart from the host that queues it.
 * @since 261114
 */
@Composable
fun Toast(
    message: String,
    actionLabel: String?,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HachimiTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .widthIn(max = 480.dp)
            .shadow(12.dp, shape, ambientColor = ToastShadow, spotColor = ToastShadow)
            .clip(shape)
            .background(colors.inverseSurface)
            .clickable(onClick = onDismiss)
            .defaultMinSize(minHeight = 40.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            message,
            modifier = Modifier.weight(1f, fill = false),
            color = colors.onSurfaceReverse,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
        actionLabel?.let { label ->
            Text(
                label,
                modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable(onClick = onAction),
                color = colors.primary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private val ToastShadow = Color.Black.copy(alpha = 0.16f)

@Composable
private fun ToastSamples() {
    Column(Modifier.padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Toast("已保存", actionLabel = null, onAction = {}, onDismiss = {})
        Toast("网络连接失败，请稍后再试", actionLabel = null, onAction = {}, onDismiss = {})
        Toast(
            "这是一条比较长的提示，换行之后成为圆角的块，最宽不超过 480dp",
            actionLabel = "撤销",
            onAction = {},
            onDismiss = {},
        )
    }
}

@Preview(widthDp = 360, name = "Light")
@Composable
private fun ToastLightPreview() {
    PreviewTheme(darkTheme = false, background = true) { ToastSamples() }
}

@Preview(widthDp = 360, name = "Dark")
@Composable
private fun ToastDarkPreview() {
    PreviewTheme(darkTheme = true, background = true) { ToastSamples() }
}
