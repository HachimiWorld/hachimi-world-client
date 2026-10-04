package world.hachimi.app.ui.root.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InstallDesktop
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.update_installs_on_exit
import hachimiworld.composeapp.generated.resources.update_notice_available
import hachimiworld.composeapp.generated.resources.update_notice_details
import hachimiworld.composeapp.generated.resources.update_notice_dismiss_cd
import hachimiworld.composeapp.generated.resources.update_notice_download
import hachimiworld.composeapp.generated.resources.update_notice_downloading
import hachimiworld.composeapp.generated.resources.update_notice_failed
import hachimiworld.composeapp.generated.resources.update_notice_install
import hachimiworld.composeapp.generated.resources.update_notice_open
import hachimiworld.composeapp.generated.resources.update_notice_ready
import hachimiworld.composeapp.generated.resources.update_notice_retry
import hachimiworld.composeapp.generated.resources.update_notice_failed_hint
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.module.VersionModule
import world.hachimi.app.getPlatform
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.update.UpdateManager
import world.hachimi.app.update.UpdateManager.Notice
import world.hachimi.app.util.formatBytes
import kotlin.time.Instant

/*
 * Non-blocking update notice, shown in the same places as the Web client promo.
 *
 * Every state shares one trailing slot: download arrow, then a progress ring, then the install button (the only
 * filled, primary-colored element), or a retry arrow. Tapping the notice opens the changelog dialog.
 */

/** Expanded (desktop): a quiet row at the bottom of the side rail, above the account footer. */
@Composable
fun ExpandedUpdateCard(
    updates: UpdateManager,
    modifier: Modifier = Modifier,
) {
    val notice = updates.notice
    // Keep drawing the last notice while it animates out
    val shown = rememberLastNonNull(notice)
    AnimatedVisibility(
        visible = notice != null,
        modifier = modifier,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        if (shown != null) ExpandedUpdateRowContent(
            notice = shown,
            supportsInAppUpdate = updates.supportsInAppUpdate,
            installsOnExit = updates.installsOnExit,
            onAction = { updates.runAction(shown) },
            onDetails = updates::showDetails,
        )
    }
}

/** Compact (phone): a floating card under the top app bar. */
@Composable
fun CompactUpdateBanner(
    updates: UpdateManager,
    modifier: Modifier = Modifier,
) {
    val notice = updates.notice
    val shown = rememberLastNonNull(notice)
    AnimatedVisibility(
        visible = notice != null,
        modifier = modifier,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        if (shown != null) CompactUpdateBannerContent(
            notice = shown,
            supportsInAppUpdate = updates.supportsInAppUpdate,
            installsOnExit = updates.installsOnExit,
            onAction = { updates.runAction(shown) },
            onDetails = updates::showDetails,
            onDismiss = updates::dismissNotice,
        )
    }
}

private fun UpdateManager.runAction(notice: Notice) {
    if (notice is Notice.Ready) install() else download()
}

@Composable
private fun rememberLastNonNull(notice: Notice?): Notice? {
    val last = remember { arrayOfNulls<Notice>(1) }
    if (notice != null) last[0] = notice
    return notice ?: last[0]
}

@Composable
private fun ExpandedUpdateRowContent(
    notice: Notice,
    supportsInAppUpdate: Boolean,
    installsOnExit: Boolean,
    onAction: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().padding(top = 8.dp)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(HachimiTheme.colorScheme.outline))
        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(12.dp))
                .detailsClickable(notice, onDetails)
                .padding(start = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NoticeTexts(notice, supportsInAppUpdate, installsOnExit, Modifier.weight(1f))
            ActionSlot(notice, supportsInAppUpdate, onAction)
        }
    }
}

@Composable
private fun CompactUpdateBannerContent(
    notice: Notice,
    supportsInAppUpdate: Boolean,
    installsOnExit: Boolean,
    onAction: () -> Unit,
    onDetails: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 8.dp)
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(shape)
            .border(1.dp, HachimiTheme.colorScheme.outline, shape)
            .background(HachimiTheme.colorScheme.surface)
            .detailsClickable(notice, onDetails)
            .padding(start = 14.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NoticeTexts(notice, supportsInAppUpdate, installsOnExit, Modifier.weight(1f))
        ActionSlot(notice, supportsInAppUpdate, onAction)
        HachimiIconButton(onClick = onDismiss, touchMode = true) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(Res.string.update_notice_dismiss_cd),
                modifier = Modifier.size(16.dp),
                tint = HachimiTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** While downloading there is nothing to decide, so only the other states open the changelog. */
private fun Modifier.detailsClickable(notice: Notice, onDetails: () -> Unit): Modifier =
    if (notice is Notice.Downloading) this else clickable(onClick = onDetails)

@Composable
private fun NoticeTexts(notice: Notice, supportsInAppUpdate: Boolean, installsOnExit: Boolean, modifier: Modifier) {
    Column(modifier.padding(vertical = 8.dp)) {
        Text(
            text = notice.title(),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = notice.subtitle(supportsInAppUpdate, installsOnExit),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = HachimiTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** The trailing slot shared by every state, so switching states only changes this one spot. */
@Composable
private fun ActionSlot(notice: Notice, supportsInAppUpdate: Boolean, onAction: () -> Unit) {
    AnimatedContent(
        targetState = notice,
        contentKey = { it::class },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentAlignment = Alignment.Center,
    ) { target ->
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            when (target) {
                is Notice.Downloading -> DownloadRing(target.progress)
                is Notice.Ready -> InstallButton(onAction)
                is Notice.Available -> ActionIconButton(
                    icon = if (supportsInAppUpdate) Icons.Default.Download else Icons.AutoMirrored.Default.OpenInNew,
                    contentDescription = stringResource(
                        if (supportsInAppUpdate) Res.string.update_notice_download else Res.string.update_notice_open
                    ),
                    onClick = onAction,
                )

                is Notice.Failed -> ActionIconButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = stringResource(Res.string.update_notice_retry),
                    onClick = onAction,
                )
            }
        }
    }
}

@Composable
private fun DownloadRing(progress: Float?) {
    Box(contentAlignment = Alignment.Center) {
        val ringModifier = Modifier.size(22.dp)
        if (progress == null) {
            CircularProgressIndicator(ringModifier, color = HachimiTheme.colorScheme.primary, strokeWidth = 2.5.dp)
        } else {
            CircularProgressIndicator(
                modifier = ringModifier,
                color = HachimiTheme.colorScheme.primary,
                strokeWidth = 2.5.dp,
                trackColor = HachimiTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                // Without the gap and with the arrow inside, it reads as progress rather than a loading spinner
                gapSize = 0.dp,
                progress = { progress },
            )
        }
        Icon(
            Icons.Default.ArrowDownward,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = HachimiTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun InstallButton(onClick: () -> Unit) {
    val mobile = getPlatform().name.let { it == "Android" || it.startsWith("iOS") }
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(HachimiTheme.colorScheme.primary)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (mobile) Icons.Default.InstallMobile else Icons.Default.InstallDesktop,
            contentDescription = stringResource(Res.string.update_notice_install),
            modifier = Modifier.size(18.dp),
            tint = HachimiTheme.colorScheme.onSurfaceReverse,
        )
    }
}

@Composable
private fun ActionIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    HachimiIconButton(onClick = onClick, touchMode = true) {
        Icon(icon, contentDescription, Modifier.size(20.dp), tint = HachimiTheme.colorScheme.primary)
    }
}

@Composable
private fun Notice.title(): String = when (this) {
    is Notice.Available -> stringResource(Res.string.update_notice_available, version.versionName)
    is Notice.Downloading -> stringResource(Res.string.update_notice_downloading, version.versionName)
    is Notice.Ready -> stringResource(Res.string.update_notice_ready, version.versionName)
    is Notice.Failed -> stringResource(Res.string.update_notice_failed, version.versionName)
}

@Composable
private fun Notice.subtitle(supportsInAppUpdate: Boolean, installsOnExit: Boolean): String = when (this) {
    is Notice.Available -> version.size?.takeIf { supportsInAppUpdate }?.let(::formatBytes)
        ?: stringResource(Res.string.update_notice_details)

    is Notice.Downloading -> {
        val progress = progress
        if (progress != null && total != null) {
            // Kept short to fit the 180dp side rail
            "${(progress * 100).toInt()}% · ${formatBytes(total)}"
        } else {
            formatBytes(downloaded)
        }
    }

    is Notice.Ready -> stringResource(
        if (installsOnExit) Res.string.update_installs_on_exit else Res.string.update_notice_details
    )
    is Notice.Failed -> stringResource(Res.string.update_notice_failed_hint)
}

private val previewVersion = VersionModule.LatestVersionResp(
    versionName = "1.4.0",
    versionNumber = 999,
    changelog = "",
    variant = "release-windows",
    url = "",
    releaseTime = Instant.fromEpochMilliseconds(0),
    size = 121_684_192,
)

private val previewNotices = listOf(
    Notice.Available(previewVersion),
    Notice.Downloading(previewVersion, downloaded = 51_107_360, total = 121_684_192),
    Notice.Ready(previewVersion),
    Notice.Failed(previewVersion),
)

@Preview(widthDp = 196, name = "Expanded rail row")
@Composable
private fun PreviewExpandedUpdateRow() {
    PreviewTheme(background = true) {
        Column(Modifier.padding(8.dp)) {
            previewNotices.forEach {
                ExpandedUpdateRowContent(it, supportsInAppUpdate = true, installsOnExit = false, onAction = {}, onDetails = {})
            }
        }
    }
}

@Preview(widthDp = 390, name = "Compact banner")
@Composable
private fun PreviewCompactUpdateBanner() {
    PreviewTheme(background = true) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            previewNotices.forEach {
                CompactUpdateBannerContent(it, supportsInAppUpdate = true, installsOnExit = false, onAction = {}, onDetails = {}, onDismiss = {})
            }
        }
    }
}
