package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.ui.util.AdaptiveScreenMargin

/**
 * A page's heading drawn in its own content: the [title] large, a quiet [caption] under it, and the
 * page's [action] (such as the fan of covers) at the end, level with the bottom of the two.
 *
 * Under a phone scaffold, which shows the title large itself, pass no [title] and no [action]
 * (the action goes by the title through [PageTitleAction]); what's left is the caption.
 *
 * @since 261114
 */
@Composable
fun PageHeading(
    title: String?,
    modifier: Modifier = Modifier,
    caption: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    if (title == null && caption == null && action == null) return
    Row(
        modifier.fillMaxWidth().padding(bottom = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.weight(1f)) {
            if (title != null) {
                Text(
                    title,
                    style = LargeTitleStyle.copy(color = HachimiTheme.colorScheme.onSurface),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (caption != null) {
                Text(
                    caption,
                    modifier = Modifier.padding(top = if (title != null) 4.dp else 0.dp),
                    style = PageCaptionStyle.copy(color = HachimiTheme.colorScheme.onSurfaceVariant),
                )
            }
        }
        action?.invoke()
    }
}

/**
 * Padding for a list that opens with a [PageHeading] under a toolbar: the page margin on the
 * sides and at the bottom, and only [PageHeadingTopGap] at the top, so the heading sits under the
 * bar as the large title does on phones. With no heading ([headingInPage] false) it's the margin
 * all round.
 *
 * @since 261114
 */
@Composable
fun headingListPadding(headingInPage: Boolean): PaddingValues {
    val margin = AdaptiveScreenMargin
    return PaddingValues(start = margin, end = margin, bottom = margin, top = if (headingInPage) PageHeadingTopGap else margin)
}

/**
 * Space over a [PageHeading] right under a toolbar.
 * @since 261114
 */
val PageHeadingTopGap = 8.dp

/**
 * The quiet line under a page title: counts, how the list is made.
 * @since 261114
 */
val PageCaptionStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp)

@Composable
private fun PreviewHeadings() {
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
        PageHeading(
            title = "每日推荐",
            caption = "并非根据你的口味进行推荐",
            action = { CoverFanPlaceholder() },
        )
        PageHeading(title = "最近发布", action = { CoverFanPlaceholder() })
        // Under a phone scaffold: the caption alone
        PageHeading(title = null, caption = "根据本周的播放量计算")
    }
}

@Preview(widthDp = 720, name = "Light")
@Composable
private fun PageHeadingLightPreview() {
    PreviewTheme(darkTheme = false, background = true) { PreviewHeadings() }
}

@Preview(widthDp = 720, name = "Dark")
@Composable
private fun PageHeadingDarkPreview() {
    PreviewTheme(darkTheme = true, background = true) { PreviewHeadings() }
}
