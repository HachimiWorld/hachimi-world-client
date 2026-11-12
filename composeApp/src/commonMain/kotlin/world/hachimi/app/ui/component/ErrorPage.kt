package world.hachimi.app.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.reload_page_message
import hachimiworld.composeapp.generated.resources.reload_retry
import hachimiworld.composeapp.generated.resources.reload_tap_to_retry
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme

/**
 * What a page or a section shows when its first load fails. The whole area is the retry: there is
 * no button to aim for, a tap anywhere in it tries again. The press only dims the words, since a
 * ripple across a whole screen is louder than the failure it answers.
 *
 * The caller decides the area through [modifier]; a section usually clips and tints it so the
 * edge of what can be tapped is visible.
 *
 * @since 261112
 */
@Composable
fun ErrorPage(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize(),
    message: String? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val hovered by interaction.collectIsHoveredAsState()
    val alpha by animateFloatAsState(
        targetValue = when {
            pressed -> 0.4f
            hovered -> 0.7f
            else -> 1f
        },
        animationSpec = tween(120),
    )
    val colors = HachimiTheme.colorScheme
    Box(
        modifier.clickable(
            interactionSource = interaction,
            indication = null,
            role = Role.Button,
            onClickLabel = stringResource(Res.string.reload_retry),
            onClick = onRetry,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(24.dp).graphicsLayer { this.alpha = alpha },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                message ?: stringResource(Res.string.reload_page_message),
                textAlign = TextAlign.Center,
                color = colors.onSurface,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Text(stringResource(Res.string.reload_tap_to_retry), fontSize = 13.sp, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        ErrorPage(onRetry = {})
    }
}
