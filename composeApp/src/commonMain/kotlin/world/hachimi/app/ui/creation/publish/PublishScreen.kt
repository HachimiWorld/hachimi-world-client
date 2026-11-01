package world.hachimi.app.ui.creation.publish

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_add
import hachimiworld.composeapp.generated.resources.common_add_cd
import hachimiworld.composeapp.generated.resources.common_cancel
import hachimiworld.composeapp.generated.resources.common_ok
import hachimiworld.composeapp.generated.resources.common_remove_cd
import hachimiworld.composeapp.generated.resources.platform_bilibili
import hachimiworld.composeapp.generated.resources.platform_douyin
import hachimiworld.composeapp.generated.resources.platform_youtube
import hachimiworld.composeapp.generated.resources.publish_add_link_title
import hachimiworld.composeapp.generated.resources.publish_add_staff_external
import hachimiworld.composeapp.generated.resources.publish_add_staff_internal
import hachimiworld.composeapp.generated.resources.publish_add_staff_name
import hachimiworld.composeapp.generated.resources.publish_add_staff_role
import hachimiworld.composeapp.generated.resources.publish_add_staff_role_supporting
import hachimiworld.composeapp.generated.resources.publish_add_staff_title
import hachimiworld.composeapp.generated.resources.publish_audio_change
import hachimiworld.composeapp.generated.resources.publish_audio_choose
import hachimiworld.composeapp.generated.resources.publish_audio_download
import hachimiworld.composeapp.generated.resources.publish_audio_header
import hachimiworld.composeapp.generated.resources.publish_audio_subtitle
import hachimiworld.composeapp.generated.resources.publish_cover_header
import hachimiworld.composeapp.generated.resources.publish_cover_subtitle
import hachimiworld.composeapp.generated.resources.publish_creation_type_header
import hachimiworld.composeapp.generated.resources.publish_creation_type_subtitle
import hachimiworld.composeapp.generated.resources.publish_derive_artist_header
import hachimiworld.composeapp.generated.resources.publish_derive_id_header
import hachimiworld.composeapp.generated.resources.publish_derive_id_supporting
import hachimiworld.composeapp.generated.resources.publish_derive_link_header
import hachimiworld.composeapp.generated.resources.publish_derive_title_header
import hachimiworld.composeapp.generated.resources.publish_description_header
import hachimiworld.composeapp.generated.resources.publish_description_subtitle
import hachimiworld.composeapp.generated.resources.publish_explicit_header
import hachimiworld.composeapp.generated.resources.publish_explicit_no
import hachimiworld.composeapp.generated.resources.publish_explicit_subtitle
import hachimiworld.composeapp.generated.resources.publish_explicit_yes
import hachimiworld.composeapp.generated.resources.publish_jmid_header
import hachimiworld.composeapp.generated.resources.publish_jmid_set_prefix
import hachimiworld.composeapp.generated.resources.publish_link_bv_placeholder
import hachimiworld.composeapp.generated.resources.publish_link_choose_platform
import hachimiworld.composeapp.generated.resources.publish_link_https_required
import hachimiworld.composeapp.generated.resources.publish_link_invalid_bv
import hachimiworld.composeapp.generated.resources.publish_link_invalid_sm
import hachimiworld.composeapp.generated.resources.publish_link_platform_required
import hachimiworld.composeapp.generated.resources.publish_link_sm_placeholder
import hachimiworld.composeapp.generated.resources.publish_link_url_placeholder
import hachimiworld.composeapp.generated.resources.publish_links_subtitle
import hachimiworld.composeapp.generated.resources.publish_lyrics_header
import hachimiworld.composeapp.generated.resources.publish_lyrics_none_warning
import hachimiworld.composeapp.generated.resources.publish_lyrics_subtitle
import hachimiworld.composeapp.generated.resources.publish_lyrics_tool
import hachimiworld.composeapp.generated.resources.publish_lyrics_type_lrc
import hachimiworld.composeapp.generated.resources.publish_lyrics_type_none
import hachimiworld.composeapp.generated.resources.publish_lyrics_type_text
import hachimiworld.composeapp.generated.resources.publish_modify_comment
import hachimiworld.composeapp.generated.resources.publish_modify_comment_supporting
import hachimiworld.composeapp.generated.resources.publish_notice
import hachimiworld.composeapp.generated.resources.publish_origin_artist_header
import hachimiworld.composeapp.generated.resources.publish_origin_artist_supporting
import hachimiworld.composeapp.generated.resources.publish_origin_link_header
import hachimiworld.composeapp.generated.resources.publish_origin_link_supporting
import hachimiworld.composeapp.generated.resources.publish_origin_title_header
import hachimiworld.composeapp.generated.resources.publish_origin_title_supporting
import hachimiworld.composeapp.generated.resources.publish_screen_title_create
import hachimiworld.composeapp.generated.resources.publish_screen_title_edit
import hachimiworld.composeapp.generated.resources.publish_staff_subtitle
import hachimiworld.composeapp.generated.resources.publish_submit_create
import hachimiworld.composeapp.generated.resources.publish_submit_edit
import hachimiworld.composeapp.generated.resources.publish_subtitle_header
import hachimiworld.composeapp.generated.resources.publish_subtitle_subtitle
import hachimiworld.composeapp.generated.resources.publish_success_text_create
import hachimiworld.composeapp.generated.resources.publish_success_text_edit
import hachimiworld.composeapp.generated.resources.publish_success_title_create
import hachimiworld.composeapp.generated.resources.publish_success_title_edit
import hachimiworld.composeapp.generated.resources.publish_tags_header
import hachimiworld.composeapp.generated.resources.publish_tags_subtitle
import hachimiworld.composeapp.generated.resources.publish_title_header
import hachimiworld.composeapp.generated.resources.publish_title_subtitle
import hachimiworld.composeapp.generated.resources.review_creation_type_derivation
import hachimiworld.composeapp.generated.resources.review_creation_type_derivation_of_derivation
import hachimiworld.composeapp.generated.resources.review_creation_type_original
import hachimiworld.composeapp.generated.resources.review_external_links
import hachimiworld.composeapp.generated.resources.review_production_crew
import hachimiworld.composeapp.generated.resources.song_cover_cd
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.getPlatform
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.PublishViewModel
import world.hachimi.app.model.PublishViewModel.LyricsType
import world.hachimi.app.model.PublishViewModel.Type
import world.hachimi.app.nav.HandleNavigationRequests
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.creation.publish.components.FormItem
import world.hachimi.app.ui.creation.publish.components.InitJmidDialog
import world.hachimi.app.ui.creation.publish.components.JmidTextField
import world.hachimi.app.ui.creation.publish.components.PrefixInactiveDialog
import world.hachimi.app.ui.creation.publish.components.TagEdit
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.RadioButton
import world.hachimi.app.ui.design.components.Select
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.insets.multiplatformStatusBarsPadding
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.fillMaxWidthIn
import world.hachimi.app.ui.util.listTailPadding
import world.hachimi.app.util.formatSongDuration
import world.hachimi.app.util.singleLined
import kotlin.time.Duration.Companion.seconds

@Composable
fun PublishScreen(
    songId: Long?,
    reviewId: Long? = null,
    vm: PublishViewModel = koinViewModel(),
    global: GlobalStore = koinInject()
) {
    val navigator = LocalNavigator.current

    DisposableEffect(vm, songId, reviewId) {
        vm.mounted(songId, reviewId)
        onDispose { vm.dispose() }
    }
    HandleNavigationRequests(vm.navigationRequests, navigator)
    val screenTitle = when (vm.initializeStatus) {
        InitializeStatus.LOADED -> if (vm.type == Type.CREATE) Res.string.publish_screen_title_create else Res.string.publish_screen_title_edit
        else -> Res.string.publish_screen_title_create
    }.let { stringResource(it) }
    ScreenScaffold(
        title = { Text(screenTitle, maxLines = 1) },
        showBack = true,
        onBack = navigator::back
    ) {
        AnimatedContent(vm.initializeStatus) {
            when (it) {
                InitializeStatus.INIT -> LoadingPage()
                InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
                InitializeStatus.LOADED -> Content(vm, global)
            }
        }
    }

    if (vm.showPrefixInactiveDialog) PrefixInactiveDialog(
        onExit = {
            vm.showPrefixInactiveDialog = false
            navigator.back()
        }
    )
}

@Composable
private fun Content(vm: PublishViewModel, global: GlobalStore) {
    val editing = vm.type != Type.CREATE
    Box(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .multiplatformStatusBarsPadding()
            .listTailPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxWidthIn()
                .padding(AdaptiveScreenMargin),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (vm.type == Type.CREATE) Text(
                stringResource(Res.string.publish_notice),
                style = MaterialTheme.typography.bodyMedium
            )

            Card {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    FormItem(
                        header = { Text(stringResource(Res.string.publish_audio_header)) },
                        subtitle = { Text(stringResource(Res.string.publish_audio_subtitle)) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (editing) Button(onClick = {
                                getPlatform().openUrl(vm.audioUrl!!)
                            }) {
                                Text(stringResource(Res.string.publish_audio_download))
                            }

                            Button(onClick = { vm.setAudioFile() }, enabled = !vm.audioUploading) {
                                if (editing) Text(stringResource(Res.string.publish_audio_change))
                                else Text(stringResource(Res.string.publish_audio_choose))
                            }

                            if (vm.audioUploading) {
                                if (vm.audioUploadProgress == 0f || vm.audioUploadProgress == 1f) CircularProgressIndicator()
                                else CircularProgressIndicator(progress = { vm.audioUploadProgress })
                            }

                            if (vm.audioUploaded) {
                                Text(
                                    text = "${vm.audioFileName}\n${formatSongDuration(vm.audioDurationSecs.seconds)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    FormItem(
                        header = { Text(stringResource(Res.string.publish_cover_header)) },
                        subtitle = {
                            Text(stringResource(Res.string.publish_cover_subtitle))
                        }
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
                                if (editing) AsyncImage(
                                    model = vm.coverImage ?: ImageRequest.Builder(LocalPlatformContext.current)
                                        .httpHeaders(CoilHeaders)
                                        .data(vm.coverImageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = stringResource(Res.string.song_cover_cd),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                ) else AsyncImage(
                                    model = vm.coverImage,
                                    contentDescription = stringResource(Res.string.song_cover_cd),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                if (vm.coverImageUploading) {
                                    if (vm.coverImageUploadProgress == 0f || vm.coverImageUploadProgress == 1f) CircularProgressIndicator()
                                    else CircularProgressIndicator(progress = { vm.coverImageUploadProgress })
                                }
                            }
                        }
                    }
                }
            }

            Card {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    if (vm.type == Type.CREATE) FormItem(
                        header = { Text(stringResource(Res.string.publish_jmid_header)) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val prefix = vm.jmidPrefix
                            if (prefix == null) {
                                Button(onClick = { vm.showJmidDialog() }) {
                                    Text(stringResource(Res.string.publish_jmid_set_prefix))
                                }
                            } else {
                                JmidTextField(
                                    jmidNumber = vm.jmidNumber,
                                    jmidPrefix = prefix,
                                    valid = vm.jmidValid,
                                    supportText = vm.jmidSupportText,
                                    onNumberChange = vm::updateJmidNumber
                                )
                            }
                        }
                    }

                    FormItem(
                        header = { Text(stringResource(Res.string.publish_title_header)) },
                        subtitle = { Text(stringResource(Res.string.publish_title_subtitle)) }
                    ) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = vm.title,
                            onValueChange = { vm.title = it.singleLined() },
                            singleLine = true
                        )
                    }

                    FormItem(
                        header = { Text(stringResource(Res.string.publish_subtitle_header)) },
                        subtitle = { Text(stringResource(Res.string.publish_subtitle_subtitle)) }
                    ) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = vm.subtitle,
                            onValueChange = { vm.subtitle = it.singleLined() },
                            singleLine = true
                        )
                    }

                    FormItem(
                        header = { Text(stringResource(Res.string.publish_tags_header)) },
                        subtitle = { Text(stringResource(Res.string.publish_tags_subtitle)) }
                    ) {
                        TagEdit(vm)
                    }

                    FormItem(
                        header = { Text(stringResource(Res.string.publish_description_header)) },
                        subtitle = { Text(stringResource(Res.string.publish_description_subtitle)) }
                    ) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = vm.description,
                            onValueChange = { vm.description = it }
                        )
                    }
                }
            }

            Card {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    FormItem(
                        header = { Text(stringResource(Res.string.publish_lyrics_header)) },
                        subtitle = {
                            Text(stringResource(Res.string.publish_lyrics_subtitle), modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                getPlatform().openUrl("https://lrc-maker.github.io/")
                            }) {
                                Text(stringResource(Res.string.publish_lyrics_tool))
                            }
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LyricsType.entries.forEach {
                                RadioButton(
                                    selected = vm.lyricsType == it,
                                    onClick = { vm.lyricsType = it }
                                ) {
                                    Text(
                                        when (it) {
                                            LyricsType.LRC -> stringResource(Res.string.publish_lyrics_type_lrc)
                                            LyricsType.TEXT -> stringResource(Res.string.publish_lyrics_type_text)
                                            LyricsType.NONE -> stringResource(Res.string.publish_lyrics_type_none)
                                        }
                                    )
                                }
                            }
                        }

                        if (vm.lyricsType == LyricsType.NONE) {
                            Text(
                                stringResource(Res.string.publish_lyrics_none_warning),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (vm.lyricsType != LyricsType.NONE) {
                            LrcTextField(
                                value = vm.lyrics,
                                onValueChange = { vm.lyrics = it }
                            )
                        }
                    }
                }
            }

            Card {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    FormItem(
                        header = { Text(stringResource(Res.string.publish_creation_type_header)) },
                        subtitle = { Text(stringResource(Res.string.publish_creation_type_subtitle)) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = vm.creationType == 0,
                                onClick = { vm.creationType = 0 }
                            ) {
                                Text(stringResource(Res.string.review_creation_type_original))
                            }

                            RadioButton(
                                selected = vm.creationType == 1,
                                onClick = { vm.creationType = 1 }
                            ) {
                                Text(stringResource(Res.string.review_creation_type_derivation))
                            }

                            RadioButton(
                                selected = vm.creationType == 2,
                                onClick = { vm.creationType = 2 }
                            ) {
                                Text(stringResource(Res.string.review_creation_type_derivation_of_derivation))
                            }
                        }
                    }

                    if (vm.creationType > 0) {
                        /*FormItem(header = { Text("原作基米ID") }) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = vm.originId,
                                onValueChange = { vm.originId = it.singleLined() },
                                singleLine = true,
                                supportingText = { Text("如果原作是基米天堂站内的作品，填写基米 ID 即可，无需再填写标题与链接") }
                            )
                        }*/
                        FormItem(header = { Text(stringResource(Res.string.publish_origin_title_header)) }) {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = vm.originTitle,
                                onValueChange = { vm.originTitle = it.singleLined() },
                                singleLine = true,
                                supportingText = { Text(stringResource(Res.string.publish_origin_title_supporting)) }
                            )
                        }
                        FormItem(header = { Text(stringResource(Res.string.publish_origin_artist_header)) }) {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = vm.originArtist,
                                onValueChange = { vm.originArtist = it.singleLined() },
                                singleLine = true,
                                supportingText = { Text(stringResource(Res.string.publish_origin_artist_supporting)) }
                            )
                        }
                        FormItem(header = { Text(stringResource(Res.string.publish_origin_link_header)) }) {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = vm.originLink,
                                onValueChange = { vm.originLink = it.singleLined() },
                                singleLine = true,
                                placeholder = { Text("https://") },
                                supportingText = { Text(stringResource(Res.string.publish_origin_link_supporting)) }
                            )
                        }
                    }

                    if (vm.creationType > 1) {
                        FormItem(header = { Text(stringResource(Res.string.publish_derive_id_header)) }) {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = vm.deriveId,
                                onValueChange = { vm.deriveId = it.singleLined() },
                                singleLine = true,
                                supportingText = { Text(stringResource(Res.string.publish_derive_id_supporting)) }
                            )
                        }
                        FormItem(header = { Text(stringResource(Res.string.publish_derive_title_header)) }) {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = vm.deriveTitle,
                                onValueChange = { vm.deriveTitle = it.singleLined() },
                                singleLine = true
                            )
                        }
                        FormItem(header = { Text(stringResource(Res.string.publish_derive_artist_header)) }) {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = vm.deriveArtist,
                                onValueChange = { vm.deriveArtist = it.singleLined() },
                                singleLine = true
                            )
                        }
                        FormItem(header = { Text(stringResource(Res.string.publish_derive_link_header)) }) {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = vm.deriveLink,
                                onValueChange = { vm.deriveLink = it.singleLined() },
                                singleLine = true
                            )
                        }
                    }


                }
            }

            Card {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    FormItem(
                        header = {
                            Text(stringResource(Res.string.review_production_crew))
                            IconButton(onClick = { vm.addStaff() }) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.common_add_cd))
                            }
                        },
                        subtitle = { Text(stringResource(Res.string.publish_staff_subtitle)) }
                    ) {
                        vm.staffs.fastForEachIndexed { index, item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = item.role,
                                    modifier = Modifier.width(120.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.name ?: "uid: ${item.uid}",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                IconButton(onClick = { vm.removeStaff(index) }) {
                                    Icon(Icons.Default.Remove, contentDescription = stringResource(Res.string.common_remove_cd))
                                }
                            }
                        }
                    }

                }
            }

            Card {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    FormItem(
                        header = {
                            Text(stringResource(Res.string.review_external_links))

                            IconButton(onClick = { vm.showAddExternalLinkDialog() }) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.common_add_cd))
                            }
                        },
                        subtitle = { Text(stringResource(Res.string.publish_links_subtitle)) }
                    ) {
                        vm.externalLinks.fastForEachIndexed { index, item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    modifier = Modifier.width(120.dp),
                                    text = translatePlatformLabel(item.platform),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = item.url,
                                    overflow = TextOverflow.MiddleEllipsis,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                IconButton(onClick = { vm.removeLink(index) }) {
                                    Icon(Icons.Default.Remove, contentDescription = stringResource(Res.string.common_remove_cd))
                                }
                            }
                        }
                    }

                }
            }

            Card {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    FormItem(
                        header = { Text(stringResource(Res.string.publish_explicit_header)) },
                        subtitle = { Text(stringResource(Res.string.publish_explicit_subtitle)) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = vm.explicit == false,
                                onClick = { vm.explicit = false }
                            ) {
                                Text(stringResource(Res.string.publish_explicit_no))
                            }

                            RadioButton(
                                selected = vm.explicit == true,
                                onClick = { vm.explicit = true }
                            ) {
                                Text(stringResource(Res.string.publish_explicit_yes))
                            }
                        }
                    }
                }
            }

            if (editing) Card {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    FormItem(
                        header = { Text(stringResource(Res.string.publish_modify_comment)) },
                        subtitle = { Text(stringResource(Res.string.publish_modify_comment_supporting)) }
                    ) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = vm.comment,
                            onValueChange = { vm.comment = it },
                            minLines = 3
                        )
                    }
                }
            }

            Button(onClick = { vm.publish() }, enabled = !vm.isOperating) {
                Text(stringResource(if (vm.type == Type.CREATE) Res.string.publish_submit_create else Res.string.publish_submit_edit))
            }
        }
    }

    if (vm.showSuccessDialog) AlertDialog(
        onDismissRequest = { vm.closeDialog() },
        title = {
            if (vm.type == Type.CREATE) {
                Text(stringResource(Res.string.publish_success_title_create))
            } else {
                Text(stringResource(Res.string.publish_success_title_edit))
            }
        },
        text = {
            if (vm.type == Type.CREATE) {
                Text(stringResource(Res.string.publish_success_text_create, vm.publishedSongId.toString()))
            } else {
                Text(stringResource(Res.string.publish_success_text_edit))
            }
        },
        confirmButton = {
            TextButton(onClick = { vm.closeDialog() }) {
                Text(stringResource(Res.string.common_ok))
            }
        }
    )

    AddStaffDialog(vm)
    AddExternalLinkDialog(vm)

    if (vm.showInitJmidDialog) InitJmidDialog(
        onDismissRequest = vm::cancelInitJmid,
        value = vm.initJmidInput,
        onValueChange = vm::updateInitJmidInput,
        valid = vm.initJmidValid,
        supportText = vm.initJmidSupportText,
        onConfirm = vm::confirmInitJmid
    )
}

@Composable
private fun LrcTextField(value: String, onValueChange: (String) -> Unit) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        minLines = 8,
        maxLines = 8,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            color = LocalContentColor.current
        )
    )
}

@Composable
private fun AddStaffDialog(vm: PublishViewModel) {
    if (vm.showAddStaffDialog) AlertDialog(
        modifier = Modifier.width(300.dp),
        onDismissRequest = { vm.cancelAddStaff() },
        title = {
            Text(stringResource(Res.string.publish_add_staff_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                var type by remember { mutableStateOf(0) }

                TextField(
                    value = vm.addStaffRole,
                    onValueChange = { vm.addStaffRole = it.singleLined() },
                    placeholder = { Text(stringResource(Res.string.publish_add_staff_role)) },
                    singleLine = true,
                    supportingText = {
                        Text(stringResource(Res.string.publish_add_staff_role_supporting))
                    }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(selected = type == 0, onClick = { type = 0 }) {
                        Text(text = stringResource(Res.string.publish_add_staff_internal))
                    }
                    RadioButton(selected = type == 1, onClick = {
                        vm.addStaffUid = ""
                        type = 1
                    }) {
                        Text(text = stringResource(Res.string.publish_add_staff_external))
                    }
                }

                if (type == 0) TextField(
                    value = vm.addStaffUid,
                    onValueChange = { vm.addStaffUid = it.singleLined() },
                    placeholder = { Text("UID") },
                    singleLine = true
                )

                if (type == 1) TextField(
                    value = vm.addStaffName,
                    onValueChange = { vm.addStaffName = it.singleLined() },
                    placeholder = { Text(stringResource(Res.string.publish_add_staff_name)) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { vm.confirmAddStaff() },
                enabled = !vm.addStaffOperating && (vm.addStaffRole.isNotBlank() && (vm.addStaffUid.isNotBlank() || vm.addStaffName.isNotBlank()))
            ) {
                Text(stringResource(Res.string.common_add))
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.cancelAddStaff() }) {
                Text(stringResource(Res.string.common_cancel))
            }
        }
    )
}

private val presetPlatforms = listOf("bilibili", "niconico", "youtube", "douyin")

@Composable
private fun AddExternalLinkDialog(vm: PublishViewModel) {
    if (vm.showAddExternalLinkDialog) {
        var platform by remember { mutableStateOf<String?>(null) }
        var value by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<StringResource?>(null) }

        AlertDialog(
            modifier = Modifier.width(300.dp),
            onDismissRequest = { vm.closeAddExternalLinkDialog() },
            title = {
                Text(stringResource(Res.string.publish_add_link_title))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Select(
                        value = platform,
                        onValueChange = { platform = it },
                        options = presetPlatforms,
                    ) {
                        Text(it?.let { translatePlatformLabel(it) } ?: stringResource(Res.string.publish_link_choose_platform))
                    }
                    /*Box {
                        var dropdown by remember { mutableStateOf(false) }
                        TextButton(
                            onClick = { dropdown = true },
                            modifier = Modifier.width(120.dp)
                        ) {
                            Text(
                                platform?.let {
                                    translatePlatformLabel(it)
                                } ?: "选择平台"
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "ArrowDropDown")
                        }
                        fun changePlatform(v: String) {
                            platform = v
                            value = ""
                            dropdown = false
                        }
                        DropdownMenu(dropdown, onDismissRequest = { dropdown = false }) {
                            presetPlatforms.fastForEach {
                                DropdownMenuItem(
                                    text = { Text(translatePlatformLabel(it)) },
                                    onClick = { changePlatform(it) }
                                )
                            }
                        }
                    }*/
                    if (platform == "bilibili") {
                        TextField(
                            modifier = Modifier,
                            value = value,
                            onValueChange = {
                                value = it.singleLined()
                            },
                            placeholder = { Text(stringResource(Res.string.publish_link_bv_placeholder)) },
                            singleLine = true,
                            supportingText = {
                                error?.let { error ->
                                    Text(stringResource(error), color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                    } else if (platform == "niconico") {
                        TextField(
                            modifier = Modifier,
                            value = value,
                            onValueChange = { value = it.singleLined() },
                            placeholder = { Text(stringResource(Res.string.publish_link_sm_placeholder)) },
                            singleLine = true,
                            supportingText = {
                                error?.let { error ->
                                    Text(stringResource(error), color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                    } else TextField(
                        modifier = Modifier,
                        value = value,
                        onValueChange = { value = it.singleLined() },
                        placeholder = { Text(stringResource(Res.string.publish_link_url_placeholder)) },
                        singleLine = true,
                        supportingText = {
                            error?.let { error ->
                                Text(stringResource(error), color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val platform = platform
                        val value = value
                        var link = value

                        if (platform == null) {
                            error = Res.string.publish_link_platform_required
                            return@TextButton
                        }

                        when (platform) {
                            "bilibili" -> {
                                if (link.matches("""BV[1-9A-HJ-NP-Za-km-z]{10}""".toRegex())) {
                                    link = "https://www.bilibili.com/video/$value"
                                } else {
                                    error = Res.string.publish_link_invalid_bv
                                    return@TextButton
                                }
                            }

                            "niconico" -> {
                                if (link.matches("""sm\d+""".toRegex())) {
                                    link = "https://www.nicovideo.jp/watch/$value"
                                } else {
                                    error = Res.string.publish_link_invalid_sm
                                    return@TextButton
                                }
                            }

                            else -> {
                                if (!link.startsWith("https://")) {
                                    error = Res.string.publish_link_https_required
                                    return@TextButton
                                }
                            }
                        }

                        vm.addLink(platform, link)
                        vm.closeAddExternalLinkDialog()
                    },
                ) {
                    Text(stringResource(Res.string.common_add))
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.closeAddExternalLinkDialog() }) {
                    Text(stringResource(Res.string.common_cancel))
                }
            }
        )
    }
}

@Stable
@Composable
fun translatePlatformLabel(label: String): String = when (label) {
    "bilibili" -> stringResource(Res.string.platform_bilibili)
    "douyin" -> stringResource(Res.string.platform_douyin)
    "youtube" -> stringResource(Res.string.platform_youtube)
    "niconico" -> "niconico"
    else -> label
}