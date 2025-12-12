package world.hachimi.app.ui.creation.artwork

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


private enum class Tab(
    val title: String
) {
    Published("已发布"), PR("我的提交")
}

@Composable
fun MyArtworkScreen() {
    val pagerState = rememberPagerState(pageCount = { Tab.entries.size })

    Column(Modifier.fillMaxSize()) {
        val scope = rememberCoroutineScope()
        SingleChoiceSegmentedButtonRow(Modifier.padding(top = 24.dp, start = 24.dp)) {
            Tab.entries.forEachIndexed { index, tab ->
                SegmentedButton(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                    label = { Text(text = tab.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
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