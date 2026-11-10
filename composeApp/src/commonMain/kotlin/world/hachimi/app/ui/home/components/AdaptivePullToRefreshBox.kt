package world.hachimi.app.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import world.hachimi.app.ui.design.components.PullRefreshBox
import world.hachimi.app.ui.util.WindowSize

@Composable
fun AdaptivePullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    screenWidth: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    if (screenWidth >= WindowSize.COMPACT) {
        Box(
            modifier = modifier,
            content = content
        )
    } else PullRefreshBox(
        modifier = modifier,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        content = content
    )
}
