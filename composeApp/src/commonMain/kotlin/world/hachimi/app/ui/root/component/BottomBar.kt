package world.hachimi.app.ui.root.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.nav.BottomTab
import world.hachimi.app.ui.TestTags
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.insets.multiplatformNavigationBars
import world.hachimi.app.ui.theme.PreviewTheme

/** The bar's height, without the system navigation bar under it. @since 261110 */
val CompactBottomBarHeight = 56.dp

/**
 * The phone's bottom bar: home, the square, and me. It sits flat on the page with no line
 * over it (the shell fades the page out above it), and a short mark slides to the tab that's open, the
 * same mark the square's tabs use.
 *
 * @param avatarUrl Stands in for the person icon once someone is signed in.
 * @param meUnread Puts a dot on the Me tab while messages are unread.
 * @since 261110
 */
@Composable
fun CompactBottomBar(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    meUnread: Boolean = false,
) {
    val colors = HachimiTheme.colorScheme
    val position by animateFloatAsState(
        selected.ordinal.toFloat(),
        spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
    )
    Box(
        modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(colors.background)
            }
            .windowInsetsPadding(WindowInsets.multiplatformNavigationBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(CompactBottomBarHeight)
                .selectableGroup()
                .drawBehind {
                    val tabWidth = size.width / BottomTab.entries.size
                    val mark = Size(MarkWidth.toPx(), MarkHeight.toPx())
                    drawRoundRect(
                        colors.primary,
                        topLeft = Offset((position + 0.5f) * tabWidth - mark.width / 2, 0f),
                        size = mark,
                        cornerRadius = CornerRadius(mark.height / 2),
                    )
                },
        ) {
            BottomTab.entries.forEach { tab ->
                val isSelected = tab == selected
                Tab(
                    label = stringResource(
                        when (tab) {
                            BottomTab.HOME -> Res.string.nav_home_title
                            BottomTab.SQUARE -> Res.string.nav_square
                            BottomTab.ME -> Res.string.nav_me
                        }
                    ),
                    selected = isSelected,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.weight(1f).testTag(
                        when (tab) {
                            BottomTab.HOME -> TestTags.NAV_HOME
                            BottomTab.SQUARE -> TestTags.NAV_EVENTS
                            BottomTab.ME -> TestTags.NAV_ME
                        }
                    ),
                ) {
                    when (tab) {
                        BottomTab.HOME -> TabIcon(Icons.Filled.Home, Icons.Outlined.Home, isSelected)
                        BottomTab.SQUARE -> TabIcon(Icons.Filled.Forum, Icons.Outlined.Forum, isSelected)
                        BottomTab.ME -> Box {
                            if (avatarUrl != null) {
                                TabAvatar(avatarUrl, isSelected)
                            } else {
                                TabIcon(Icons.Filled.Person, Icons.Outlined.Person, isSelected)
                            }
                            if (meUnread) {
                                Box(
                                    Modifier.align(Alignment.TopEnd).graphicsLayer {
                                        translationX = 3.dp.toPx()
                                        translationY = -2.dp.toPx()
                                    }.size(10.dp).background(colors.background, CircleShape).padding(1.5.dp)
                                        .background(colors.primary, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Tab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val colors = HachimiTheme.colorScheme
    // A light press on the icon as its tab is chosen
    val scale = remember { Animatable(1f) }
    LaunchedEffect(selected) {
        if (selected) {
            scale.snapTo(0.86f)
            scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium))
        }
    }
    Column(
        modifier
            .fillMaxHeight()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 36.dp),
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(24.dp).graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
            contentAlignment = Alignment.Center,
        ) { icon() }
        Text(
            label,
            modifier = Modifier.padding(top = 3.dp),
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) colors.primary else colors.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun TabIcon(filled: ImageVector, outlined: ImageVector, selected: Boolean) {
    val colors = HachimiTheme.colorScheme
    Icon(
        if (selected) filled else outlined,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = if (selected) colors.primary else colors.onSurfaceVariant,
    )
}

/** The signed-in user's picture as the Me tab's icon, ringed while the tab is open. */
@Composable
private fun TabAvatar(url: String, selected: Boolean) {
    val colors = HachimiTheme.colorScheme
    Box(
        Modifier
            .fillMaxSize()
            .border(1.5.dp, if (selected) colors.primary else Color.Transparent, CircleShape)
            .padding(if (selected) 3.dp else 1.dp)
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

private val MarkWidth = 28.dp
private val MarkHeight = 3.dp

@Preview(widthDp = 400)
@Composable
private fun PreviewCompactBottomBar() {
    PreviewTheme(background = true) {
        CompactBottomBar(selected = BottomTab.SQUARE, onSelect = {}, meUnread = true)
    }
}
