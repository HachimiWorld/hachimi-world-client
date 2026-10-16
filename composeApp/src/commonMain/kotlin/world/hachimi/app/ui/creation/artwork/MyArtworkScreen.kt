package world.hachimi.app.ui.creation.artwork

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.artwork_tab_published
import hachimiworld.composeapp.generated.resources.artwork_tab_submissions
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch
import world.hachimi.app.ui.design.components.TabBar
import world.hachimi.app.ui.insets.multiplatformStatusBarsPadding
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.fillMaxWidthIn

private enum class Tab(
    val title: StringResource
) {
    Published(Res.string.artwork_tab_published), PR(Res.string.artwork_tab_submissions)
}

@Composable
fun MyArtworkScreen() {
    val pagerState = rememberPagerState(pageCount = { Tab.entries.size })

    Column(Modifier.fillMaxSize().fillMaxWidthIn().multiplatformStatusBarsPadding()) {
        val scope = rememberCoroutineScope()

        TabBar(
            tabs = Tab.entries.map { stringResource(it.title) },
            selectedIndex = pagerState.currentPage,
            onTabSelected = { index ->
                scope.launch {
                    pagerState.animateScrollToPage(index)
                }
            },
            modifier = Modifier.padding(
                top = AdaptiveScreenMargin,
                start = AdaptiveScreenMargin,
                end = AdaptiveScreenMargin,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        HorizontalPager(pagerState, modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (it) {
                0 -> PublishedTabContent()
                1 -> PRTabContent()
            }
        }
    }
}