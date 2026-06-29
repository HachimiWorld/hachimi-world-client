package world.hachimi.app.ui.util

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import world.hachimi.app.ui.LocalContentInsets

/**
 * Adds padding to the bottom of a list to account for navigation bars and content insets.
 */
@Composable
fun Modifier.listTailPadding(): Modifier =
    this.navigationBarsPadding()
        .padding(LocalContentInsets.current.asPaddingValues())

/**
 * Adds a spacer to the bottom of a list to account for navigation bars and content insets.
 */
@Composable
fun ListTailSpacer() {
    Spacer(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(LocalContentInsets.current.asPaddingValues())
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