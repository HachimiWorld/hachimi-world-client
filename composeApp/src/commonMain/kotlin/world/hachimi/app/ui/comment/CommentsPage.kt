package world.hachimi.app.ui.comment

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_cancel
import hachimiworld.composeapp.generated.resources.common_delete
import hachimiworld.composeapp.generated.resources.common_more
import hachimiworld.composeapp.generated.resources.comment_cancel_reply_cd
import hachimiworld.composeapp.generated.resources.comment_delete_confirm_text
import hachimiworld.composeapp.generated.resources.comment_delete_confirm_title
import hachimiworld.composeapp.generated.resources.comment_empty_subtitle
import hachimiworld.composeapp.generated.resources.comment_empty_title
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
import hachimiworld.composeapp.generated.resources.comment_order_newest
import hachimiworld.composeapp.generated.resources.comment_order_oldest
import hachimiworld.composeapp.generated.resources.comment_section_all
import hachimiworld.composeapp.generated.resources.comment_section_hot
import hachimiworld.composeapp.generated.resources.comment_unlike_cd
import hachimiworld.composeapp.generated.resources.comment_view_all_replies
import hachimiworld.composeapp.generated.resources.notification_retry
import hachimiworld.composeapp.generated.resources.report_action
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance
import world.hachimi.app.api.module.CommentModule
import world.hachimi.app.api.module.CommentModule.CommentItem
import world.hachimi.app.api.module.CommentModule.CommentThread
import world.hachimi.app.api.module.CommentModule.CommentUser
import world.hachimi.app.api.module.ReportModule
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.ReportStore
import world.hachimi.app.model.CommentTarget
import world.hachimi.app.model.CommentsStore
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.component.AnimatedCount
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.DropdownMenu
import world.hachimi.app.ui.design.components.DropdownMenuItem
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.insets.AdjustResizeForIme
import world.hachimi.app.ui.message.components.RemoteImage
import world.hachimi.app.ui.notification.components.NotificationMessageState
import world.hachimi.app.ui.notification.components.RetryLine
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.fadingEdges
import world.hachimi.app.ui.util.listTailPadding
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime
import kotlin.time.Instant

/**
 * A target's comments as a page: [header] and the list scroll together, with the box to write in
 * below them. Back leaves the replies of a comment first.
 *
 * @param title The page's title over the list, given the number of comments.
 * @param header What the comments are on, above them.
 * @param startAtComments Opens scrolled past [header], for when it's long.
 * @since 261031
 */
@Composable
fun CommentsPage(
    target: CommentTarget,
    title: @Composable (total: Long) -> String,
    onBack: () -> Unit,
    header: (@Composable () -> Unit)? = null,
    startAtComments: Boolean = false,
    store: CommentsStore = koinInject(),
    reports: ReportStore = koinInject(),
) {
    val navigator = LocalNavigator.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    // Here so the place in the list, past a long header, survives opening a comment's replies
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = if (startAtComments && header != null) 1 else 0)
    val slideDistance = rememberSlideDistance()
    AdjustResizeForIme()
    LaunchedEffect(target) { store.open(target) }
    LaunchedEffect(store.replyTo) {
        if (store.replyTo != null) focusRequester.requestFocus()
    }
    var toDelete by remember { mutableStateOf<CommentItem?>(null) }
    val actions = CommentActions(
        onLike = store::toggleLike,
        onReply = store::startReply,
        onDelete = { toDelete = it },
        onReport = { reports.open(ReportModule.TARGET_COMMENT, it.id) },
        onUserClick = { navigator.push(Route.Root.PublicUserSpace(it)) },
        canDelete = store::canDelete,
        isMine = store::isMine,
    )
    val current = store.target == target
    val thread = store.openThread.takeIf { current }
    // Still there for the replies animating out
    val shownThread = rememberLastNonNull(thread)
    val repliesState = remember(shownThread?.comment?.id) { LazyListState() }
    LaunchedEffect(store) {
        store.sent.collect { item ->
            withFrameNanos {} // Lets the lists take it in
            if (item.rootId == null) {
                val hot = store.hotThreads.size
                val allHeader = (if (header != null) 1 else 0) + (if (hot > 0) hot + 1 else 0)
                when {
                    store.order == CommentModule.ORDER_NEWEST -> listState.animateScrollToItem(allHeader)
                    store.threads.lastOrNull()?.comment?.id == item.id -> listState.animateScrollToItem(allHeader + store.threads.size)
                }
            } else if (store.openThread?.comment?.id == item.rootId && store.replies.lastOrNull()?.id == item.id) {
                repliesState.animateScrollToItem(store.replies.size)
            }
        }
    }

    CommentsPageLayout(
        title = if (thread != null) {
            stringResource(Res.string.comment_replies_header, store.repliesTotal.toString())
        } else {
            title(if (current) store.total else 0)
        },
        onBack = { if (thread != null) store.closeThread() else onBack() },
        list = {
            if (!current) {
                CommentsLoadState(InitializeStatus.INIT, onRetry = {})
            } else {
                AnimatedContent(
                    targetState = thread?.comment?.id,
                    transitionSpec = { materialSharedAxisX(forward = targetState != null, slideDistance) },
                ) { openId ->
                    val replied = shownThread
                    if (openId == null || replied == null) {
                        CommentList(
                            header = header,
                            status = store.initializeStatus,
                            hotThreads = store.hotThreads,
                            threads = store.threads,
                            order = store.order,
                            onOrderChange = store::changeOrder,
                            hasMore = store.hasMore,
                            loadingMore = store.loadingMore,
                            loadMoreFailed = store.loadMoreFailed,
                            actions = actions,
                            onLoadMore = store::loadMore,
                            onRetry = store::retry,
                            onOpenReplies = store::openReplies,
                            listState = listState,
                        )
                    } else {
                        ReplyList(
                            thread = replied,
                            status = store.repliesStatus,
                            replies = store.replies,
                            hasMore = store.repliesHasMore,
                            loadingMore = store.repliesLoadingMore,
                            actions = actions,
                            onLoadMore = store::loadMoreReplies,
                            onRetry = store::retryReplies,
                            listState = repliesState,
                        )
                    }
                }
            }
        },
        composer = {
            CommentComposer(
                value = store.input,
                onValueChange = { store.input = it.take(CommentModule.CONTENT_MAX_CHARS) },
                replyTo = store.replyTo,
                onCancelReply = store::cancelReply,
                sending = store.sending,
                onSend = {
                    focusManager.clearFocus()
                    store.send()
                },
                focusRequester = focusRequester,
            )
        },
    )

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

/** The latest [value] that wasn't null, so what's animating out still has something to show. */
@Composable
private fun <T : Any> rememberLastNonNull(value: T?): T? {
    val last = remember { arrayOfNulls<Any>(1) }
    if (value != null) last[0] = value
    @Suppress("UNCHECKED_CAST")
    return last[0] as T?
}

/** A small pop whenever [key] changes, but not on first showing. */
@Composable
private fun Modifier.popOnChange(key: Any?): Modifier {
    val scale = remember { Animatable(1f) }
    val first = remember { booleanArrayOf(true) }
    LaunchedEffect(key) {
        if (first[0]) {
            first[0] = false
        } else {
            scale.snapTo(1f)
            scale.animateTo(1.3f, tween(90))
            scale.animateTo(1f, tween(150))
        }
    }
    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

/** The page's layout: the list, and below it the box to write in. */
@Composable
private fun CommentsPageLayout(
    title: String,
    onBack: () -> Unit,
    list: @Composable () -> Unit,
    composer: @Composable () -> Unit,
) {
    ScreenScaffold(
        title = {
            AnimatedContent(
                targetState = title,
                transitionSpec = {
                    fadeIn(tween(200)).togetherWith(fadeOut(tween(120))).using(SizeTransform(clip = false))
                },
                contentAlignment = Alignment.CenterStart,
            ) { Text(it, maxLines = 1) }
        },
        showBack = true,
        onBack = onBack,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            // The box to write in stays above the mini player and the keyboard
            Column(
                Modifier.widthIn(max = 720.dp)
                    .fillMaxSize()
                    .listTailPadding()
                    .imePadding()
            ) {
                Box(Modifier.weight(1f).fillMaxWidth()) { list() }
                Box(Modifier.padding(start = AdaptiveScreenMargin, top = 8.dp, end = AdaptiveScreenMargin, bottom = 12.dp)) {
                    composer()
                }
            }
        }
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

/** How all comments are sorted. It sits with them, since the top comments don't follow it. */
@Composable
private fun OrderTabs(order: String, onOrderChange: (String) -> Unit) {
    Row {
        OrderTab(order == CommentModule.ORDER_NEWEST, { onOrderChange(CommentModule.ORDER_NEWEST) }, stringResource(Res.string.comment_order_newest))
        OrderTab(order == CommentModule.ORDER_OLDEST, { onOrderChange(CommentModule.ORDER_OLDEST) }, stringResource(Res.string.comment_order_oldest))
    }
}

@Composable
private fun OrderTab(selected: Boolean, onClick: () -> Unit, label: String) {
    val color by animateColorAsState(if (selected) HachimiTheme.colorScheme.onSurface else HachimiTheme.colorScheme.onSurfaceVariant)
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
        color = color,
    )
}

@Composable
private fun SectionHeader(label: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp).heightIn(min = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
        trailing?.invoke()
    }
}

/**
 * Loading, failed to load, or no comments.
 *
 * @since 261031
 */
@Composable
internal fun CommentsLoadState(status: InitializeStatus, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Crossfade(status, modifier.fillMaxSize()) { shown ->
        when (shown) {
            InitializeStatus.INIT -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadMoreItem(hasMore = true, isLoading = true)
            }
            InitializeStatus.FAILED -> NotificationMessageState(
                title = stringResource(Res.string.comment_load_error),
                modifier = Modifier.fillMaxSize(),
                icon = Icons.AutoMirrored.Outlined.Comment,
                action = {
                    AccentButton(onClick = onRetry) { Text(stringResource(Res.string.notification_retry)) }
                },
            )
            InitializeStatus.LOADED -> NotificationMessageState(
                title = stringResource(Res.string.comment_empty_title),
                subtitle = stringResource(Res.string.comment_empty_subtitle),
                modifier = Modifier.fillMaxSize(),
                icon = Icons.AutoMirrored.Outlined.Comment,
            )
        }
    }
}

@Composable
private fun CommentList(
    header: (@Composable () -> Unit)?,
    status: InitializeStatus,
    hotThreads: List<CommentThread>,
    threads: List<CommentThread>,
    order: String,
    onOrderChange: (String) -> Unit,
    hasMore: Boolean,
    loadingMore: Boolean,
    loadMoreFailed: Boolean,
    actions: CommentActions,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onOpenReplies: (CommentThread) -> Unit,
    listState: LazyListState = rememberLazyListState(),
) {
    val margin = AdaptiveScreenMargin
    LaunchedEffect(listState.canScrollForward, hasMore, loadingMore, loadMoreFailed) {
        if (!listState.canScrollForward && hasMore && !loadingMore && !loadMoreFailed) onLoadMore()
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().fadingEdges(0.dp, 16.dp),
        contentPadding = PaddingValues(start = margin, top = 8.dp, end = margin, bottom = 16.dp),
    ) {
        if (header != null) item(key = "header") { header() }
        if (status != InitializeStatus.LOADED || threads.isEmpty()) {
            item(key = "state") { CommentsLoadState(status, onRetry, Modifier.animateItem().fillParentMaxHeight(0.6f)) }
            return@LazyColumn
        }
        if (hotThreads.isNotEmpty()) {
            item(key = "hot-header") { SectionHeader(stringResource(Res.string.comment_section_hot), Modifier.animateItem()) }
            // The same comment can be in both lists, so the keys differ
            items(hotThreads, key = { "hot-${it.comment.id}" }) { thread ->
                ThreadItem(thread, actions, onOpenReplies = { onOpenReplies(thread) }, Modifier.animateItem())
            }
        }
        item(key = "all-header") {
            SectionHeader(stringResource(Res.string.comment_section_all), Modifier.animateItem()) {
                OrderTabs(order, onOrderChange)
            }
        }
        items(threads, key = { it.comment.id }) { thread ->
            ThreadItem(thread, actions, onOpenReplies = { onOpenReplies(thread) }, Modifier.animateItem())
        }
        item(key = "tail") {
            when {
                loadMoreFailed -> RetryLine(
                    stringResource(Res.string.comment_load_more_failed),
                    onRetry = onLoadMore,
                )
                hasMore -> LoadMoreItem(hasMore = true, isLoading = loadingMore)
            }
        }
    }
}

@Composable
private fun ThreadItem(thread: CommentThread, actions: CommentActions, onOpenReplies: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().animateContentSize().padding(vertical = 10.dp)) {
        CommentRow(thread.comment, actions, avatarSize = 36)
        if (thread.replies.isNotEmpty() || thread.replyCount > 0) {
            Column(
                Modifier
                    .padding(start = 46.dp, top = 6.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HachimiTheme.colorScheme.onSurface.copy(0.04f))
                    .padding(start = 10.dp, top = 6.dp, end = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                thread.replies.forEach { reply ->
                    CommentRow(reply, actions, avatarSize = 22)
                }
                if (thread.replyCount > thread.replies.size) {
                    Text(
                        text = stringResource(Res.string.comment_view_all_replies, thread.replyCount.toString()),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onOpenReplies)
                            .padding(vertical = 4.dp),
                        fontSize = 13.sp,
                        color = HachimiTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReplyList(
    thread: CommentThread,
    status: InitializeStatus,
    replies: List<CommentItem>,
    hasMore: Boolean,
    loadingMore: Boolean,
    actions: CommentActions,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    listState: LazyListState = rememberLazyListState(),
) {
    LaunchedEffect(listState.canScrollForward, hasMore, loadingMore) {
        if (!listState.canScrollForward && hasMore && !loadingMore) onLoadMore()
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().fadingEdges(0.dp, 16.dp),
        contentPadding = PaddingValues(start = AdaptiveScreenMargin, top = 8.dp, end = AdaptiveScreenMargin, bottom = 16.dp),
    ) {
        item(key = "root") {
            CommentRow(thread.comment, actions, avatarSize = 36, modifier = Modifier.padding(vertical = 8.dp))
            Box(
                Modifier.fillMaxWidth().padding(vertical = 6.dp).height(1.dp)
                    .background(HachimiTheme.colorScheme.outline)
            )
        }
        when (status) {
            InitializeStatus.INIT -> item(key = "loading") { LoadMoreItem(hasMore = true, isLoading = true) }
            InitializeStatus.FAILED -> item(key = "failed") {
                RetryLine(stringResource(Res.string.comment_load_error), onRetry = onRetry)
            }
            InitializeStatus.LOADED -> {
                items(replies, key = { it.id }) { reply ->
                    CommentRow(reply, actions, avatarSize = 28, modifier = Modifier.animateItem().padding(top = 8.dp, bottom = 8.dp))
                }
                if (hasMore) item(key = "tail") {
                    LoadMoreItem(hasMore = true, isLoading = loadingMore)
                }
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
                Text(
                    text = formatTime(item.createTime, distance = true, precise = false, fullFormat = LocalDateTime.Formats.YMD),
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 12.sp,
                    color = variant.copy(alpha = variant.alpha * 0.75f),
                    maxLines = 1,
                )
            }
            val replyTo = item.replyToUser
            val prefix = replyTo?.let { stringResource(Res.string.comment_reply_to_inline, it.username) }
            Text(
                text = buildAnnotatedString {
                    if (prefix != null) withStyle(SpanStyle(color = variant)) { append(prefix) }
                    append(item.content)
                },
                modifier = Modifier.padding(top = 3.dp),
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.comment_reply),
                    modifier = Modifier
                        .offset(x = (-4).dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { actions.onReply(item) }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
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
    val color by animateColorAsState(if (item.liked) HachimiTheme.colorScheme.primary else HachimiTheme.colorScheme.onSurfaceVariant)
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
            modifier = Modifier.size(16.dp).popOnChange(item.liked),
            tint = color,
        )
        AnimatedVisibility(
            visible = item.likeCount > 0,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            // Stays at 1 while it animates out at 0
            AnimatedCount(item.likeCount.coerceAtLeast(1).toLong()) {
                Text(text = it.toString(), fontSize = 12.sp, color = color)
            }
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

/**
 * The box to write in. Its edge marks it off from the list, and everything to do with writing goes
 * inside it: [tools] (such as emoji or attachments) sit at the start of the row, before the text.
 */
@Composable
private fun CommentComposer(
    value: String,
    onValueChange: (String) -> Unit,
    replyTo: CommentItem?,
    onCancelReply: () -> Unit,
    sending: Boolean,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    tools: @Composable RowScope.() -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val onSurface = HachimiTheme.colorScheme.onSurface
    val shape = RoundedCornerShape(16.dp)
    val canSend = value.isNotBlank() && !sending
    val borderColor by animateColorAsState(if (focused) onSurface.copy(0.3f) else HachimiTheme.colorScheme.outline)
    val sendBackground by animateColorAsState(if (canSend) HachimiTheme.colorScheme.primary else Color.Transparent)
    val sendTint by animateColorAsState(if (canSend) HachimiTheme.colorScheme.onSurfaceReverse else HachimiTheme.colorScheme.onSurfaceVariant)
    val shownReplyTo = rememberLastNonNull(replyTo)

    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(onSurface.copy(0.05f))
            .border(1.dp, borderColor, shape)
    ) {
        AnimatedVisibility(
            visible = replyTo != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(start = 14.dp, top = 6.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.comment_replying_to, shownReplyTo?.author?.username.orEmpty()),
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    color = HachimiTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                HachimiIconButton(onClick = onCancelReply) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(Res.string.comment_cancel_reply_cd),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().animateContentSize(tween(150)).padding(start = 6.dp, top = 6.dp, end = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            tools()
            Box(Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 7.dp)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    enabled = !sending,
                    textStyle = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, color = onSurface),
                    maxLines = 5,
                    interactionSource = interactionSource,
                    cursorBrush = SolidColor(HachimiTheme.colorScheme.primary),
                )
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.comment_input_placeholder),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = HachimiTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val remaining = CommentModule.CONTENT_MAX_CHARS - value.length
                AnimatedVisibility(
                    visible = remaining <= CHARS_LEFT_SHOWN_AT,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Text(
                        text = remaining.toString(),
                        modifier = Modifier.padding(bottom = 4.dp),
                        fontSize = 11.sp,
                        color = HachimiTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(sendBackground)
                        .clickable(enabled = canSend, onClick = onSend),
                    contentAlignment = Alignment.Center,
                ) {
                    Crossfade(sending) { isSending ->
                        if (isSending) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(Res.string.comment_send_cd),
                                modifier = Modifier.size(18.dp),
                                tint = sendTint,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** How close to the limit the count of characters left appears. */
private const val CHARS_LEFT_SHOWN_AT = 50

private val PreviewUserA = CommentUser(1, "基米观察员", null)
private val PreviewUserB = CommentUser(2, "循环一下午", null)
private val PreviewUserC = CommentUser(3, "耳机党", null)

private val PreviewActions = CommentActions(
    onLike = {},
    onReply = {},
    onDelete = {},
    onReport = {},
    onUserClick = {},
    canDelete = { it.author == PreviewUserA },
    isMine = { it.author == PreviewUserA },
)

private fun previewComment(
    id: Long,
    author: CommentUser,
    content: String,
    rootId: Long? = null,
    replyTo: CommentUser? = null,
    likes: Int = 0,
    liked: Boolean = false,
) = CommentItem(
    id = id,
    targetType = CommentModule.TARGET_SONG,
    targetId = 144,
    rootId = rootId,
    replyToId = rootId,
    replyToUser = replyTo,
    author = author,
    content = content,
    removed = false,
    likeCount = likes,
    liked = liked,
    createTime = Instant.parse("2026-10-31T03:01:34Z"),
)

private val PreviewHotThread = CommentThread(
    comment = previewComment(1, PreviewUserA, "副歌进来那一下，还是没忍住又听了一遍。", likes = 128, liked = true),
    replies = listOf(
        previewComment(6, PreviewUserB, "同感，尤其是鼓进来之前那半拍。", rootId = 1),
        previewComment(7, PreviewUserC, "半拍那里确实，第一次听差点以为是卡了。", rootId = 1, replyTo = PreviewUserB),
    ),
    replyCount = 12,
)
private val PreviewThreads = listOf(
    CommentThread(previewComment(5, PreviewUserC, "这首的混音在耳机里层次很清楚，外放就差一点。"), emptyList(), 0),
    CommentThread(previewComment(4, PreviewUserB, "循环一下午了。", likes = 3), emptyList(), 0),
    CommentThread(
        comment = previewComment(3, PreviewUserA, "").copy(removed = true, author = null),
        replies = listOf(previewComment(8, PreviewUserB, "原评论去哪了。", rootId = 3)),
        replyCount = 1,
    ),
    PreviewHotThread,
)

/**
 * The page with sample comments, at the size of [width] so the toolbar and margins match.
 *
 * @since 261031
 */
@Composable
internal fun PreviewCommentsPage(
    title: String = "评论 16",
    width: Dp = 360.dp,
    darkTheme: Boolean = false,
    status: InitializeStatus = InitializeStatus.LOADED,
    empty: Boolean = false,
    startAtComments: Boolean = false,
    header: (@Composable () -> Unit)? = null,
) {
    PreviewPage(title, width, darkTheme) {
        CommentList(
            header = header,
            status = status,
            hotThreads = if (empty) emptyList() else listOf(PreviewHotThread),
            threads = if (empty) emptyList() else PreviewThreads,
            order = CommentModule.ORDER_NEWEST,
            onOrderChange = {},
            hasMore = true,
            loadingMore = false,
            loadMoreFailed = false,
            actions = PreviewActions,
            onLoadMore = {},
            onRetry = {},
            onOpenReplies = {},
            listState = rememberLazyListState(initialFirstVisibleItemIndex = if (startAtComments) 1 else 0),
        )
    }
}

@Composable
private fun PreviewPage(
    title: String,
    width: Dp,
    darkTheme: Boolean = false,
    replyTo: CommentItem? = null,
    list: @Composable () -> Unit,
) {
    PreviewTheme(darkTheme = darkTheme, background = true) {
        CompositionLocalProvider(LocalWindowSize provides DpSize(width, 800.dp)) {
            CommentsPageLayout(
                title = title,
                onBack = {},
                list = list,
                composer = {
                    CommentComposer("", {}, replyTo, {}, sending = false, onSend = {})
                },
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 800, name = "Page · Replies")
@Composable
private fun CommentsRepliesPreview() {
    PreviewPage(title = "回复 12", width = 360.dp, replyTo = PreviewHotThread.replies.first()) {
        ReplyList(
            thread = PreviewHotThread,
            status = InitializeStatus.LOADED,
            replies = PreviewHotThread.replies,
            hasMore = false,
            loadingMore = false,
            actions = PreviewActions,
            onLoadMore = {},
            onRetry = {},
        )
    }
}

@Preview(widthDp = 360, name = "Thread · Liked, with replies")
@Composable
private fun ThreadItemPreview() {
    PreviewTheme(background = true) {
        ThreadItem(PreviewHotThread, PreviewActions, onOpenReplies = {})
    }
}

@Preview(widthDp = 360, name = "Thread · Removed, with a reply")
@Composable
private fun ThreadItemRemovedPreview() {
    PreviewTheme(background = true) {
        ThreadItem(PreviewThreads[2], PreviewActions, onOpenReplies = {})
    }
}

@Preview(widthDp = 360, name = "Composer · Empty")
@Composable
private fun CommentComposerPreview() {
    PreviewTheme(background = true) {
        CommentComposer("", {}, null, {}, sending = false, onSend = {}, modifier = Modifier.padding(16.dp))
    }
}

@Preview(widthDp = 360, name = "Composer · Replying, near the limit")
@Composable
private fun CommentComposerReplyingPreview() {
    PreviewTheme(background = true) {
        CommentComposer(
            value = "半拍那里确实，第一次听差点以为是卡了。".repeat(25).take(CommentModule.CONTENT_MAX_CHARS - 12),
            onValueChange = {},
            replyTo = PreviewHotThread.replies.first(),
            onCancelReply = {},
            sending = false,
            onSend = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(widthDp = 360, name = "Composer · With tools")
@Composable
private fun CommentComposerToolsPreview() {
    PreviewTheme(background = true) {
        CommentComposer("半拍那里", {}, null, {}, sending = false, onSend = {}, modifier = Modifier.padding(16.dp)) {
            HachimiIconButton(onClick = {}, touchMode = true) {
                Icon(Icons.Outlined.EmojiEmotions, null, tint = HachimiTheme.colorScheme.onSurfaceVariant)
            }
            HachimiIconButton(onClick = {}, touchMode = true) {
                Icon(Icons.Outlined.Image, null, tint = HachimiTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
