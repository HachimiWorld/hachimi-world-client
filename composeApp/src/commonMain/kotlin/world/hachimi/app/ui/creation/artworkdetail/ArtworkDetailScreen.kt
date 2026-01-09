package world.hachimi.app.ui.creation.artworkdetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.ArtworkDetailViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.DevelopingPage
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.creation.publish.components.FormItem
import world.hachimi.app.ui.creation.publish.components.JmidTextField
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.LocalTextStyle
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton

private enum class Tab(
    val title: String
) {
    Detail("详情"), Statistics("统计数据"), ChangeHistory("编辑历史")
}

@Composable
fun ArtworkDetailScreen(
    songId: Long,
    vm: ArtworkDetailViewModel = koinViewModel()
) {
    DisposableEffect(songId, vm) {
        vm.mounted(songId)
        onDispose { vm.dispose() }
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("作品详情", style = MaterialTheme.typography.titleLarge)

        val pagerState = rememberPagerState(pageCount = { Tab.entries.size })
        val scope = rememberCoroutineScope()

        SecondaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.padding(top = 16.dp).widthIn(max = 300.dp).fillMaxWidth()
        ) {
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
                0 -> DetailTab()
                1 -> StatisticsTab()
                2 -> HistoryTab()
            }
        }
    }
}

@Composable
private fun DetailTab(vm: ArtworkDetailViewModel = koinViewModel()) {
    AnimatedContent(vm.initializeStatus) {
        when (it) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> DetailContent(vm)
        }
    }
}

@Composable
private fun StatisticsTab() {
    DevelopingPage()
}

@Composable
private fun HistoryTab() {
    DevelopingPage()
}

@Composable
private fun DetailContent(
    vm: ArtworkDetailViewModel,
    global: GlobalStore = koinInject()
) {
    Column(
        Modifier.fillMaxSize()
            .navigationBarsPadding().windowInsetsPadding(LocalContentInsets.current),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Show all properties
        vm.detail?.let { detail ->
            Row(Modifier.padding(top = 24.dp)) {
                Button(onClick = {
                    global.nav.push(Route.Root.CreationCenter.Modify(songId = vm.detail?.id ?: 0L))
                }) {
                    Text("编辑作品")
                }
            }

            PropertyItem(label = { Text("标题") }) {
                Text(detail.title)
            }
            PropertyItem(label = { Text("副标题") }) {
                Text(detail.subtitle)
            }
            PropertyItem(
                label = {
                    Text("基米ID")
                    IconButton(onClick = { vm.startChangeJmid() }, enabled = !vm.changeOperating) {
                        Icon(Icons.Default.Edit, contentDescription = "Change")
                    }
                }
            ) {
                Text(detail.displayId)
            }
        }

        ChangeJmidDialogHost()
    }
}

@Composable
private fun PropertyItem(
    label: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                label()
            }
        }
        Spacer(Modifier.height(8.dp))
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodySmall.copy(
                color = LocalContentColor.current.copy(
                    0.7f
                )
            )
        ) {
            content()
        }
    }
}

@Composable
private fun PropertyItem(
    label: String,
    content: String
) {
    PropertyItem(label = { Text(label) }, content = { Text(content) })
}

@Composable
private fun ChangeJmidDialogHost(
    vm: ArtworkDetailViewModel = koinViewModel()
) {
    if (vm.showChangeJmidDialog) {
        if (vm.changeJmidAvailable) {
            AlertDialog(
                modifier = Modifier.width(300.dp),
                title = { Text("更改基米 ID") },
                onDismissRequest = { vm.cancelChangeJmid() },
                confirmButton = {
                    Button(
                        onClick = { vm.confirmChangeJmid() },
                        enabled = vm.jmidValid == true && !vm.changeOperating
                    ) {
                        Text("更改")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { vm.cancelChangeJmid() }) {
                        Text("取消")
                    }
                },
                text = {
                    FormItem(header = { Text("新的基米 ID") }) {
                        JmidTextField(
                            vm.jmidNumber,
                            vm.jmidPrefix!!,
                            vm.jmidValid,
                            vm.jmidSupportText,
                            vm::updateJmidNumber
                        )
                    }
                }
            )
        } else {
            AlertDialog(
                title = { Text("当前不可更改") },
                text = { Text("你还没有一个独占的基米ID前缀，当前不可更改") },
                onDismissRequest = { vm.cancelChangeJmid() },
                confirmButton = {
                    TextButton(onClick = { vm.cancelChangeJmid() }) {
                        Text("关闭")
                    }
                }
            )
        }
    }
}