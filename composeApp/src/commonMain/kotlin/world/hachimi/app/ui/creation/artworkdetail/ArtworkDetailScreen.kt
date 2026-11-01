package world.hachimi.app.ui.creation.artworkdetail

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.artwork_change_jmid_title
import hachimiworld.composeapp.generated.resources.artwork_change_jmid_unavailable_text
import hachimiworld.composeapp.generated.resources.artwork_change_jmid_unavailable_title
import hachimiworld.composeapp.generated.resources.artwork_details_title
import hachimiworld.composeapp.generated.resources.artwork_edit
import hachimiworld.composeapp.generated.resources.artwork_new_jmid
import hachimiworld.composeapp.generated.resources.artwork_tab_detail
import hachimiworld.composeapp.generated.resources.artwork_tab_history
import hachimiworld.composeapp.generated.resources.artwork_tab_statistics
import hachimiworld.composeapp.generated.resources.common_cancel
import hachimiworld.composeapp.generated.resources.common_change
import hachimiworld.composeapp.generated.resources.common_close
import hachimiworld.composeapp.generated.resources.moderation_song_hidden_body
import hachimiworld.composeapp.generated.resources.moderation_song_hidden_title
import hachimiworld.composeapp.generated.resources.moderation_submit_modification
import hachimiworld.composeapp.generated.resources.review_id_label
import hachimiworld.composeapp.generated.resources.review_subtitle_label
import hachimiworld.composeapp.generated.resources.review_title_label
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.ArtworkDetailViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.DevelopingPage
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.creation.publish.components.FormItem
import world.hachimi.app.ui.creation.publish.components.JmidTextField
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.LocalTextStyle
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.report.ModerationNotice
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.fillMaxWidthIn
import world.hachimi.app.ui.util.listTailPadding

private enum class Tab(
    val title: StringResource
) {
    Detail(Res.string.artwork_tab_detail),
    Statistics(Res.string.artwork_tab_statistics),
    ChangeHistory(Res.string.artwork_tab_history)
}

@Composable
fun ArtworkDetailScreen(
    songId: Long,
    vm: ArtworkDetailViewModel = koinViewModel()
) {
    val navigator = LocalNavigator.current

    DisposableEffect(songId, vm) {
        vm.mounted(songId)
        onDispose { vm.dispose() }
    }

    ScreenScaffold(
        title = { Text(stringResource(Res.string.artwork_details_title), maxLines = 1) },
        showBack = true,
        onBack = navigator::back
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .fillMaxWidthIn()
                .padding(AdaptiveScreenMargin)
                .listTailPadding()
        ) {
            val pagerState = rememberPagerState(pageCount = { Tab.entries.size })
            val scope = rememberCoroutineScope()

            if (vm.detail?.isHidden == true) {
                ModerationNotice(
                    title = stringResource(Res.string.moderation_song_hidden_title),
                    body = stringResource(Res.string.moderation_song_hidden_body),
                    reason = vm.hiddenReason,
                    modifier = Modifier.padding(top = 8.dp),
                    action = {
                        AccentButton(onClick = { navigator.push(Route.Root.CreationCenter.Modify(songId)) }) {
                            Text(stringResource(Res.string.moderation_submit_modification))
                        }
                    },
                )
            }

            Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tab.entries.forEachIndexed { index, tab ->
                    val selected = pagerState.currentPage == index
                    if (selected) AccentButton(onClick = {}) {
                        Text(text = stringResource(tab.title), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    } else Button(onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }) {
                        Text(text = stringResource(tab.title), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
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
}

@Composable
private fun DetailTab(vm: ArtworkDetailViewModel = koinViewModel()) {
    AnimatedContent(vm.initializeStatus) {
        when (it) {
            InitializeStatus.INIT -> LoadingPage(Modifier.padding(bottom = 24.dp))
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() }, modifier = Modifier.padding(bottom = 24.dp))
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
    val navigator = LocalNavigator.current

    Column(
        Modifier.fillMaxSize()
            .listTailPadding()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Show all properties
        vm.detail?.let { detail ->
            Row(Modifier.padding(top = 24.dp)) {
                Button(onClick = {
                    navigator.push(Route.Root.CreationCenter.Modify(songId = vm.detail?.id ?: 0L))
                }) {
                    Text(stringResource(Res.string.artwork_edit))
                }
            }

            PropertyItem(label = { Text(stringResource(Res.string.review_title_label)) }) {
                Text(detail.title)
            }
            PropertyItem(label = { Text(stringResource(Res.string.review_subtitle_label)) }) {
                Text(detail.subtitle)
            }
            PropertyItem(
                label = {
                    Text(stringResource(Res.string.review_id_label))
                    IconButton(onClick = { vm.startChangeJmid() }, enabled = !vm.changeOperating) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.common_change))
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
                title = { Text(stringResource(Res.string.artwork_change_jmid_title)) },
                onDismissRequest = { vm.cancelChangeJmid() },
                confirmButton = {
                    Button(
                        onClick = { vm.confirmChangeJmid() },
                        enabled = vm.jmidValid == true && !vm.changeOperating
                    ) {
                        Text(stringResource(Res.string.common_change))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { vm.cancelChangeJmid() }) {
                        Text(stringResource(Res.string.common_cancel))
                    }
                },
                text = {
                    FormItem(header = { Text(stringResource(Res.string.artwork_new_jmid)) }) {
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
                title = { Text(stringResource(Res.string.artwork_change_jmid_unavailable_title)) },
                text = { Text(stringResource(Res.string.artwork_change_jmid_unavailable_text)) },
                onDismissRequest = { vm.cancelChangeJmid() },
                confirmButton = {
                    TextButton(onClick = { vm.cancelChangeJmid() }) {
                        Text(stringResource(Res.string.common_close))
                    }
                }
            )
        }
    }
}