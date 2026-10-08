package world.hachimi.app.ui.committee

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.report_actions_title
import hachimiworld.composeapp.generated.resources.report_author_reason_hint
import hachimiworld.composeapp.generated.resources.report_author_reason_optional_hint
import hachimiworld.composeapp.generated.resources.report_author_reason_shown
import hachimiworld.composeapp.generated.resources.report_case_history
import hachimiworld.composeapp.generated.resources.report_case_ignored_later
import hachimiworld.composeapp.generated.resources.report_case_no_reports
import hachimiworld.composeapp.generated.resources.report_case_reports
import hachimiworld.composeapp.generated.resources.report_case_title
import hachimiworld.composeapp.generated.resources.report_ignore_later
import hachimiworld.composeapp.generated.resources.report_ignore_later_desc
import hachimiworld.composeapp.generated.resources.report_note_hint
import hachimiworld.composeapp.generated.resources.report_resolve_submit
import hachimiworld.composeapp.generated.resources.report_resolve_title
import hachimiworld.composeapp.generated.resources.report_target_deleted
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.ReportModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.ReportCaseViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.SubtleButton
import world.hachimi.app.ui.design.components.Switcher
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.message.components.cardItems
import world.hachimi.app.ui.report.reasonLabel
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.InitStatusScaffold
import world.hachimi.app.ui.util.ListTailSpacer
import world.hachimi.app.ui.util.listTailSpacerItem
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime
import kotlin.time.Instant

private val DecisionPanelWidth = 340.dp

/**
 * One report case: the target, its pending reports and past decisions, and for contributors the
 * decision form, which sits beside the list on wide windows.
 */
@Composable
fun ReportCaseScreen(
    targetType: String,
    targetId: Long,
    vm: ReportCaseViewModel = koinViewModel(),
) {
    val navigator = LocalNavigator.current
    LaunchedEffect(targetType, targetId) { vm.mounted(targetType, targetId) }

    ScreenScaffold(
        title = { Text(stringResource(Res.string.report_case_title), maxLines = 1) },
        showBack = true,
        onBack = navigator::back,
    ) {
        InitStatusScaffold(
            initializeStatus = vm.initializeStatus,
            isLoading = false,
            onRetryClick = vm::retry,
            modifier = Modifier.fillMaxSize(),
        ) {
            val case = vm.case ?: return@InitStatusScaffold
            val showDecision = vm.canResolve && case.verdicts.isNotEmpty()
            BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                val twoPane = showDecision && maxWidth >= 860.dp
                Row(Modifier.widthIn(max = 1120.dp).fillMaxSize().padding(horizontal = AdaptiveScreenMargin)) {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        contentPadding = PaddingValues(top = AdaptiveScreenMargin),
                    ) {
                        item(key = "target") { TargetHeader(case.case) }
                        reportsSection(case)
                        if (showDecision && !twoPane) {
                            item(key = "decision") {
                                Column(Modifier.padding(top = 28.dp)) { DecisionPanel(vm, case.verdicts, case.case.targetType) }
                            }
                        }
                        historySection(case)
                        listTailSpacerItem()
                    }
                    if (twoPane) {
                        Column(
                            Modifier
                                .padding(start = 24.dp)
                                .width(DecisionPanelWidth)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(top = AdaptiveScreenMargin),
                        ) {
                            DecisionPanel(vm, case.verdicts, case.case.targetType)
                            ListTailSpacer()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetHeader(case: ReportModule.CaseItem, global: GlobalStore = koinInject()) {
    val navigator = LocalNavigator.current
    val target = case.target
    val type = ReportTargetType.of(case.targetType)
    val open: (() -> Unit)? = if (type != null && target != null) {
        { type.open(case.targetId, target, navigator, global.player) }
    } else null
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            TargetCover(case.targetType, target?.coverUrl, size = 72.dp)
            Column(Modifier.weight(1f).padding(start = 16.dp)) {
                Pill(targetTypeLabel(case.targetType))
                Text(
                    text = target?.title ?: stringResource(Res.string.report_target_deleted),
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (target == null) HachimiTheme.colorScheme.onSurfaceVariant else Color.Unspecified,
                )
                val owner = target?.owner
                if (owner != null && type?.showsOwner == true) {
                    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(owner.avatarUrl, 18.dp)
                        Text(
                            text = listOf(owner.username, target.displayId).filter { it.isNotEmpty() }.joinToString(" · "),
                            modifier = Modifier.padding(start = 6.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = HachimiTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (type != null && open != null) {
                SubtleButton(onClick = open, modifier = Modifier.padding(start = 12.dp)) {
                    Text(stringResource(type.openLabel))
                }
            }
        }
    }
}

private fun LazyListScope.reportsSection(case: ReportModule.CaseResp) {
    item(key = "reports_title") {
        Column(Modifier.padding(top = 28.dp)) {
            SectionTitle(
                stringResource(Res.string.report_case_reports),
                trailing = case.pendingReports.size.takeIf { it > 0 }?.toString(),
            )
            if (case.case.reasons.isNotEmpty()) {
                ReasonPills(case.case.reasons, Modifier.padding(start = 4.dp, bottom = 12.dp))
            }
        }
    }
    if (case.pendingReports.isEmpty()) {
        item(key = "no_reports") {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(Res.string.report_case_no_reports),
                    modifier = Modifier.padding(20.dp),
                    color = HachimiTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
    cardItems(case.pendingReports, key = { "report_${it.reportId}" }) { report ->
        EntryRow(
            avatarUrl = report.reporter?.avatarUrl,
            name = report.reporter?.username,
            time = report.createTime,
            tags = { Pill(reasonLabel(report.reason)) },
            body = report.detail,
        )
    }
}

private fun LazyListScope.historySection(case: ReportModule.CaseResp) {
    if (case.actions.isEmpty()) return
    item(key = "history_title") {
        SectionTitle(stringResource(Res.string.report_case_history), Modifier.padding(top = 28.dp))
    }
    cardItems(case.actions, key = { "action_${it.actionId}" }) { action ->
        EntryRow(
            avatarUrl = action.operator?.avatarUrl,
            name = action.operator?.username,
            time = action.createTime,
            tags = {
                Pill(verdictLabel(action.verdict), accent = action.verdict == ReportModule.VERDICT_AGREE)
                action.contentActions.forEach { Pill(contentActionLabel(it)) }
                if (action.ignoreReports) Pill(stringResource(Res.string.report_case_ignored_later))
            },
            body = listOfNotNull(
                action.authorReason?.let { stringResource(Res.string.report_author_reason_shown, it) },
                action.note,
            ).joinToString("\n").ifEmpty { null },
        )
    }
}

@Composable
private fun EntryRow(
    avatarUrl: String?,
    name: String?,
    time: Instant,
    tags: @Composable () -> Unit,
    body: String?,
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Avatar(avatarUrl, 32.dp)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name.orEmpty(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatTime(time, distance = true, precise = false, fullFormat = LocalDateTime.Formats.YMD),
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = HachimiTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) { tags() }
            if (!body.isNullOrBlank()) {
                Text(
                    text = body,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun DecisionPanel(vm: ReportCaseViewModel, verdicts: List<String>, targetType: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(
                stringResource(Res.string.report_resolve_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                verdicts.forEach { verdict ->
                    VerdictOption(
                        title = verdictLabel(verdict),
                        description = verdictDescription(verdict),
                        selected = vm.verdict == verdict,
                        enabled = !vm.resolving,
                        onClick = { vm.selectVerdict(verdict) },
                    )
                }
            }
            val actions = vm.actionsForVerdict
            if (actions.isNotEmpty()) {
                Text(
                    stringResource(Res.string.report_actions_title),
                    modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = HachimiTheme.colorScheme.onSurfaceVariant,
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    actions.forEach { action ->
                        ActionToggle(
                            title = contentActionLabel(action),
                            description = contentActionDescription(action, targetType),
                            checked = action in vm.contentActions,
                            enabled = !vm.resolving,
                            onClick = { vm.toggleAction(action) },
                        )
                    }
                }
            }
            if (vm.contentActions.isNotEmpty()) {
                TextField(
                    value = vm.authorReason,
                    onValueChange = { if (it.length <= 500) vm.authorReason = it },
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                    placeholder = {
                        Text(stringResource(if (vm.needsAuthorReason) Res.string.report_author_reason_hint else Res.string.report_author_reason_optional_hint))
                    },
                    enabled = !vm.resolving,
                    minLines = 2,
                    maxLines = 4,
                )
            }
            Spacer(Modifier.height(16.dp))
            TextField(
                value = vm.note,
                onValueChange = { if (it.length <= 1000) vm.note = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.report_note_hint)) },
                enabled = !vm.resolving,
                minLines = 3,
                maxLines = 6,
            )
            Row(Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(Res.string.report_ignore_later), style = MaterialTheme.typography.labelLarge)
                    Text(
                        stringResource(Res.string.report_ignore_later_desc),
                        modifier = Modifier.padding(top = 2.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = HachimiTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switcher(
                    checked = vm.ignoreReports,
                    onCheckedChange = { vm.ignoreReports = it },
                    enabled = !vm.resolving,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            AccentButton(
                onClick = vm::resolve,
                enabled = vm.canSubmit,
                modifier = Modifier.padding(top = 20.dp).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.report_resolve_submit))
                }
            }
        }
    }
}

@Composable
private fun ActionToggle(title: String, description: String?, checked: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = HachimiTheme.colorScheme
    val shape = RoundedCornerShape(10.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (checked) colors.primary.copy(alpha = 0.10f) else Color.Transparent)
            .border(1.dp, if (checked) colors.primary else colors.onSurface.copy(alpha = 0.10f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (checked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (checked) colors.primary else colors.onSurfaceVariant,
        )
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            if (description != null) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun VerdictOption(title: String, description: String?, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = HachimiTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) colors.primary.copy(alpha = 0.10f) else colors.onSurface.copy(alpha = 0.04f))
            .border(1.dp, if (selected) colors.primary else colors.onSurface.copy(alpha = 0.08f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) colors.primary else Color.Unspecified,
        )
        if (description != null) {
            Text(
                description,
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}
