package world.hachimi.app.ui.contributor

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.CreatePostViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.creation.publish.components.FormItem
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.util.singleLined

@Composable
fun CreatePostScreen(global: GlobalStore = koinInject(), vm: CreatePostViewModel = koinViewModel()) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { }
    }

    AnimatedContent(vm.initializeStatus) {
        when (it) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
            InitializeStatus.LOADED -> Content(vm, global)
        }
    }
}

@Composable
private fun Content(vm: CreatePostViewModel, global: GlobalStore) {
    Box(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(LocalContentInsets.current.asPaddingValues())
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().wrapContentWidth().widthIn(max = 900.dp)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("发布文章", style = MaterialTheme.typography.titleLarge)

            Card {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    FormItem(
                        header = { Text("设置封面*") },
                        subtitle = { Text("支持 jpg, png, webp 格式，大小不超过 10MB") }
                    ) {
                        Surface(
                            Modifier.size(200.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = LocalContentColor.current.copy(0.12f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickable(enabled = !vm.coverImageUploading) { vm.setCoverImage() },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = vm.coverImage,
                                    contentDescription = "Cover Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                if (vm.coverImageUploading) {
                                    if (vm.coverImageUploadProgress == 0f || vm.coverImageUploadProgress == 1f) {
                                        CircularProgressIndicator()
                                    } else {
                                        CircularProgressIndicator(progress = { vm.coverImageUploadProgress })
                                    }
                                }
                            }
                        }
                    }

                    FormItem(header = { Text("标题*") }) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = vm.title,
                            onValueChange = { vm.title = it.singleLined() },
                            singleLine = true
                        )
                    }

                    FormItem(
                        header = { Text("内容*") },
                        subtitle = { Text("Markdown 格式") }
                    ) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = vm.content,
                            onValueChange = { vm.content = it },
                            singleLine = false,
                            minLines = 8
                        )
                    }
                }
            }

            Card {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { vm.publish() },
                        enabled = !vm.isOperating && !vm.coverImageUploading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("发布")
                    }
                }
            }
        }

        if (vm.showSuccessDialog) {
            AlertDialog(
                title = { Text("发布成功") },
                text = {
                    Text(
                        if (vm.publishedPostId != null) "文章已发布（ID: ${vm.publishedPostId}）" else "文章已发布"
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        vm.closeSuccessDialog()
                        global.nav.back()
                    }) { Text("完成") }
                },
                onDismissRequest = {
                    vm.closeSuccessDialog()
                    global.nav.back()
                }
            )
        }
    }
}