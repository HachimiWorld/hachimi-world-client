package world.hachimi.app.ui.creation.artwork

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.coroutines.launch
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.fillMaxWidthIn


private enum class Tab(
    val title: String
) {
    Published("已发布"), PR("我的提交")
}

@Composable
fun MyArtworkScreen() {
    val pagerState = rememberPagerState(pageCount = { Tab.entries.size })

    Column(Modifier.fillMaxSize().fillMaxWidthIn()) {
        val scope = rememberCoroutineScope()

        Row(Modifier.padding(top = 24.dp, start = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tab.entries.fastForEachIndexed { index, tab ->
                val selected = pagerState.currentPage == index
                if (selected) AccentButton(onClick = {}) {
                    Text(text = tab.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                } else Button(onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }) {
                    Text(text = tab.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        HorizontalPager(pagerState, modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (it) {
                0 -> PublishedTabContent()
                1 -> PRTabContent()
            }
        }
    }
}