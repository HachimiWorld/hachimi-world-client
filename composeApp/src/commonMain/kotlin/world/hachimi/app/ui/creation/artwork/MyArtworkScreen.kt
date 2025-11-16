package world.hachimi.app.ui.creation.artwork

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore


private enum class Tab(
    val title: String
) {
    Published("已发布"), PR("我的提交")
}

@Composable
fun MyArtworkScreen() {
    val global = koinInject<GlobalStore>()
    val pagerState = rememberPagerState(pageCount = { 2 })

    Column(Modifier.fillMaxSize()) {
        val scope = rememberCoroutineScope()

        PrimaryTabRow(selectedTabIndex = pagerState.currentPage, modifier = Modifier.fillMaxWidth().widthIn(max = 300.dp)) {
            Tab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = tab.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                )
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