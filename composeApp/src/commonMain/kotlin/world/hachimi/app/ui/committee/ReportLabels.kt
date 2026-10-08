package world.hachimi.app.ui.committee

import androidx.compose.runtime.Composable
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.report_action_hide
import hachimiworld.composeapp.generated.resources.report_action_hide_playlist_desc
import hachimiworld.composeapp.generated.resources.report_action_hide_song_desc
import hachimiworld.composeapp.generated.resources.report_action_reset_avatar
import hachimiworld.composeapp.generated.resources.report_action_reset_bio
import hachimiworld.composeapp.generated.resources.report_action_reset_username
import hachimiworld.composeapp.generated.resources.report_action_reset_username_desc
import hachimiworld.composeapp.generated.resources.report_action_restore
import hachimiworld.composeapp.generated.resources.report_verdict_agree_desc
import hachimiworld.composeapp.generated.resources.report_type_playlist
import hachimiworld.composeapp.generated.resources.report_type_song
import hachimiworld.composeapp.generated.resources.report_type_user
import hachimiworld.composeapp.generated.resources.report_verdict_agree
import hachimiworld.composeapp.generated.resources.report_verdict_disagree
import hachimiworld.composeapp.generated.resources.report_verdict_disagree_desc
import hachimiworld.composeapp.generated.resources.report_verdict_ignore
import hachimiworld.composeapp.generated.resources.report_verdict_ignore_desc
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.module.ReportModule

@Composable
fun targetTypeLabel(type: String): String = when (type) {
    ReportModule.TARGET_SONG -> stringResource(Res.string.report_type_song)
    ReportModule.TARGET_PLAYLIST -> stringResource(Res.string.report_type_playlist)
    ReportModule.TARGET_USER -> stringResource(Res.string.report_type_user)
    else -> type
}

@Composable
fun verdictLabel(verdict: String): String = when (verdict) {
    ReportModule.VERDICT_AGREE -> stringResource(Res.string.report_verdict_agree)
    ReportModule.VERDICT_DISAGREE -> stringResource(Res.string.report_verdict_disagree)
    ReportModule.VERDICT_IGNORE -> stringResource(Res.string.report_verdict_ignore)
    else -> verdict
}

@Composable
fun contentActionLabel(action: String): String = when (action) {
    ReportModule.ACTION_HIDE -> stringResource(Res.string.report_action_hide)
    ReportModule.ACTION_RESTORE -> stringResource(Res.string.report_action_restore)
    ReportModule.ACTION_RESET_AVATAR -> stringResource(Res.string.report_action_reset_avatar)
    ReportModule.ACTION_RESET_BIO -> stringResource(Res.string.report_action_reset_bio)
    ReportModule.ACTION_RESET_USERNAME -> stringResource(Res.string.report_action_reset_username)
    else -> action
}

@Composable
fun contentActionDescription(action: String, targetType: String): String? = when (action) {
    ReportModule.ACTION_HIDE -> stringResource(
        if (targetType == ReportModule.TARGET_SONG) Res.string.report_action_hide_song_desc
        else Res.string.report_action_hide_playlist_desc
    )
    ReportModule.ACTION_RESET_USERNAME -> stringResource(Res.string.report_action_reset_username_desc)
    else -> null
}

@Composable
fun verdictDescription(verdict: String): String? = when (verdict) {
    ReportModule.VERDICT_AGREE -> stringResource(Res.string.report_verdict_agree_desc)
    ReportModule.VERDICT_DISAGREE -> stringResource(Res.string.report_verdict_disagree_desc)
    ReportModule.VERDICT_IGNORE -> stringResource(Res.string.report_verdict_ignore_desc)
    else -> null
}
