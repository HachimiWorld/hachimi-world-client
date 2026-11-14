package world.hachimi.app.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_back_cd
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.LocalTextStyle
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.insets.multiplatformSafeDrawing
import world.hachimi.app.ui.insets.multiplatformStatusBars
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.util.PlatformBackHandler
import kotlin.math.roundToInt

/**
 * How a page's title sits on a phone.
 *
 * @since 261111
 */
enum class TitleStyle {
    /** Large, at the top of the page, and folded into the bar as the page scrolls. For lists and settings. */
    Large,

    /** Small, in the bar, always. For pages that are one thing: a detail, a form, a thread. */
    Compact,

    /** In the bar only once the page has scrolled, for pages whose own header already shows the name. */
    Revealed,
}

/**
 * True inside a page whose title the surrounding scaffold already shows, so the page leaves out
 * its own heading.
 *
 * @since 261111
 */
val LocalPageTitleShown = compositionLocalOf { false }

/** Set by a scaffold for what it wraps, so a scaffold inside it adds no second bar. */
private val LocalInScaffold = compositionLocalOf { false }

/** Where the page under a phone scaffold puts its main action, through [PageTitleAction]. */
@Stable
private class TitleActionSlot {
    var content by mutableStateOf<(@Composable (folded: Boolean) -> Unit)?>(null)
}

private val LocalTitleActionSlot = compositionLocalOf<TitleActionSlot?> { null }

/**
 * Puts the page's main action, such as playing everything, beside its large title on phones; once
 * the title has folded away it waits in the bar, where [content] gets `folded` and should keep to
 * the bar's size. Call it outside any lazy list, which would drop it
 * as it scrolls by.
 *
 * Returns false, and puts nothing anywhere, when no phone scaffold above holds the page (wide
 * screens), so the page shows the action itself.
 *
 * @since 261114
 */
/**
 * Whether a [PageTitleAction] here would sit by the title, so a placeholder for the page can leave
 * out the page's own button.
 *
 * @since 261114
 */
val titleActionPlaced: Boolean
    @Composable @ReadOnlyComposable get() = LocalTitleActionSlot.current != null

@Composable
fun PageTitleAction(content: @Composable (folded: Boolean) -> Unit): Boolean {
    val slot = LocalTitleActionSlot.current ?: return false
    val latest by rememberUpdatedState(content)
    DisposableEffect(slot) {
        val shown: @Composable (Boolean) -> Unit = { folded -> latest(folded) }
        slot.content = shown
        onDispose { if (slot.content === shown) slot.content = null }
    }
    return true
}

/**
 * A page below the top level: a bar with the way back, the title and the page's actions, over
 * the page. Wide screens keep the plain toolbar ([ScreenToolbar]). On phones the bar sits flat on
 * the page with no line under it until something has scrolled beneath it, and the title follows
 * [titleStyle].
 */
@Composable
fun ScreenScaffold(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    showBack: Boolean = false,
    showMenu: Boolean = false,
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    /** @since 261111 */
    titleStyle: TitleStyle = TitleStyle.Large,
    /**
     * On wide screens, the page draws its own [PageHeading], so the toolbar keeps its title (and
     * the line under it) out of sight until the page has scrolled. Phones are unaffected.
     * @since 261114
     */
    headingInPageOnWide: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Already inside a scaffold that shows this page's bar
    if (LocalInScaffold.current) {
        Column(modifier.fillMaxSize(), content = content)
        return
    }
    if (showBack && onBack != null) {
        PlatformBackHandler(enabled = true, onBack = onBack)
    }

    val phone = LocalWindowSize.current.width < WindowSize.COMPACT
    if (phone) {
        PhoneScaffold(
            title = title,
            subtitle = subtitle,
            onBack = onBack.takeIf { showBack },
            actions = actions,
            // A subtitle needs the bar's two lines
            titleStyle = if (subtitle != null && titleStyle == TitleStyle.Large) TitleStyle.Compact else titleStyle,
            modifier = modifier,
            content = content,
        )
        return
    }

    // Only tracks how far the page has scrolled; nothing folds on wide screens
    val scroll = remember { TitleScroll(0f) }
    val scrollConnection = remember(scroll) { TitleScrollConnection(scroll, folds = false) }
    val revealAt = with(LocalDensity.current) { WideRevealAfter.toPx() }
    val titleAlpha by animateFloatAsState(if (!headingInPageOnWide || scroll.scrolled > revealAt) 1f else 0f)
    val lineAlpha by animateFloatAsState(if (!headingInPageOnWide || scroll.scrolled > 0f) 1f else 0f)
    Column(modifier.fillMaxSize()) {
        ScreenToolbar(
            title = title,
            subtitle = subtitle,
            showBack = showBack,
            showMenu = showMenu,
            onBack = onBack,
            onMenuClick = onMenuClick,
            actions = actions,
            titleAlpha = { titleAlpha },
            lineAlpha = { lineAlpha },
        )
        // Toolbar applied multiplatform top insets; body must not re-apply them.
        Column(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .consumeWindowInsets(WindowInsets.multiplatformStatusBars)
                .then(if (headingInPageOnWide) Modifier.nestedScroll(scrollConnection) else Modifier)
        ) {
            CompositionLocalProvider(LocalInScaffold provides true) {
                content()
            }
        }
    }
}

/**
 * Where the large title has got to. [collapse] runs from 0 (all shown) to [range] (folded away);
 * [scrolled] is how far the page under the bar has scrolled, as far as the bar can tell from the
 * scrolling that passes through it.
 */
@Stable
private class TitleScroll(val range: Float) {
    var collapse by mutableFloatStateOf(0f)
    var scrolled by mutableFloatStateOf(0f)
    val fraction: Float get() = if (range <= 0f) 1f else collapse / range
}

/** The large title folds away before the page scrolls, and opens again only once the page is back at its top. */
private class TitleScrollConnection(private val state: TitleScroll, private val folds: Boolean) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (!folds) return Offset.Zero
        val before = state.collapse
        if (available.y < 0f) {
            state.collapse = (before - available.y).coerceAtMost(state.range)
        } else if (available.y > 0f && state.scrolled <= 0f) {
            // Back at the top, the title opens before anything under it (such as pull to refresh) takes the drag
            state.collapse = (before - available.y).coerceAtLeast(0f)
        }
        return Offset(0f, before - state.collapse)
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        state.scrolled = (state.scrolled - consumed.y).coerceAtLeast(0f)
        if (!folds || available.y <= 0f) return Offset.Zero
        val before = state.collapse
        state.collapse = (before - available.y).coerceAtLeast(0f)
        return Offset(0f, before - state.collapse)
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        // Never left half folded
        if (folds && state.collapse > 0f && state.collapse < state.range) {
            val target = if (state.fraction > 0.5f) state.range else 0f
            animate(state.collapse, target, animationSpec = spring(stiffness = 600f)) { value, _ -> state.collapse = value }
        }
        return Velocity.Zero
    }
}

@Composable
private fun PhoneScaffold(
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)?,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    titleStyle: TitleStyle,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = HachimiTheme.colorScheme
    val density = LocalDensity.current
    val large = titleStyle == TitleStyle.Large
    val actionSlot = remember { TitleActionSlot() }
    val titleAction = actionSlot.content
    // A page action beside the large title (such as a fan of covers) gets a taller row
    val largeTitleHeight = if (titleAction != null) LargeTitleWithActionHeight else LargeTitleHeight
    val state = remember(large, largeTitleHeight) {
        TitleScroll(if (large) with(density) { largeTitleHeight.toPx() } else 0f)
    }
    val connection = remember(state, large) { TitleScrollConnection(state, folds = large) }
    val revealAt = with(density) { RevealAfter.toPx() }

    // The bar's title: in once the large one has mostly folded, or once the page has scrolled
    val smallTitleShown = when (titleStyle) {
        TitleStyle.Large -> state.fraction > 0.75f
        TitleStyle.Compact -> true
        TitleStyle.Revealed -> state.scrolled > revealAt
    }
    val smallTitleAlpha by animateFloatAsState(if (smallTitleShown) 1f else 0f)
    // The line under the bar only once something is under it
    val lineShown = if (large) state.fraction >= 1f && state.scrolled > 0f else state.scrolled > 0f
    val lineAlpha by animateFloatAsState(if (lineShown) 1f else 0f)

    Column(modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(colors.background)
                .windowInsetsPadding(WindowInsets.multiplatformSafeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
        ) {
            Row(
                Modifier.fillMaxWidth().height(BarHeight).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    HachimiIconButton(onClick = onBack, modifier = Modifier.size(48.dp), touchMode = true) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back_cd))
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                }
                Column(
                    Modifier.weight(1f).padding(start = 4.dp, end = 8.dp).graphicsLayer { alpha = smallTitleAlpha },
                ) {
                    CompositionLocalProvider(LocalTextStyle provides SmallTitleStyle.copy(color = LocalContentColor.current)) {
                        title()
                    }
                    subtitle?.let {
                        CompositionLocalProvider(LocalTextStyle provides SubtitleStyle.copy(color = colors.onSurfaceVariant)) {
                            it()
                        }
                    }
                }
                if (titleAction != null) {
                    // Beside the large title while it shows, in the bar once it has folded
                    AnimatedVisibility(!large || smallTitleShown, enter = fadeIn(), exit = fadeOut()) {
                        // In line with where it stood by the large title (16dp in, past the bar's own 4dp)
                        Box(Modifier.padding(start = 8.dp, end = 12.dp)) { titleAction(true) }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, content = actions)
            }
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(1.dp)
                    .graphicsLayer { alpha = lineAlpha }
                    .background(colors.outline)
            )
        }
        if (large) {
            // Folds up under the bar: the box shrinks and the title rides up with its top edge
            Box(
                Modifier
                    .fillMaxWidth()
                    .clipToBounds()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val shift = state.collapse.roundToInt()
                        layout(placeable.width, (placeable.height - shift).coerceAtLeast(0)) {
                            placeable.place(0, -shift)
                        }
                    }
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(largeTitleHeight)
                        .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                        .graphicsLayer { alpha = (1f - state.fraction * 1.4f).coerceIn(0f, 1f) },
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(Modifier.weight(1f)) {
                        CompositionLocalProvider(LocalTextStyle provides LargeTitleStyle.copy(color = LocalContentColor.current)) {
                            title()
                        }
                    }
                    if (titleAction != null && !smallTitleShown) titleAction(false)
                }
            }
        }
        Column(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .consumeWindowInsets(WindowInsets.multiplatformStatusBars)
                .nestedScroll(connection)
        ) {
            CompositionLocalProvider(LocalInScaffold provides true, LocalTitleActionSlot provides actionSlot) {
                content()
            }
        }
    }
}

private val BarHeight = 56.dp
private val LargeTitleHeight = 52.dp
private val LargeTitleWithActionHeight = 64.dp
/** How far a [TitleStyle.Revealed] page scrolls before its name comes into the bar. */
private val RevealAfter = 160.dp
/** Roughly a [PageHeading] and the margin over it: once it's scrolled away the toolbar shows the title. */
private val WideRevealAfter = 88.dp
private val SmallTitleStyle = TextStyle(fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)
private val SubtitleStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp)
/** The large title, here and in [PageHeading]. */
internal val LargeTitleStyle = TextStyle(fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold)

@Preview(widthDp = 360, name = "Compact · Secondary")
@Composable
private fun PreviewScreenScaffoldCompact() {
    PreviewTheme(background = true) {
        CompositionLocalProvider(
            LocalWindowSize provides DpSize(WindowSize.COMPACT - 1.dp, 800.dp),
        ) {
            ScreenScaffold(
                title = { Text("最近发布") },
                showBack = true,
                onBack = {},
                showMenu = false,
            ) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(30) { Text("Row $it", Modifier.padding(16.dp)) }
                }
            }
        }
    }
}

@Preview(widthDp = 360, name = "Compact · Title action")
@Composable
private fun PreviewScreenScaffoldTitleAction() {
    PreviewTheme(background = true) {
        CompositionLocalProvider(
            LocalWindowSize provides DpSize(WindowSize.COMPACT - 1.dp, 800.dp),
        ) {
            ScreenScaffold(title = { Text("每日推荐") }, showBack = true, onBack = {}) {
                PageTitleAction { folded ->
                    if (folded) PlayAllButton(onClick = {})
                    else CoverFan(count = 3, onClick = {}) { index ->
                        Box(Modifier.fillMaxSize().background(listOf(Color(0xFFF2C14E), Color(0xFFE88FB0), Color(0xFF5DB3A0))[index]))
                    }
                }
                LazyColumn(Modifier.fillMaxSize()) {
                    items(30) { Text("Row $it", Modifier.padding(16.dp)) }
                }
            }
        }
    }
}

@Preview(widthDp = 960, heightDp = 400, name = "Expanded · Heading in page")
@Composable
private fun PreviewScreenScaffoldHeadingInPage() {
    PreviewTheme(background = true) {
        CompositionLocalProvider(
            LocalWindowSize provides DpSize(960.dp, 400.dp),
        ) {
            ScreenScaffold(title = { Text("每日推荐") }, showBack = true, onBack = {}, headingInPageOnWide = true) {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp)) {
                    item {
                        PageHeading(
                            title = "每日推荐",
                            caption = "并非根据你的口味进行推荐",
                            action = {
                                CoverFan(count = 3, onClick = {}) { index ->
                                    Box(Modifier.fillMaxSize().background(listOf(Color(0xFFF2C14E), Color(0xFFE88FB0), Color(0xFF5DB3A0))[index]))
                                }
                            },
                        )
                    }
                    items(30) { Text("Row $it", Modifier.padding(vertical = 16.dp)) }
                }
            }
        }
    }
}

@Preview(widthDp = 1280, name = "Expanded · Secondary")
@Composable
private fun PreviewScreenScaffoldExpanded() {
    PreviewTheme(background = true) {
        CompositionLocalProvider(
            LocalWindowSize provides DpSize(1280.dp, 800.dp),
        ) {
            ScreenScaffold(title = { Text("设置") }) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Page content")
                }
            }
        }
    }
}
