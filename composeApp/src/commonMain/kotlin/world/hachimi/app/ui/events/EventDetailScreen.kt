package world.hachimi.app.ui.events

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.datetime.LocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.PostModule
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.getPlatform
import world.hachimi.app.model.EventDetailViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.MarkdownText
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.player.fullscreen.components.AmbientUserChip
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.WindowSize
import world.hachimi.app.util.YMD
import world.hachimi.app.util.fillMaxWidthIn
import world.hachimi.app.util.formatTime
import world.hachimi.app.util.isValidHttpsUrl
import kotlin.time.Clock

@Composable
fun EventDetailScreen(
    eventId: Long,
    global: GlobalStore = koinInject(),
    vm: EventDetailViewModel = koinViewModel(),
) {
    DisposableEffect(eventId, vm) {
        vm.mounted(eventId)
        onDispose { vm.dispose() }
    }

    AnimatedContent(vm.initStat) { st ->
        when (st) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> EventDetailContent(data = vm.data) {
                global.nav.push(Route.Root.PublicUserSpace(it))
            }
        }
    }
}

@Composable
private fun EventDetailContent(
    data: PostModule.PostDetail?,
    onNavToUser: (Long) -> Unit,
) {
    if (data == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("内容为空")
        }
        return
    }

    if (data.contentType != "markdown") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("当前客户端不支持展示该文章内容，请升级版本")
        }
        return
    }

    val context = LocalPlatformContext.current

    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(LocalContentInsets.current.asPaddingValues())
                .padding(24.dp)
                .fillMaxWidthIn(maxWidth = WindowSize.COMPACT),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AmbientUserChip(
                    onClick = { onNavToUser(data.author.uid) },
                    avatar = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalPlatformContext.current)
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
            Text(
                text = data.title,
                style = MaterialTheme.typography.headlineMedium
            )

            if (data.coverUrl != null) {
                Surface(
                    Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                    color = LocalContentColor.current.copy(0.12f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(data.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Event cover",
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

            Spacer(
                Modifier
                    .navigationBarsPadding()
                    .padding(LocalContentInsets.current.asPaddingValues())
            )
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
        author = UserModule.PublicUserProfile(0, "Author", null, null, null, false),
        title = "This is a test title",
        content = previewMarkdown,
        contentType = "markdown",
        coverUrl = "",
        createTime = Clock.System.now(),
        updateTime = Clock.System.now()
    )

@Preview(heightDp = 1600)
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        EventDetailContent(
            data = previewPost,
            onNavToUser = {}
        )
    }
}