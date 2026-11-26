package world.hachimi.app.ui.moment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_close
import hachimiworld.composeapp.generated.resources.moment_add_image_cd
import hachimiworld.composeapp.generated.resources.moment_create_title
import hachimiworld.composeapp.generated.resources.moment_discard
import hachimiworld.composeapp.generated.resources.moment_discard_text
import hachimiworld.composeapp.generated.resources.moment_discard_title
import hachimiworld.composeapp.generated.resources.moment_image_failed
import hachimiworld.composeapp.generated.resources.moment_image_rejected
import hachimiworld.composeapp.generated.resources.moment_input_placeholder
import hachimiworld.composeapp.generated.resources.moment_keep_editing
import hachimiworld.composeapp.generated.resources.moment_publish
import hachimiworld.composeapp.generated.resources.moment_remove_cd
import hachimiworld.composeapp.generated.resources.moment_share
import hachimiworld.composeapp.generated.resources.moment_shared_load_failed
import hachimiworld.composeapp.generated.resources.notification_retry
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import world.hachimi.app.api.module.MomentModule
import world.hachimi.app.model.DraftImage
import world.hachimi.app.model.MomentComposer
import world.hachimi.app.model.MomentDraft
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.ElevatedCard
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.ModalBottomSheet
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.util.WindowSize

/**
 * Writing a moment, over whatever page opened it: a dialog on wide windows, a bottom sheet on compact ones.
 * Drawn once by the root screen; shown while [MomentComposer.draft] is set.
 *
 * Images go up as soon as they're picked; it can be posted once they're all up.
 *
 * @since 261114
 */
@Composable
fun MomentComposerHost(composer: MomentComposer = koinInject()) {
    val draft = composer.draft ?: return
    var confirmDiscard by remember(draft) { mutableStateOf(false) }
    /** Closes right away unless there's writing or images to lose. */
    val requestClose: () -> Boolean = {
        when {
            draft.publishing -> false
            draft.hasDraft -> {
                confirmDiscard = true
                false
            }
            else -> true
        }
    }
    val close = { if (requestClose()) composer.close() }

    if (LocalWindowSize.current.width < WindowSize.COMPACT) {
        ModalBottomSheet(
            onDismissRequest = composer::close,
            confirmDismiss = requestClose,
        ) {
            ComposerContent(draft, onClose = close, modifier = Modifier.padding(bottom = 8.dp))
        }
    } else {
        Dialog(
            onDismissRequest = close,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            ElevatedCard(
                modifier = Modifier.padding(24.dp).widthIn(max = 600.dp).fillMaxWidth(),
                color = HachimiTheme.colorScheme.surface.compositeOver(HachimiTheme.colorScheme.background),
            ) {
                ComposerContent(draft, onClose = close, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
            }
        }
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text(stringResource(Res.string.moment_discard_title)) },
            text = { Text(stringResource(Res.string.moment_discard_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    composer.close()
                }) { Text(stringResource(Res.string.moment_discard)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) { Text(stringResource(Res.string.moment_keep_editing)) }
            },
        )
    }
}

@Composable
private fun ComposerContent(vm: MomentDraft, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(if (vm.share != null) Res.string.moment_share else Res.string.moment_create_title),
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            HachimiIconButton(onClick = onClose, enabled = !vm.publishing) {
                Icon(Icons.Default.Close, stringResource(Res.string.common_close))
            }
        }
        Column(
            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(
                value = vm.content,
                onValueChange = { vm.content = it.take(MomentModule.CONTENT_MAX_CHARS) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.moment_input_placeholder)) },
                minLines = 4,
                enabled = !vm.publishing,
            )
            if (vm.share != null) SharedPreview(vm)
            ImagesEditor(vm)
        }
        Row(Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${vm.content.length} / ${MomentModule.CONTENT_MAX_CHARS}",
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                color = HachimiTheme.colorScheme.onSurfaceVariant,
            )
            AccentButton(onClick = vm::publish, enabled = vm.canPublish) {
                if (vm.publishing) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(Res.string.moment_publish))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ImagesEditor(vm: MomentDraft) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        vm.images.forEach { image ->
            DraftImageTile(image, onRemove = { vm.removeImage(image) }, onRetry = { vm.retryUpload(image) })
        }
        if (vm.canAddImages && !vm.publishing) {
            Box(
                Modifier
                    .size(TileSize)
                    .clip(TileShape)
                    .border(1.dp, HachimiTheme.colorScheme.outline, TileShape)
                    .clickable(onClick = vm::pickImages),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.AddPhotoAlternate,
                        stringResource(Res.string.moment_add_image_cd),
                        Modifier.size(28.dp),
                        tint = HachimiTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "${vm.images.size} / ${MomentModule.MAX_IMAGES}",
                        fontSize = 12.sp,
                        color = HachimiTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DraftImageTile(image: DraftImage, onRemove: () -> Unit, onRetry: () -> Unit) {
    val state = image.state
    val context = LocalPlatformContext.current
    Box(Modifier.size(TileSize).clip(TileShape)) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(image.bytes).crossfade(true).build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().background(HachimiTheme.colorScheme.onSurface.copy(0.08f)),
            contentScale = ContentScale.Crop,
        )
        when (state) {
            is DraftImage.State.Uploading -> Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(Modifier.size(28.dp), color = Color.White, strokeWidth = 3.dp, progress = { state.progress })
            }
            is DraftImage.State.Rejected -> Box(
                Modifier.fillMaxSize().background(Color(0xCCD32F2F)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(Res.string.moment_image_rejected),
                    modifier = Modifier.padding(6.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
            is DraftImage.State.Failed -> Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)).clickable(onClick = onRetry),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Refresh, null, Modifier.size(20.dp), tint = Color.White)
                    Text(
                        stringResource(Res.string.moment_image_failed),
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            is DraftImage.State.Uploaded -> {}
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Close, stringResource(Res.string.moment_remove_cd), Modifier.size(14.dp), tint = Color.White)
        }
    }
}

/** The song or playlist being shared, as the moment will show it. */
@Composable
private fun SharedPreview(vm: MomentDraft) {
    val shared = vm.shared
    when {
        shared != null -> AttachmentCard(shared, onClick = null)
        vm.sharedLoadFailed -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(Res.string.moment_shared_load_failed),
                fontSize = 13.sp,
                color = HachimiTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = vm::loadShared) { Text(stringResource(Res.string.notification_retry)) }
        }
        else -> Box(Modifier.fillMaxWidth().heightIn(min = 64.dp), contentAlignment = Alignment.CenterStart) {
            CircularProgressIndicator(Modifier.padding(start = 16.dp).size(20.dp), strokeWidth = 2.dp)
        }
    }
}

private val TileSize = 96.dp
private val TileShape = RoundedCornerShape(10.dp)
