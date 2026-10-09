package world.hachimi.app.ui.committee

import androidx.compose.runtime.Composable
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.report_verdict_agree_desc
import hachimiworld.composeapp.generated.resources.report_verdict_agree
import hachimiworld.composeapp.generated.resources.report_verdict_disagree
import hachimiworld.composeapp.generated.resources.report_verdict_disagree_desc
import hachimiworld.composeapp.generated.resources.report_verdict_ignore
import hachimiworld.composeapp.generated.resources.report_verdict_ignore_desc
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.module.ReportModule

@Composable
fun targetTypeLabel(type: String): String =
    ReportTargetType.of(type)?.let { stringResource(it.label) } ?: type

@Composable
fun verdictLabel(verdict: String): String = when (verdict) {
    ReportModule.VERDICT_AGREE -> stringResource(Res.string.report_verdict_agree)
    ReportModule.VERDICT_DISAGREE -> stringResource(Res.string.report_verdict_disagree)
    ReportModule.VERDICT_IGNORE -> stringResource(Res.string.report_verdict_ignore)
    else -> verdict
}

/** The label of a content action declared by [targetType]; its id if this version doesn't know it. */
@Composable
fun contentActionLabel(action: String, targetType: String): String =
    ReportTargetType.of(targetType)?.actions?.get(action)?.let { stringResource(it.label) } ?: action

@Composable
fun contentActionDescription(action: String, targetType: String): String? =
    ReportTargetType.of(targetType)?.actions?.get(action)?.description?.let { stringResource(it) }

@Composable
fun verdictDescription(verdict: String): String? = when (verdict) {
    ReportModule.VERDICT_AGREE -> stringResource(Res.string.report_verdict_agree_desc)
    ReportModule.VERDICT_DISAGREE -> stringResource(Res.string.report_verdict_disagree_desc)
    ReportModule.VERDICT_IGNORE -> stringResource(Res.string.report_verdict_ignore_desc)
    else -> null
}
