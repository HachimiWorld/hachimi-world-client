package world.hachimi.app.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.report_action
import hachimiworld.composeapp.generated.resources.report_cancel
import hachimiworld.composeapp.generated.resources.report_choose_reason
import hachimiworld.composeapp.generated.resources.report_detail_hint
import hachimiworld.composeapp.generated.resources.report_reason_abuse
import hachimiworld.composeapp.generated.resources.report_reason_copyright
import hachimiworld.composeapp.generated.resources.report_reason_illegal
import hachimiworld.composeapp.generated.resources.report_reason_nsfw
import hachimiworld.composeapp.generated.resources.report_reason_other
import hachimiworld.composeapp.generated.resources.report_reason_spam
import hachimiworld.composeapp.generated.resources.report_submit
import hachimiworld.composeapp.generated.resources.report_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import world.hachimi.app.api.module.ReportModule
import world.hachimi.app.model.ReportStore
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.DropdownMenu
import world.hachimi.app.ui.design.components.DropdownMenuItem
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.RadioButton
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField

/** Drawn once by the root screen; shown while [ReportStore.target] is set. */
@Composable
fun ReportDialogHost(store: ReportStore = koinInject()) {
    if (store.target == null) return
    AlertDialog(
        onDismissRequest = store::dismiss,
        title = { Text(stringResource(Res.string.report_title)) },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    stringResource(Res.string.report_choose_reason),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HachimiTheme.colorScheme.onSurfaceVariant,
                )
                ReportModule.REASONS.forEach { reason ->
                    RadioButton(
                        selected = store.reason == reason,
                        onClick = { store.reason = reason },
                        enabled = !store.submitting,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(reasonLabel(reason))
                    }
                }
                TextField(
                    value = store.detail,
                    onValueChange = { if (it.length <= 500) store.detail = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(Res.string.report_detail_hint)) },
                    enabled = !store.submitting,
                    minLines = 2,
                    maxLines = 4,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = store::submit, enabled = store.canSubmit) {
                Text(stringResource(Res.string.report_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = store::dismiss, enabled = !store.submitting) {
                Text(stringResource(Res.string.report_cancel))
            }
        },
    )
}

@Composable
fun reasonLabel(reason: String): String = when (reason) {
    "spam" -> stringResource(Res.string.report_reason_spam)
    "abuse" -> stringResource(Res.string.report_reason_abuse)
    "illegal" -> stringResource(Res.string.report_reason_illegal)
    "nsfw" -> stringResource(Res.string.report_reason_nsfw)
    "copyright" -> stringResource(Res.string.report_reason_copyright)
    "other" -> stringResource(Res.string.report_reason_other)
    else -> reason
}

/** A "more" button whose menu has a single report item, for page toolbars. */
@Composable
fun ReportMenuButton(onReport: () -> Unit) {
    Box {
        var expanded by remember { mutableStateOf(false) }
        HachimiIconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = null)
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    onReport()
                },
                text = { Text(stringResource(Res.string.report_action)) },
                leadingIcon = { Icon(Icons.Outlined.Flag, null) },
            )
        }
    }
}
