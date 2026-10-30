package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_cancel
import hachimiworld.composeapp.generated.resources.common_delete
import hachimiworld.composeapp.generated.resources.common_more
import hachimiworld.composeapp.generated.resources.comment_back_cd
import hachimiworld.composeapp.generated.resources.comment_cancel_reply_cd
import hachimiworld.composeapp.generated.resources.comment_delete_confirm_text
import hachimiworld.composeapp.generated.resources.comment_delete_confirm_title
import hachimiworld.composeapp.generated.resources.comment_empty_subtitle
import hachimiworld.composeapp.generated.resources.comment_empty_title
import hachimiworld.composeapp.generated.resources.comment_header
import hachimiworld.composeapp.generated.resources.comment_input_placeholder
import hachimiworld.composeapp.generated.resources.comment_like_cd
import hachimiworld.composeapp.generated.resources.comment_load_error
import hachimiworld.composeapp.generated.resources.comment_load_more_failed
import hachimiworld.composeapp.generated.resources.comment_removed
import hachimiworld.composeapp.generated.resources.comment_replies_header
import hachimiworld.composeapp.generated.resources.comment_reply
import hachimiworld.composeapp.generated.resources.comment_reply_to_inline
import hachimiworld.composeapp.generated.resources.comment_replying_to
import hachimiworld.composeapp.generated.resources.comment_send_cd
import hachimiworld.composeapp.generated.resources.comment_sort_hot
import hachimiworld.composeapp.generated.resources.comment_sort_new
import hachimiworld.composeapp.generated.resources.comment_unlike_cd
import hachimiworld.composeapp.generated.resources.comment_view_all_replies
import hachimiworld.composeapp.generated.resources.notification_retry
import hachimiworld.composeapp.generated.resources.report_action
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import world.hachimi.app.api.module.CommentModule
import world.hachimi.app.api.module.CommentModule.CommentItem
import world.hachimi.app.api.module.CommentModule.CommentThread
import world.hachimi.app.api.module.ReportModule
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.ReportStore
import world.hachimi.app.model.SongCommentsStore
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.DropdownMenu
import world.hachimi.app.ui.design.components.DropdownMenuItem
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.design.components.ToggleButton
import world.hachimi.app.ui.message.components.RemoteImage
import world.hachimi.app.ui.notification.components.NotificationMessageState
import world.hachimi.app.ui.notification.components.RetryLine
import world.hachimi.app.ui.util.fadingEdges
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime

/**
 * The comments tab of the player: the song's comments, the replies under one of them once it's
 * opened, and the box to write in.
 *
 * @param songId the song the comments are on; null while it loads.
 * @param uploaderUid who uploaded the song, who can delete any comment under it.
 */
@Composable
fun CommentsTabContent(
    songId: Long?,
    uploaderUid: Long?,
    onUserClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    store: SongCommentsStore = koinInject(),
    reports: ReportStore = koinInject(),
) {
    LaunchedEffect(songId, uploaderUid) {
        if (songId != null && uploaderUid != null) store.bind(songId, uploaderUid)
    }
    var toDelete by remember { mutableStateOf<CommentItem?>(null) }
    val actions = CommentActions(
        onLike = store::toggleLike,
        onReply = store::startReply,
        onDelete = { toDelete = it },
        onReport = { reports.open(ReportModule.TARGET_COMMENT, it.id) },
        onUserClick = onUserClick,
        canDelete = store::canDelete,
        isMine = store::isMine,
    )

    Column(modifier) {
        val thread = store.openThread
        if (thread == null) {
            ListHeader(
                total = store.total,
                sort = store.sort,
                onSortChange = store::changeSort,
            )
        } else {
            RepliesHeader(total = store.repliesTotal, onBack = store::closeThread)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (songId == null || store.songId != songId) {
                LoadingBox()
            } else if (thread == null) {
                CommentList(store, actions, contentPadding)
            } else {
                ReplyList(store, thread, actions, contentPadding)
            }
        }
        InputBar(
            value = store.input,
            onValueChange = { store.input = it.take(CommentModule.CONTENT_MAX_CHARS) },
            replyTo = store.replyTo,
            onCancelReply = store::cancelReply,
            sending = store.sending,
            onSend = store::send,
        )
    }

    toDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text(stringResource(Res.string.comment_delete_confirm_title)) },
            text = { Text(stringResource(Res.string.comment_delete_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    store.delete(item)
                    toDelete = null
                }) { Text(stringResource(Res.string.common_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text(stringResource(Res.string.common_cancel)) }
            },
        )
    }
}

private class CommentActions(
    val onLike: (CommentItem) -> Unit,
    val onReply: (CommentItem) -> Unit,
    val onDelete: (CommentItem) -> Unit,
    val onReport: (CommentItem) -> Unit,
    val onUserClick: (Long) -> Unit,
    val canDelete: (CommentItem) -> Boolean,
    val isMine: (CommentItem) -> Boolean,
)

@Composable
private fun ListHeader(total: Long, sort: String, onSortChange: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(Res.string.comment_header, total.toString()),
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
        SortButton(sort == CommentModule.SORT_HOT, { onSortChange(CommentModule.SORT_HOT) }, stringResource(Res.string.comment_sort_hot))
        Spacer(Modifier.width(4.dp))
        SortButton(sort == CommentModule.SORT_NEW, { onSortChange(CommentModule.SORT_NEW) }, stringResource(Res.string.comment_sort_new))
    }
}

@Composable
private fun SortButton(selected: Boolean, onClick: () -> Unit, label: String) {
    ToggleButton(selected = selected, onClick = onClick) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 13.sp)
    }
}

@Composable
private fun RepliesHeader(total: Long, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        HachimiIconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.comment_back_cd))
        }
        Text(
            text = stringResource(Res.string.comment_replies_header, total.toString()),
            modifier = Modifier.padding(start = 8.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun LoadingBox() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LoadMoreItem(hasMore = true, isLoading = true)
    }
}

@Composable
private fun CommentList(store: SongCommentsStore, actions: CommentActions, contentPadding: PaddingValues) {
    when (store.initializeStatus) {
        InitializeStatus.INIT -> LoadingBox()
        InitializeStatus.FAILED -> NotificationMessageState(
            title = stringResource(Res.string.comment_load_error),
            modifier = Modifier.fillMaxSize(),
            icon = Icons.AutoMirrored.Outlined.Comment,
            action = {
                AccentButton(onClick = store::retry) { Text(stringResource(Res.string.notification_retry)) }
            },
        )
        InitializeStatus.LOADED -> if (store.threads.isEmpty()) {
            NotificationMessageState(
                title = stringResource(Res.string.comment_empty_title),
                subtitle = stringResource(Res.string.comment_empty_subtitle),
                modifier = Modifier.fillMaxSize(),
                icon = Icons.AutoMirrored.Outlined.Comment,
            )
        } else {
            val listState = rememberLazyListState()
            LaunchedEffect(listState.canScrollForward, store.hasMore, store.loadingMore, store.loadMoreFailed) {
                if (!listState.canScrollForward && store.hasMore && !store.loadingMore && !store.loadMoreFailed) {
                    store.loadMore()
                }
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().fadingEdges(16.dp, 16.dp),
                contentPadding = contentPadding,
            ) {
                items(store.threads, key = { it.comment.id }) { thread ->
                    ThreadItem(thread, actions, onOpenReplies = { store.openReplies(thread) })
                }
                item(key = "tail") {
                    when {
                        store.loadMoreFailed -> RetryLine(
                            stringResource(Res.string.comment_load_more_failed),
                            onRetry = store::loadMore,
                        )
                        store.hasMore -> LoadMoreItem(hasMore = true, isLoading = store.loadingMore)
                    }
                }
            }
        }
    }
}

@Composable
private fun ThreadItem(thread: CommentThread, actions: CommentActions, onOpenReplies: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        CommentRow(thread.comment, actions, avatarSize = 36)
        if (thread.replies.isNotEmpty() || thread.replyCount > 0) {
            Column(
                Modifier
                    .padding(start = 48.dp, top = 6.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(HachimiTheme.colorScheme.onSurface.copy(0.05f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                thread.replies.forEach { reply ->
                    CommentRow(reply, actions, avatarSize = 24, modifier = Modifier.padding(vertical = 4.dp))
                }
                if (thread.replyCount > thread.replies.size) {
                    Text(
                        text = stringResource(Res.string.comment_view_all_replies, thread.replyCount.toString()),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onOpenReplies)
                            .padding(vertical = 6.dp),
                        fontSize = 13.sp,
                        color = HachimiTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReplyList(store: SongCommentsStore, thread: CommentThread, actions: CommentActions, contentPadding: PaddingValues) {
    val listState = rememberLazyListState()
    LaunchedEffect(listState.canScrollForward, store.repliesHasMore, store.repliesLoadingMore) {
        if (!listState.canScrollForward && store.repliesHasMore && !store.repliesLoadingMore) store.loadMoreReplies()
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().fadingEdges(16.dp, 16.dp),
        contentPadding = contentPadding,
    ) {
        item(key = "root") {
            CommentRow(thread.comment, actions, avatarSize = 36, modifier = Modifier.padding(vertical = 10.dp))
            Box(
                Modifier.fillMaxWidth().padding(vertical = 4.dp).height(1.dp)
                    .background(HachimiTheme.colorScheme.outline)
            )
        }
        replyItems(store, actions)
    }
}

private fun LazyListScope.replyItems(store: SongCommentsStore, actions: CommentActions) {
    when (store.repliesStatus) {
        InitializeStatus.INIT -> item(key = "loading") { LoadMoreItem(hasMore = true, isLoading = true) }
        InitializeStatus.FAILED -> item(key = "failed") {
            RetryLine(stringResource(Res.string.comment_load_error), onRetry = store::retryReplies)
        }
        InitializeStatus.LOADED -> {
            items(store.replies, key = { it.id }) { reply ->
                CommentRow(reply, actions, avatarSize = 28, modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp))
            }
            if (store.repliesHasMore) item(key = "tail") {
                LoadMoreItem(hasMore = true, isLoading = store.repliesLoadingMore)
            }
        }
    }
}

@Composable
private fun CommentRow(item: CommentItem, actions: CommentActions, avatarSize: Int, modifier: Modifier = Modifier) {
    val variant = HachimiTheme.colorScheme.onSurfaceVariant
    if (item.removed) {
        Text(
            text = stringResource(Res.string.comment_removed),
            modifier = modifier.padding(vertical = 4.dp),
            fontSize = 14.sp,
            color = variant,
        )
        return
    }
    val author = item.author ?: return
    Row(modifier.fillMaxWidth()) {
        RemoteImage(
            author.avatarUrl,
            Modifier.size(avatarSize.dp).clip(CircleShape).clickable { actions.onUserClick(author.uid) },
        )
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = author.username,
                    modifier = Modifier.weight(1f, fill = false).clickable { actions.onUserClick(author.uid) },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = variant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val replyTo = item.replyToUser
            val prefix = replyTo?.let { stringResource(Res.string.comment_reply_to_inline, it.username) }
            Text(
                text = buildAnnotatedString {
                    if (prefix != null) withStyle(SpanStyle(color = variant)) { append(prefix) }
                    append(item.content)
                },
                modifier = Modifier.padding(top = 2.dp),
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatTime(item.createTime, distance = true, precise = false, fullFormat = LocalDateTime.Formats.YMD),
                    fontSize = 12.sp,
                    color = variant,
                )
                Text(
                    text = stringResource(Res.string.comment_reply),
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { actions.onReply(item) }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    fontSize = 12.sp,
                    color = variant,
                )
                Spacer(Modifier.weight(1f))
                LikeButton(item, onClick = { actions.onLike(item) })
                MoreButton(item, actions)
            }
        }
    }
}

@Composable
private fun LikeButton(item: CommentItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = if (item.liked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
            contentDescription = stringResource(if (item.liked) Res.string.comment_unlike_cd else Res.string.comment_like_cd),
            modifier = Modifier.size(16.dp),
            tint = if (item.liked) HachimiTheme.colorScheme.primary else HachimiTheme.colorScheme.onSurfaceVariant,
        )
        if (item.likeCount > 0) {
            Text(
                text = item.likeCount.toString(),
                fontSize = 12.sp,
                color = if (item.liked) HachimiTheme.colorScheme.primary else HachimiTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MoreButton(item: CommentItem, actions: CommentActions) {
    val canDelete = actions.canDelete(item)
    val canReport = !actions.isMine(item)
    if (!canDelete && !canReport) return
    Box {
        var expanded by remember { mutableStateOf(false) }
        HachimiIconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = stringResource(Res.string.common_more),
                modifier = Modifier.size(16.dp),
                tint = HachimiTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            if (canDelete) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        actions.onDelete(item)
                    },
                    text = { Text(stringResource(Res.string.common_delete)) },
                    leadingIcon = { Icon(Icons.Outlined.Delete, null) },
                )
            }
            if (canReport) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        actions.onReport(item)
                    },
                    text = { Text(stringResource(Res.string.report_action)) },
                    leadingIcon = { Icon(Icons.Outlined.Flag, null) },
                )
            }
        }
    }
}

@Composable
private fun InputBar(
    value: String,
    onValueChange: (String) -> Unit,
    replyTo: CommentItem?,
    onCancelReply: () -> Unit,
    sending: Boolean,
    onSend: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        if (replyTo != null) {
            Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.comment_replying_to, replyTo.author?.username.orEmpty()),
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    color = HachimiTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                HachimiIconButton(onClick = onCancelReply) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(Res.string.comment_cancel_reply_cd),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(Res.string.comment_input_placeholder)) },
                maxLines = 4,
                enabled = !sending,
            )
            HachimiIconButton(
                onClick = onSend,
                modifier = Modifier.padding(start = 8.dp),
                enabled = value.isNotBlank() && !sending,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(Res.string.comment_send_cd),
                    tint = if (value.isNotBlank()) HachimiTheme.colorScheme.primary else LocalContentColor.current,
                )
            }
        }
    }
}
