package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.player_comments_cd
import hachimiworld.composeapp.generated.resources.player_like
import hachimiworld.composeapp.generated.resources.player_unlike
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.component.AnimatedCount
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.formatCompactCount

/**
 * Draws [count] at the top end of the button in [content], widening it to fit. No badge at zero
 * or while the count is unknown.
 *
 * @param overhang how far the badge may run past the end without widening, for buttons with empty
 * space around their icons that the next one can spare.
 * @since 261031
 */
@Composable
fun CountBadgeBox(
    count: Long?,
    modifier: Modifier = Modifier,
    overhang: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Layout(
        content = {
            content()
            if (count != null && count > 0) CountBadge(count)
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val button = measurables[0].measure(constraints)
        val badge = measurables.getOrNull(1)?.measure(Constraints())
        val badgeX = button.width / 2 + 11.dp.roundToPx()
        val width = if (badge != null) maxOf(button.width, badgeX + badge.width - overhang.roundToPx()) else button.width
        layout(width, button.height) {
            button.place(0, 0)
            badge?.place(badgeX, button.height / 2 - 15.dp.roundToPx())
        }
    }
}

@Composable
private fun CountBadge(count: Long) {
    AnimatedCount(count) {
        Text(
            text = formatCompactCount(it),
            color = LocalContentColor.current.copy(0.7f),
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * The like button and the way to the comments, side by side, for the compact player.
 *
 * @param likeCount null hides its badge, as does [commentCount].
 * @since 261031
 */
@Composable
fun LikeAndCommentButtons(
    liked: Boolean,
    likeEnabled: Boolean,
    likeCount: Long?,
    commentCount: Long?,
    onLikeClick: () -> Unit,
    onCommentsClick: () -> Unit,
    commentsEnabled: Boolean = true,
    touchMode: Boolean = false,
) {
    val overhang = if (touchMode) 10.dp else 0.dp
    CountBadgeBox(likeCount, overhang = overhang) {
        HachimiIconButton(onClick = onLikeClick, touchMode = touchMode, enabled = likeEnabled) {
            Icon(
                imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(
                    if (liked) Res.string.player_unlike else Res.string.player_like
                ),
                tint = if (liked) HachimiTheme.colorScheme.primary else LocalContentColor.current,
            )
        }
    }
    CountBadgeBox(commentCount, overhang = overhang) {
        HachimiIconButton(onClick = onCommentsClick, touchMode = touchMode, enabled = commentsEnabled) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Comment,
                contentDescription = stringResource(Res.string.player_comments_cd),
            )
        }
    }
}

@Preview(name = "Counts")
@Composable
private fun LikeAndCommentButtonsPreview() {
    PreviewTheme(background = true) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LikeAndCommentButtons(true, true, 128, 8, {}, {}, touchMode = true)
        }
    }
}

@Preview(name = "Large counts")
@Composable
private fun LikeAndCommentButtonsLargePreview() {
    PreviewTheme(background = true) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LikeAndCommentButtons(false, true, 12_345, 1_203, {}, {}, touchMode = true)
        }
    }
}

@Preview(name = "No counts")
@Composable
private fun LikeAndCommentButtonsEmptyPreview() {
    PreviewTheme(background = true) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LikeAndCommentButtons(false, true, 0, null, {}, {}, touchMode = true)
        }
    }
}
