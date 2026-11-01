package world.hachimi.app.ui.message

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.comment_reply_to_inline
import hachimiworld.composeapp.generated.resources.message_channel_replies
import hachimiworld.composeapp.generated.resources.message_replies_empty_subtitle
import hachimiworld.composeapp.generated.resources.message_replies_empty_title
import hachimiworld.composeapp.generated.resources.message_reply_commented
import hachimiworld.composeapp.generated.resources.message_reply_commented_post
import hachimiworld.composeapp.generated.resources.message_reply_replied
import hachimiworld.composeapp.generated.resources.message_load_error
import hachimiworld.composeapp.generated.resources.message_load_more_failed
import hachimiworld.composeapp.generated.resources.notification_retry
import kotlinx.datetime.LocalDateTime
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.CommentModule
import world.hachimi.app.api.module.MessageModule
import world.hachimi.app.api.module.MessageModule.ReceivedReplyItem
import world.hachimi.app.api.module.UgcPreview
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.ReceivedRepliesViewModel
import world.hachimi.app.model.commentsRoute
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.message.components.RemoteImage
import world.hachimi.app.ui.message.components.cardItems
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import androidx.compose.material3.MaterialTheme
import world.hachimi.app.ui.message.components.unreadDot
import world.hachimi.app.ui.notification.components.NotificationListSkeleton
import world.hachimi.app.ui.notification.components.NotificationMessageState
import world.hachimi.app.ui.notification.components.RetryLine
import world.hachimi.app.ui.util.listTailPadding
import world.hachimi.app.ui.util.listTailSpacerItem
import world.hachimi.app.util.YMD
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.formatTime
import kotlin.time.Instant

@Composable
fun ReceivedRepliesScreen() {
    val navigator = LocalNavigator.current
    ScreenScaffold(
        title = { Text(stringResource(Res.string.message_channel_replies), maxLines = 1) },
        showBack = true,
        onBack = navigator::back,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            ReceivedRepliesContent(Modifier.widthIn(max = 720.dp).fillMaxWidth())
        }
    }
}

/**
 * Replies to the user's comments and comments on their songs and posts, without the toolbar, for
 * embedding. Opening it marks replies read; tapping one opens the comments it's among.
 */
@Composable
fun ReceivedRepliesContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = AdaptiveScreenMargin, top = AdaptiveScreenMargin, end = AdaptiveScreenMargin),
    vm: ReceivedRepliesViewModel = koinViewModel(),
) {
    val navigator = LocalNavigator.current
    LaunchedEffect(vm) { vm.mounted() }

    when (vm.initializeStatus) {
        InitializeStatus.INIT -> NotificationListSkeleton(modifier)
        InitializeStatus.FAILED -> NotificationMessageState(
            title = stringResource(Res.string.message_load_error),
            modifier = modifier.listTailPadding(),
            icon = Icons.AutoMirrored.Outlined.Comment,
            action = {
                AccentButton(onClick = { vm.retry() }) {
                    Text(stringResource(Res.string.notification_retry))
                }
            },
        )
        InitializeStatus.LOADED -> if (vm.items.isEmpty()) {
            NotificationMessageState(
                title = stringResource(Res.string.message_replies_empty_title),
                subtitle = stringResource(Res.string.message_replies_empty_subtitle),
                modifier = modifier.listTailPadding(),
                icon = Icons.AutoMirrored.Outlined.Comment,
            )
        } else {
            val listState = rememberLazyListState()
            LaunchedEffect(listState.canScrollForward, vm.hasMore, vm.loadingMore, vm.loadMoreFailed) {
                if (!listState.canScrollForward && vm.hasMore && !vm.loadingMore && !vm.loadMoreFailed) {
                    vm.loadMore()
                }
            }
            LazyColumn(state = listState, modifier = modifier.fillMaxSize(), contentPadding = contentPadding) {
                cardItems(items = vm.items.toList(), key = { it.comment.id }) { item ->
                    ReceivedReplyRow(
                        item = item,
                        isNew = vm.isNew(item),
                        onClick = { item.target.preview.commentsRoute()?.let { navigator.push(it) } },
                    )
                }
                item(key = "tail") {
                    when {
                        vm.loadMoreFailed -> RetryLine(
                            stringResource(Res.string.message_load_more_failed),
                            onRetry = { vm.loadMore() },
                        )
                        vm.hasMore -> LoadMoreItem(hasMore = true, isLoading = vm.loadingMore)
                    }
                }
                listTailSpacerItem()
            }
        }
    }
}

@Composable
private fun ReceivedReplyRow(item: ReceivedReplyItem, isNew: Boolean, onClick: () -> Unit) {
    val comment = item.comment
    val variant = HachimiTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .unreadDot(isNew)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        RemoteImage(comment.author?.avatarUrl, Modifier.size(40.dp).clip(CircleShape))
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(comment.author?.username.orEmpty()) }
                        append(" ")
                        withStyle(SpanStyle(color = variant)) {
                            append(
                                stringResource(
                                    when {
                                        comment.rootId != null -> Res.string.message_reply_replied
                                        item.target.targetType == CommentModule.TARGET_POST -> Res.string.message_reply_commented_post
                                        else -> Res.string.message_reply_commented
                                    }
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatTime(comment.createTime, distance = true, precise = false, fullFormat = LocalDateTime.Formats.YMD),
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = variant,
                )
            }
            val prefix = comment.replyToUser?.let { stringResource(Res.string.comment_reply_to_inline, it.username) }
            Text(
                text = buildAnnotatedString {
                    if (prefix != null) withStyle(SpanStyle(color = variant)) { append(prefix) }
                    append(comment.content)
                },
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.repliedContent ?: "《${item.target.preview.headline}》",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = variant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        RemoteImage(item.target.preview.imageUrl, Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)))
    }
}

private fun previewReply(replyTo: CommentModule.CommentUser?, repliedContent: String?) = ReceivedReplyItem(
    comment = CommentModule.CommentItem(
        id = 7,
        targetType = CommentModule.TARGET_SONG,
        targetId = 144,
        rootId = if (replyTo != null) 1 else null,
        replyToId = if (replyTo != null) 1 else null,
        replyToUser = replyTo,
        author = CommentModule.CommentUser(2, "循环一下午", null),
        content = "半拍那里确实，第一次听差点以为是卡了。",
        removed = false,
        likeCount = 0,
        liked = false,
        createTime = Instant.parse("2026-10-31T03:01:34Z"),
    ),
    target = MessageModule.ReplyTarget(
        targetType = CommentModule.TARGET_SONG,
        targetId = 144,
        preview = UgcPreview.Song("曼波小队", "", "JM-CBRN-506"),
    ),
    repliedContent = repliedContent,
)

@Preview(widthDp = 360, name = "Reply to a comment · New")
@Composable
private fun ReceivedReplyPreview() {
    PreviewTheme(background = true) {
        ReceivedReplyRow(
            previewReply(CommentModule.CommentUser(1, "基米观察员", null), "同感，尤其是鼓进来之前那半拍。"),
            isNew = true,
            onClick = {},
        )
    }
}

@Preview(widthDp = 360, name = "Comment on a song")
@Composable
private fun ReceivedSongCommentPreview() {
    PreviewTheme(background = true) {
        ReceivedReplyRow(previewReply(null, null), isNew = false, onClick = {})
    }
}

@Preview(widthDp = 360, name = "Comment on a post")
@Composable
private fun ReceivedPostCommentPreview() {
    PreviewTheme(background = true) {
        ReceivedReplyRow(
            previewReply(null, null).copy(
                target = MessageModule.ReplyTarget(
                    targetType = CommentModule.TARGET_POST,
                    targetId = 12,
                    preview = UgcPreview.Post(12, "十月活动公告", null),
                ),
            ),
            isNew = false,
            onClick = {},
        )
    }
}
