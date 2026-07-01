package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.tooling.preview.Preview
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun RefreshingIndicator(isRefreshing: Boolean, modifier: Modifier = Modifier.fillMaxWidth().wrapContentWidth(align = Alignment.CenterHorizontally)) {
    val state = rememberPullToRefreshState()
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            state.animateToThreshold()
        } else {
            state.animateToHidden()
        }
    }
    PullToRefreshDefaults.Indicator(
        state, isRefreshing, modifier = modifier,
        containerColor = HachimiTheme.colorScheme.surface.compositeOver(HachimiTheme.colorScheme.background),
        color = HachimiTheme.colorScheme.onSurface
    )
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        RefreshingIndicator(isRefreshing = true)
    }
}