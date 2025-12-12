package world.hachimi.app.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import world.hachimi.app.util.WindowSize

@OptIn(ExperimentalMaterial3Api::class)
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
    } else PullToRefreshBox(
        modifier = modifier,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        content = content
    )
}
