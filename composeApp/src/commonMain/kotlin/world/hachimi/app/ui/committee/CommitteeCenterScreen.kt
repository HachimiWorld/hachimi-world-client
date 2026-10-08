package world.hachimi.app.ui.committee

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.committee_members
import hachimiworld.composeapp.generated.resources.committee_no_access
import hachimiworld.composeapp.generated.resources.committee_queue_empty
import hachimiworld.composeapp.generated.resources.committee_resolved_empty
import hachimiworld.composeapp.generated.resources.committee_tab_pending
import hachimiworld.composeapp.generated.resources.committee_tab_resolved
import hachimiworld.composeapp.generated.resources.committee_title
import hachimiworld.composeapp.generated.resources.report_pending_count
import hachimiworld.composeapp.generated.resources.report_target_deleted
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.ReportModule
import world.hachimi.app.api.module.ReportModule.CaseItem
import world.hachimi.app.model.CommitteeViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.TabBar
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.SubtleButton
import world.hachimi.app.ui.message.components.cardItems
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.InitStatusScaffold
import world.hachimi.app.ui.util.contentPaddingForMaxWidth
import world.hachimi.app.ui.util.listHeadInsetsSpacerItem
import world.hachimi.app.ui.util.listTailSpacerItem
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime

private val STATUSES = listOf(ReportModule.STATUS_PENDING, ReportModule.STATUS_RESOLVED)

/** The report queue, for committee members and contributors. */
@Composable
fun CommitteeCenterScreen(vm: CommitteeViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    LaunchedEffect(vm) { vm.mounted() }

    InitStatusScaffold(
        initializeStatus = vm.initializeStatus,
        isLoading = false,
        onRetryClick = vm::retry,
        modifier = Modifier.fillMaxSize(),
    ) {
        if (vm.access?.canView != true) {
            Box(Modifier.fillMaxSize().padding(AdaptiveScreenMargin), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.committee_no_access), color = HachimiTheme.colorScheme.onSurfaceVariant)
            }
            return@InitStatusScaffold
        }
        val listState = rememberLazyListState()
        LaunchedEffect(listState.canScrollForward, vm.hasMore, vm.loadingMore, vm.loadMoreFailed) {
            if (!listState.canScrollForward && vm.hasMore && !vm.loadingMore && !vm.loadMoreFailed) vm.loadMore()
        }
        BoxWithConstraints {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPaddingForMaxWidth(PaddingValues(AdaptiveScreenMargin), maxWidth, 960.dp),
            ) {
                listHeadInsetsSpacerItem()
                item(key = "header") {
                    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(Res.string.committee_title),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        SubtleButton(onClick = { navigator.push(Route.Root.CommitteeMembers) }) {
                            Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(stringResource(Res.string.committee_members), modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
                item(key = "tabs") {
                    TabBar(
                        tabs = listOf(stringResource(Res.string.committee_tab_pending), stringResource(Res.string.committee_tab_resolved)),
                        selectedIndex = STATUSES.indexOf(vm.status),
                        onTabSelected = { vm.selectStatus(STATUSES[it]) },
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
                if (vm.items.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            stringResource(
                                if (vm.status == ReportModule.STATUS_PENDING) Res.string.committee_queue_empty
                                else Res.string.committee_resolved_empty
                            ),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            color = HachimiTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
                cardItems(items = vm.items.toList(), key = { it.caseId }) { item ->
                    CaseRow(item, onClick = { navigator.push(Route.Root.ReportCase(item.targetType, item.targetId)) })
                }
                item(key = "tail") {
                    if (vm.hasMore) LoadMoreItem(hasMore = true, isLoading = vm.loadingMore)
                }
                listTailSpacerItem()
            }
        }
    }
}

@Composable
private fun CaseRow(item: CaseItem, onClick: () -> Unit) {
    val target = item.target
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        TargetCover(item.targetType, target?.coverUrl)
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                text = target?.title ?: stringResource(Res.string.report_target_deleted),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (target == null) HachimiTheme.colorScheme.onSurfaceVariant else Color.Unspecified,
            )
            Text(
                text = listOfNotNull(
                    targetTypeLabel(item.targetType),
                    target?.owner?.username?.takeIf { item.targetType != ReportModule.TARGET_USER },
                ).joinToString(" · "),
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodySmall,
                color = HachimiTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.reasons.isNotEmpty()) {
                ReasonPills(item.reasons, Modifier.padding(top = 10.dp))
            }
        }
        Column(Modifier.padding(start = 12.dp), horizontalAlignment = Alignment.End) {
            Text(
                text = formatTime(item.lastReportTime, distance = true, precise = false, fullFormat = LocalDateTime.Formats.YMD),
                style = MaterialTheme.typography.bodySmall,
                color = HachimiTheme.colorScheme.onSurfaceVariant,
            )
            if (item.pendingCount > 0) {
                Pill(stringResource(Res.string.report_pending_count, item.pendingCount), Modifier.padding(top = 8.dp), accent = true)
            }
        }
    }
}
