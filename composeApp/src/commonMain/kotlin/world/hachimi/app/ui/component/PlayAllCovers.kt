package world.hachimi.app.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
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
import world.hachimi.app.ui.theme.PreviewTheme

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
    CoverFan(count = covers.size.coerceAtMost(MaxCovers), onClick = onClick, modifier = modifier) { index ->
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .httpHeaders(CoilHeaders)
                .data(covers[index])
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

/**
 * The fan of [PlayAllCovers] around whatever [cover] draws: index 0 is the newest, on top. Pressing
 * it spreads the fan.
 *
 * @since 261114
 */
@Composable
fun CoverFan(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cover: @Composable (index: Int) -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val spread by animateFloatAsState(if (pressed) 1f else 0f, spring(dampingRatio = 0.6f, stiffness = 500f))
    val label = stringResource(Res.string.play_all)
    CoverFanLayout(
        count = count,
        spread = spread,
        modifier = modifier
            .semantics(mergeDescendants = true) { contentDescription = label }
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
        cover = cover,
    )
}

/** The fan at a given [spread], from 0 (at rest) to 1 (pressed). */
@Composable
private fun CoverFanLayout(
    count: Int,
    spread: Float,
    modifier: Modifier = Modifier,
    cover: @Composable (index: Int) -> Unit,
) {
    val shown = count.coerceIn(1, MaxCovers)
    val colors = HachimiTheme.colorScheme
    Box(modifier.size(width = CoverSize + CoverStep * (shown - 1), height = CoverSize)) {
        // The oldest is drawn first, so the newest lies on top and furthest right
        for (slot in 0 until shown) {
            val depth = shown - 1 - slot
            val tilt = Tilts[depth] * (1f + spread * 0.6f)
            val shift = CoverStep * slot + SpreadShift * spread * (slot - (shown - 1))
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
                cover(depth)
                if (depth == 0) PlayMark()
            }
        }
    }
}

@Composable
private fun PlayMark() {
    val colors = HachimiTheme.colorScheme
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

private const val MaxCovers = 3
private val CoverSize = 46.dp
private val CoverStep = 17.dp
private val SpreadShift = 5.dp
private val CoverShape = RoundedCornerShape(9.dp)

/** Degrees for the top cover, the one under it, and the one at the back. */
private val Tilts = floatArrayOf(6f, -3f, -11f)

/** Stand-ins for covers: newest, then older. */
private val PreviewCovers = listOf(Color(0xFFF2C14E), Color(0xFFE88FB0), Color(0xFF5DB3A0))

@Composable
private fun PreviewFans() {
    Row(
        Modifier.padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (count in 1..3) {
            CoverFanLayout(count = count, spread = 0f) { Box(Modifier.fillMaxSize().background(PreviewCovers[it])) }
        }
        // Pressed
        CoverFanLayout(count = 3, spread = 1f) { Box(Modifier.fillMaxSize().background(PreviewCovers[it])) }
        // Folded into the bar
        PlayAllCovers(covers = emptyList(), folded = true, onClick = {})
    }
}

@Preview(name = "Fans · light")
@Composable
private fun CoverFanLightPreview() {
    PreviewTheme(darkTheme = false, background = true) { PreviewFans() }
}

@Preview(name = "Fans · dark")
@Composable
private fun CoverFanDarkPreview() {
    PreviewTheme(darkTheme = true, background = true) { PreviewFans() }
}
