package world.hachimi.app.ui.root.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.nav_home_title
import hachimiworld.composeapp.generated.resources.nav_me
import hachimiworld.composeapp.generated.resources.nav_square
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.nav.BottomTab
import world.hachimi.app.ui.TestTags
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.insets.multiplatformNavigationBars
import world.hachimi.app.ui.theme.PreviewTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** The bar's height, without the system navigation bar under it. @since 261110 */
val CompactBottomBarHeight = 60.dp

/**
 * The phone's bottom bar: home, the square, and me.
 *
 * The open tab wears the halo from the app's icon: a small tilted ring floating over its icon, its
 * far half drawn under the icon and its near half over it, so the two can cross. Choosing another tab sends the halo over in
 * a small hop; choosing the open one again turns it once on the spot. Nothing else on the bar is in
 * the brand color.
 *
 * @param avatarUrl Stands in for the person icon once someone is signed in.
 * @param meUnread Puts a dot on the Me tab while messages are unread.
 * @param onReselect Called when the open tab is chosen again. @since 261111
 * @since 261110
 */
@Composable
fun CompactBottomBar(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    meUnread: Boolean = false,
    onReselect: (BottomTab) -> Unit = {},
) {
    val colors = HachimiTheme.colorScheme
    val halo = rememberHaloMotion(selected)
    val scope = rememberCoroutineScope()
    val haloColor = colors.primary
    Box(
        modifier
            .fillMaxWidth()
            .drawBehind { drawRect(colors.background) }
            .windowInsetsPadding(WindowInsets.multiplatformNavigationBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(CompactBottomBarHeight)
                .selectableGroup()
                .drawWithContent {
                    // The far half under the icons, the near half over them
                    drawHalo(halo, haloColor, front = false)
                    drawContent()
                    drawHalo(halo, haloColor, front = true)
                },
        ) {
            BottomTab.entries.forEach { tab ->
                val isSelected = tab == selected
                // The tab lights up as the halo gets to it rather than when it's tapped; a tab the
                // halo only flies over stays dark
                val lit by remember(tab, selected) {
                    derivedStateOf {
                        val under = abs(halo.position.value - tab.ordinal) < 0.5f
                        under && (isSelected || tab.ordinal.toFloat() == halo.from)
                    }
                }
                Tab(
                    label = stringResource(
                        when (tab) {
                            BottomTab.HOME -> Res.string.nav_home_title
                            BottomTab.SQUARE -> Res.string.nav_square
                            BottomTab.ME -> Res.string.nav_me
                        }
                    ),
                    selected = isSelected,
                    lit = lit,
                    onClick = {
                        if (isSelected) {
                            scope.launch { halo.spin() }
                            onReselect(tab)
                        } else {
                            onSelect(tab)
                        }
                    },
                    modifier = Modifier.weight(1f).testTag(
                        when (tab) {
                            BottomTab.HOME -> TestTags.NAV_HOME
                            BottomTab.SQUARE -> TestTags.NAV_EVENTS
                            BottomTab.ME -> TestTags.NAV_ME
                        }
                    ),
                ) {
                    when (tab) {
                        BottomTab.HOME -> TabIcon(Icons.Filled.Home, Icons.Outlined.Home, lit)
                        BottomTab.SQUARE -> TabIcon(Icons.Filled.Forum, Icons.Outlined.Forum, lit)
                        BottomTab.ME -> Box {
                            if (avatarUrl != null) {
                                TabAvatar(avatarUrl)
                            } else {
                                TabIcon(Icons.Filled.Person, Icons.Outlined.Person, lit)
                            }
                            if (meUnread) {
                                Box(
                                    Modifier.align(Alignment.TopEnd).graphicsLayer {
                                        // Low and to the side, clear of the halo's end; no cut-out ring, so
                                        // it sits as well on the press pill as on the bar
                                        translationX = 3.dp.toPx()
                                        translationY = 2.dp.toPx()
                                    }.size(7.dp).background(colors.primary, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Where the halo is: [position] in tabs (0 is the first), and how far through a turn it is.
 * A hop's height comes from how far it has gone between the tab it left and the one it's bound for.
 */
private class HaloMotion {
    val position = Animatable(0f)
    val turn = Animatable(0f)
    var from = 0f
    var to = 0f

    /** 0 at either end of a hop, 1 halfway. */
    fun hop(): Float {
        val span = to - from
        if (abs(span) < 0.001f) return 0f
        val done = ((position.value - from) / span).coerceIn(0f, 1f)
        return sin(PI.toFloat() * done)
    }

    /** Which way it's going: -1, 0 or 1. */
    fun heading(): Float = if (to > from) 1f else if (to < from) -1f else 0f

    suspend fun spin() {
        turn.snapTo(0f)
        turn.animateTo(1f, tween(560, easing = FastOutSlowInEasing))
        turn.snapTo(0f)
    }
}

@Composable
private fun rememberHaloMotion(selected: BottomTab): HaloMotion {
    val motion = remember { HaloMotion().also { it.from = selected.ordinal.toFloat(); it.to = it.from } }
    LaunchedEffect(Unit) { motion.position.snapTo(selected.ordinal.toFloat()) }
    LaunchedEffect(selected) {
        val target = selected.ordinal.toFloat()
        if (motion.position.value == target) return@LaunchedEffect
        motion.from = motion.position.value
        motion.to = target
        motion.position.animateTo(target, spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow))
        motion.from = target
    }
    return motion
}

/**
 * Draws one half of the halo over the open tab's icon. Its resting place is fixed by the tab's
 * layout: the icon is the top of a centred 24dp icon, 3dp gap, 14sp label column.
 */
private fun DrawScope.drawHalo(motion: HaloMotion, color: Color, front: Boolean) {
    val tabWidth = size.width / BottomTab.entries.size
    val iconTop = (size.height - (IconSize + LabelGap + LabelLine).toPx()) / 2
    val hop = motion.hop()
    val center = Offset(
        x = (motion.position.value + 0.5f) * tabWidth,
        y = iconTop - HaloRise.toPx() - HopHeight.toPx() * hop,
    )
    val ring = Size(HaloWidth.toPx(), HaloHeight.toPx())
    // Tilted like the icon's, and leaning into the hop
    val tilt = HaloTilt + HopLean * motion.heading() * hop
    // A turn about its upright axis: seen edge-on halfway, then back face up
    val turnScale = cos(2f * PI.toFloat() * motion.turn.value)
    // Edge-on it keeps a sliver of width rather than vanishing
    val squeeze = if (abs(turnScale) < EdgeOn) EdgeOn * (if (turnScale < 0f) -1f else 1f) else turnScale
    // Which half is nearer flips while the ring shows its other face
    val nearIsBottom = turnScale >= 0f
    rotate(tilt, center) {
        scale(scaleX = squeeze, scaleY = 1f, pivot = center) {
            drawArc(
                color = color,
                startAngle = if (front == nearIsBottom) 0f else 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = center - Offset(ring.width / 2, ring.height / 2),
                size = ring,
                style = Stroke(HaloStroke.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
private fun Tab(
    label: String,
    selected: Boolean,
    lit: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val colors = HachimiTheme.colorScheme
    val labelColor by animateColorAsState(if (lit) colors.onSurface else colors.onSurfaceVariant)
    // The icon dips as the halo comes over it
    val dip = remember { Animatable(0f) }
    LaunchedEffect(lit) {
        if (lit) {
            dip.snapTo(0f)
            dip.animateTo(1f, tween(140))
            dip.animateTo(0f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow))
        }
    }
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier
            .fillMaxHeight()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interaction,
                indication = null,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(IconSize).graphicsLayer {
                translationY = 1.5.dp.toPx() * dip.value
            },
            contentAlignment = Alignment.Center,
        ) {
            // The whole tab takes the tap, but the press shows only as a small pill behind the icon;
            // it hangs over the icon's box without taking room from the label
            Box(
                Modifier
                    .requiredSize(PressWidth, PressHeight)
                    .clip(PressShape)
                    .indication(interaction, ripple())
            )
            icon()
        }
        Text(
            label,
            modifier = Modifier.padding(top = LabelGap),
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = if (lit) FontWeight.SemiBold else FontWeight.Medium,
            color = labelColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun TabIcon(filled: ImageVector, outlined: ImageVector, selected: Boolean) {
    val colors = HachimiTheme.colorScheme
    val tint by animateColorAsState(if (selected) colors.onSurface else colors.onSurfaceVariant)
    Icon(
        if (selected) filled else outlined,
        contentDescription = null,
        modifier = Modifier.size(IconSize),
        tint = tint,
    )
}

/** The signed-in user's picture as the Me tab's icon. */
@Composable
private fun TabAvatar(url: String) {
    val colors = HachimiTheme.colorScheme
    Box(
        Modifier
            .fillMaxSize()
            .padding(1.dp)
            .clip(CircleShape)
            .background(colors.onSurface.copy(0.12f))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .httpHeaders(CoilHeaders)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

private val IconSize = 24.dp
private val LabelGap = 3.dp
private val LabelLine = 14.dp
private val PressWidth = 56.dp
private val PressHeight = 32.dp
private val PressShape = RoundedCornerShape(16.dp)

private val HaloWidth = 20.dp
private val HaloHeight = 6.dp
private val HaloStroke = 1.75.dp
/** How far above the icon's top edge the ring's middle floats. */
private val HaloRise = 2.dp
private val HopHeight = 10.dp
private const val HaloTilt = -14f
private const val HopLean = 16f
private const val EdgeOn = 0.06f

@Preview(widthDp = 400)
@Composable
private fun PreviewCompactBottomBar() {
    PreviewTheme(background = true) {
        CompactBottomBar(selected = BottomTab.SQUARE, onSelect = {}, meUnread = true)
    }
}
