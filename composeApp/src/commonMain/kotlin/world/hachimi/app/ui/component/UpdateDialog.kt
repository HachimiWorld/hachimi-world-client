package world.hachimi.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import world.hachimi.app.api.module.VersionModule
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import kotlin.time.Instant

/**
 * Changelog dialog for an update, opened from the update notice or a manual check.
 *
 * @param subtitle gray lines under the title, e.g. the current version and package size
 * @param versions versions to show the changelog of, newest first
 * @param onDismissRequest closing the dialog without pressing a button, defaults to [onDismiss]
 */
@Composable
fun UpdateDialog(
    title: String,
    subtitle: List<String>,
    versions: List<VersionModule.LatestVersionResp>,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String,
    onDismiss: () -> Unit,
    onDismissRequest: () -> Unit = onDismiss,
) {
    AlertDialog(
        modifier = Modifier.width(380.dp),
        onDismissRequest = onDismissRequest,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium)
                subtitle.forEach {
                    Text(it, fontSize = 13.sp, lineHeight = 18.sp, color = HachimiTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = { Changelogs(versions) },
        dismissButton = {
            Button(
                onClick = onDismiss,
                color = Color.Transparent,
                contentColor = HachimiTheme.colorScheme.onSurfaceVariant,
            ) {
                Text(dismissText)
            }
        },
        confirmButton = {
            AccentButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
    )
}

@Composable
private fun Changelogs(versions: List<VersionModule.LatestVersionResp>) {
    Column(
        modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        versions.forEach { version ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // A single version is already named in the title
                if (versions.size > 1) {
                    val date = remember(version.releaseTime) {
                        version.releaseTime.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                    }
                    Text(
                        "${version.versionName} · $date",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = HachimiTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val lines = remember(version.changelog) { parseChangelog(version.changelog) }
                lines.forEach { ChangelogLineView(it) }
            }
        }
    }
}

@Composable
private fun ChangelogLineView(line: ChangelogLine) {
    when (line) {
        is ChangelogLine.Section -> Text(
            line.text,
            modifier = Modifier.padding(top = 4.dp),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
        )

        is ChangelogLine.Item -> Row {
            Box(Modifier.padding(top = 9.dp, end = 10.dp).size(4.dp).background(HachimiTheme.colorScheme.primary, CircleShape))
            Text(line.text, fontSize = 14.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal)
        }

        is ChangelogLine.Paragraph -> Text(line.text, fontSize = 14.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal)
    }
}

internal sealed interface ChangelogLine {
    val text: String

    data class Section(override val text: String) : ChangelogLine
    data class Item(override val text: String) : ChangelogLine
    data class Paragraph(override val text: String) : ChangelogLine
}

private val headingPrefix = Regex("""^#{1,6}\s+""")
private val listItemPrefix = Regex("""^(\d+[.)]|[-*•])\s+""")

/**
 * Parses a changelog in the CHANGELOG.md format: `## Section` headings and numbered or bulleted items.
 * Anything else, including malformed markdown, is kept as a plain paragraph, so nothing is lost.
 */
internal fun parseChangelog(markdown: String): List<ChangelogLine> =
    markdown.lines().map { it.trim() }.filter { it.isNotEmpty() }.map { line ->
        when {
            headingPrefix.containsMatchIn(line) -> ChangelogLine.Section(line.replaceFirst(headingPrefix, ""))
            listItemPrefix.containsMatchIn(line) -> ChangelogLine.Item(line.replaceFirst(listItemPrefix, ""))
            else -> ChangelogLine.Paragraph(line)
        }
    }

private val previewChangelog = """
    ## Features 新功能

    1. Support automatic updates. 支持自动更新。
    2. Web: suggest downloading the native client. Web 端新增下载客户端的提示。

    ## Fixes 修复

    1. Fixed charsets issues in changelog. 修复更新日志的字符集问题。
""".trimIndent()

@Preview
@Composable
private fun PreviewUpdateDialog() {
    PreviewTheme(background = false) {
        UpdateDialog(
            title = "1.4.0 已就绪",
            subtitle = listOf("当前版本 1.3.0"),
            versions = listOf(
                VersionModule.LatestVersionResp(
                    versionName = "1.4.0",
                    versionNumber = 2,
                    changelog = previewChangelog,
                    variant = "",
                    url = "",
                    releaseTime = Instant.fromEpochMilliseconds(0),
                )
            ),
            confirmText = "立即安装",
            onConfirm = {},
            dismissText = "稍后",
            onDismiss = {},
        )
    }
}
