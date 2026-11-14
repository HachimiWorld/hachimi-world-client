package world.hachimi.app.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.play_all
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Icon

/**
 * The page's play-all, made of the covers of its first songs: up to three, fanned out like records
 * left on a table, newest on top with the play mark. Pressing it spreads the fan a little. In the
 * bar ([folded]), or with no covers, it's the plain [PlayAllButton].
 *
 * @param covers cover URLs, newest first
 * @since 261114
 */
@Composable
fun PlayAllCovers(
    covers: List<String>,
    folded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (folded || covers.isEmpty()) {
        PlayAllButton(onClick = onClick, modifier = modifier)
        return
    }
    // Oldest first, so the newest is drawn last, on top and to the right
    val shown = covers.take(3).reversed()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val spread by animateFloatAsState(if (pressed) 1f else 0f, spring(dampingRatio = 0.6f, stiffness = 500f))
    val label = stringResource(Res.string.play_all)
    val colors = HachimiTheme.colorScheme

    Box(
        modifier
            .size(width = CoverSize + CoverStep * (shown.size - 1), height = CoverSize)
            .semantics(mergeDescendants = true) { contentDescription = label }
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
    ) {
        shown.forEachIndexed { index, url ->
            // Counted from the top cover: 0 is the newest
            val depth = shown.lastIndex - index
            val tilt = Tilts[depth] * (1f + spread * 0.6f)
            val shift = CoverStep * index + SpreadShift * spread * (index - shown.lastIndex)
            Box(
                Modifier
                    .graphicsLayer {
                        translationX = shift.toPx()
                        rotationZ = tilt
                    }
                    .size(CoverSize)
                    // A thin edge in the page color keeps the covers apart where they overlap
                    .border(1.dp, colors.background, CoverShape)
                    .clip(CoverShape)
                    .background(colors.onSurface.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    modifier = Modifier.size(CoverSize),
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .httpHeaders(CoilHeaders)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                if (depth == 0) {
                    Box(
                        Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colors.onSurfaceReverse,
                        )
                    }
                }
            }
        }
    }
}

private val CoverSize = 46.dp
private val CoverStep = 17.dp
private val SpreadShift = 5.dp
private val CoverShape = RoundedCornerShape(9.dp)

/** Degrees for the top cover, the one under it, and the one at the back. */
private val Tilts = floatArrayOf(6f, -3f, -11f)
