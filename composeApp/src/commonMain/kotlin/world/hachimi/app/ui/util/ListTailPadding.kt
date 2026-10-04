package world.hachimi.app.ui.util

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.insets.multiplatformNavigationBars
import world.hachimi.app.ui.insets.multiplatformSafeDrawing

/**
 * Top safe-area spacer for scroll content (edge-to-edge).
 * Compact AppBar / ScreenScaffold consume system bars → height 0.
 */
@Composable
fun ListHeadInsetsSpacer(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier.windowInsetsTopHeight(WindowInsets.multiplatformSafeDrawing)
    )
}

fun LazyListScope.listHeadInsetsSpacerItem() {
    item(key = "status_bar_insets", contentType = "insets_head") {
        ListHeadInsetsSpacer()
    }
}

fun LazyGridScope.listHeadInsetsSpacerItem() {
    item(
        key = "status_bar_insets",
        contentType = "insets_head",
        span = { GridItemSpan(maxLineSpan) },
    ) {
        ListHeadInsetsSpacer()
    }
}

/**
 * Bottom-only padding for list tails (MiniPlayer / shell [LocalContentInsets]).
 * Top insets use [listHeadInsetsSpacerItem] so content can draw under chrome.
 */
@Composable
fun Modifier.listTailPadding(): Modifier = this.windowInsetsPadding(
    WindowInsets.multiplatformNavigationBars
        .add(LocalContentInsets.current)
        .only(WindowInsetsSides.Bottom)
)

/**
 * Adds a spacer to the bottom of a list to account for navigation bars and content insets.
 */
@Composable
fun ListTailSpacer(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier
            .windowInsetsBottomHeight(
                WindowInsets.multiplatformNavigationBars
                    .add(LocalContentInsets.current)
            )
    )
}

/**
 * Adds a spacer to the bottom of a LazyList to account for navigation bars and content insets.
 */
fun LazyListScope.listTailSpacerItem() {
    item("button_safe_area") {
        ListTailSpacer()
    }
}

/**
 * Adds a spacer to the bottom of a LazyGrid to account for navigation bars and content insets.
 */
fun LazyGridScope.listTailSpacerItem() {
    item("button_safe_area", span = { GridItemSpan(maxLineSpan) }) {
        ListTailSpacer()
    }
}