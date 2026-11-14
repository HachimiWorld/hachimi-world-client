package world.hachimi.app.ui.events

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import world.hachimi.app.ui.design.components.placeholder
import world.hachimi.app.ui.util.loadStateTransition
import world.hachimi.app.ui.component.ErrorPage
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.events_content_empty
import hachimiworld.composeapp.generated.resources.events_content_unsupported
import hachimiworld.composeapp.generated.resources.events_cover_cd
import org.jetbrains.compose.resources.stringResource
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.datetime.LocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.api.module.CommentModule
import world.hachimi.app.api.module.PostModule
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.getPlatform
import world.hachimi.app.model.CommentTarget
import world.hachimi.app.model.EventDetailViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.comment.CommentsPage
import world.hachimi.app.ui.comment.PreviewCommentsPage
import world.hachimi.app.ui.component.MarkdownText
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.component.TitleStyle
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.player.fullscreen.components.AmbientUserChip
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime
import world.hachimi.app.util.isValidHttpsUrl
import kotlin.time.Clock

@Composable
fun EventDetailScreen(
    eventId: Long,
    atComments: Boolean = false,
    global: GlobalStore = koinInject(),
    vm: EventDetailViewModel = koinViewModel(),
) {
    val navigator = LocalNavigator.current

    DisposableEffect(eventId, vm) {
        vm.mounted(eventId)
        onDispose { vm.dispose() }
    }

    // The comments load together with the post; they show once the post is there, and only a post
    // that didn't load, or has nothing in it, takes the page from them
    val state = when {
        vm.initStat == InitializeStatus.FAILED -> PostState.FAILED
        vm.initStat == InitializeStatus.LOADED && vm.data == null -> PostState.EMPTY
        else -> PostState.SHOWN
    }
    AnimatedContent(state, modifier = Modifier.fillMaxSize(), transitionSpec = { loadStateTransition() }) { st ->
        when (st) {
            PostState.FAILED -> EventDetailPlaceholder(navigator::back) { ErrorPage(onRetry = { vm.retry() }) }
            PostState.EMPTY -> EventDetailPlaceholder(navigator::back) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.events_content_empty))
                }
            }
            PostState.SHOWN -> {
                val post = vm.data
                CommentsPage(
                    target = CommentTarget(CommentModule.TARGET_POST, eventId, post?.author?.uid),
                    title = { post?.title.orEmpty() },
                    onBack = navigator::back,
                    header = {
                        AnimatedContent(post, transitionSpec = { loadStateTransition() }, contentKey = { it != null }) { shown ->
                            if (shown == null) PostArticleSkeleton()
                            else PostArticle(shown, onNavToUser = { navigator.push(Route.Root.PublicUserSpace(it)) })
                        }
                    },
                    startAtComments = atComments,
                    // Comments that come back first wait for the post; a post that comes first shows at once
                    holdComments = post == null,
                )
            }
        }
    }
}

private enum class PostState { SHOWN, FAILED, EMPTY }

/** The page before there's a post to show. */
@Composable
private fun EventDetailPlaceholder(onBack: () -> Unit, content: @Composable () -> Unit) {
    ScreenScaffold(
        titleStyle = TitleStyle.Compact,
        title = { Text("", maxLines = 1) },
        showBack = true,
        onBack = onBack,
    ) {
        content()
    }
}

/** Stands in for [PostArticle] while the post loads, in its place above the comments: the author, a cover, then paragraphs. */
@Composable
private fun PostArticleSkeleton() {
    Column(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(24.dp).placeholder(true, CircleShape))
            Box(Modifier.padding(start = 8.dp).width(64.dp).height(10.dp).placeholder(true))
            Box(Modifier.padding(start = 12.dp).width(40.dp).height(10.dp).placeholder(true))
        }
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).placeholder(true, RoundedCornerShape(16.dp)))
        repeat(2) { paragraph ->
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) { Box(Modifier.fillMaxWidth().height(12.dp).placeholder(true)) }
                Box(Modifier.fillMaxWidth(if (paragraph == 0) 0.6f else 0.35f).height(12.dp).placeholder(true))
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(HachimiTheme.colorScheme.outline))
    }
}

/** The post, above its comments; those scroll together, so this doesn't scroll on its own. */
@Composable
private fun PostArticle(
    data: PostModule.PostDetail,
    onNavToUser: (Long) -> Unit,
) {
    if (data.contentType != "markdown") {
        Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(Res.string.events_content_unsupported))
        }
        return
    }

    val context = LocalPlatformContext.current

    SelectionContainer {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AmbientUserChip(
                    onClick = { onNavToUser(data.author.uid) },
                    avatar = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalPlatformContext.current)
                            .httpHeaders(CoilHeaders)
                            .data(data.author.avatarUrl)
                            .crossfade(true)
                            .build()
                    ),
                    name = data.author.username,
                )

                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = formatTime(
                        data.createTime,
                        distance = true,
                        precise = false,
                        fullFormat = LocalDateTime.Formats.YMD
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = LocalContentColor.current.copy(alpha = 0.8f)
                )

            }
            if (data.coverUrl != null) {
                Surface(
                    Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                    color = LocalContentColor.current.copy(0.12f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .httpHeaders(CoilHeaders)
                            .data(data.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(Res.string.events_cover_cd),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 1040.dp),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }

            MarkdownText(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                markdown = data.content,
                onUrlClick = { url ->
                    if (isValidHttpsUrl(url)) {
                        getPlatform().openUrl(url)
                    }
                }
            )

            Box(Modifier.fillMaxWidth().height(1.dp).background(HachimiTheme.colorScheme.outline))
        }
    }
}

private val previewMarkdown = """
# This is a test title
## h2
### h3
#### h4
##### h5
###### h6

This is body.

- Unordered list 1
- Unordered list 2
- Unordered list 3
- Unordered list 4

Test a [Links](https://www.example.com)

Test a span image ![The image](https://dummyimage.com/600x400/000/0011ff)

Test a block image

![image](https://dummyimage.com/600x400/000/0011ff)
""".trimIndent()

private val previewPost
    @Stable
    get() = PostModule.PostDetail(
        id = 0,
        author = UserModule.PublicUserProfile(0, "Author", null, null, null, false, listOf()),
        title = "This is a test title",
        content = previewMarkdown,
        contentType = "markdown",
        coverUrl = "",
        createTime = Clock.System.now(),
        updateTime = Clock.System.now()
    )

@Preview(heightDp = 1600, name = "Article")
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        PostArticle(
            data = previewPost,
            onNavToUser = {}
        )
    }
}

@Preview(widthDp = 360, heightDp = 800, name = "Page · Compact")
@Composable
private fun EventDetailCompactPreview() {
    PreviewCommentsPage(title = previewPost.title) { PostArticle(previewPost, onNavToUser = {}) }
}

@Preview(widthDp = 360, heightDp = 800, name = "Page · Compact · Dark")
@Composable
private fun EventDetailCompactDarkPreview() {
    PreviewCommentsPage(title = previewPost.title, darkTheme = true) { PostArticle(previewPost, onNavToUser = {}) }
}

@Preview(widthDp = 360, heightDp = 800, name = "Page · At comments")
@Composable
private fun EventDetailAtCommentsPreview() {
    PreviewCommentsPage(title = previewPost.title, startAtComments = true) { PostArticle(previewPost, onNavToUser = {}) }
}

@Preview(widthDp = 360, heightDp = 2000, name = "Page · No comments, whole article")
@Composable
private fun EventDetailEmptyPreview() {
    PreviewCommentsPage(title = previewPost.title, empty = true) { PostArticle(previewPost, onNavToUser = {}) }
}

@Preview(widthDp = 1280, heightDp = 800, name = "Page · Expanded")
@Composable
private fun EventDetailExpandedPreview() {
    PreviewCommentsPage(title = previewPost.title, width = 1280.dp) { PostArticle(previewPost, onNavToUser = {}) }
}
