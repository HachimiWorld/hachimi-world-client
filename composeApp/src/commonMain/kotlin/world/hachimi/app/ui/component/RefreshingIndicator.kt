package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import world.hachimi.app.ui.design.components.PullRefreshIndicator
import world.hachimi.app.ui.theme.PreviewTheme

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
    PullRefreshIndicator(state, isRefreshing, modifier = modifier)
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        RefreshingIndicator(isRefreshing = true)
    }
}