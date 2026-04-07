package world.hachimi.app.ui.contributor

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.artwork_details_title
import hachimiworld.composeapp.generated.resources.common_delete
import hachimiworld.composeapp.generated.resources.common_more
import hachimiworld.composeapp.generated.resources.contributor_submitter
import hachimiworld.composeapp.generated.resources.review_approve
import hachimiworld.composeapp.generated.resources.review_audio
import hachimiworld.composeapp.generated.resources.review_cover
import hachimiworld.composeapp.generated.resources.review_creation_type
import hachimiworld.composeapp.generated.resources.review_creation_type_derivation
import hachimiworld.composeapp.generated.resources.review_creation_type_derivation_of_derivation
import hachimiworld.composeapp.generated.resources.review_creation_type_original
import hachimiworld.composeapp.generated.resources.review_decision_comment
import hachimiworld.composeapp.generated.resources.review_description_label
import hachimiworld.composeapp.generated.resources.review_detail_title
import hachimiworld.composeapp.generated.resources.review_discussion_edited
import hachimiworld.composeapp.generated.resources.review_discussion_empty
import hachimiworld.composeapp.generated.resources.review_discussion_input_placeholder
import hachimiworld.composeapp.generated.resources.review_discussion_input_supporting
import hachimiworld.composeapp.generated.resources.review_discussion_send
import hachimiworld.composeapp.generated.resources.review_discussion_sending
import hachimiworld.composeapp.generated.resources.review_discussion_title
import hachimiworld.composeapp.generated.resources.review_discussion_unknown_author
import hachimiworld.composeapp.generated.resources.review_download_audio
import hachimiworld.composeapp.generated.resources.review_duration
import hachimiworld.composeapp.generated.resources.review_empty_value
import hachimiworld.composeapp.generated.resources.review_explicit
import hachimiworld.composeapp.generated.resources.review_explicit_no
import hachimiworld.composeapp.generated.resources.review_explicit_unmarked
import hachimiworld.composeapp.generated.resources.review_explicit_yes
import hachimiworld.composeapp.generated.resources.review_external_links
import hachimiworld.composeapp.generated.resources.review_id_label
import hachimiworld.composeapp.generated.resources.review_lyrics
import hachimiworld.composeapp.generated.resources.review_modify_draft
import hachimiworld.composeapp.generated.resources.review_origin_artist
import hachimiworld.composeapp.generated.resources.review_origin_link
import hachimiworld.composeapp.generated.resources.review_origin_title
import hachimiworld.composeapp.generated.resources.review_origin_type
import hachimiworld.composeapp.generated.resources.review_origin_type_derivation
import hachimiworld.composeapp.generated.resources.review_origin_type_original
import hachimiworld.composeapp.generated.resources.review_production_crew
import hachimiworld.composeapp.generated.resources.review_reject
import hachimiworld.composeapp.generated.resources.review_status
import hachimiworld.composeapp.generated.resources.review_status_approved
import hachimiworld.composeapp.generated.resources.review_status_pending
import hachimiworld.composeapp.generated.resources.review_status_rejected
import hachimiworld.composeapp.generated.resources.review_submit_time
import hachimiworld.composeapp.generated.resources.review_subtitle_label
import hachimiworld.composeapp.generated.resources.review_tags
import hachimiworld.composeapp.generated.resources.review_time
import hachimiworld.composeapp.generated.resources.review_title_label
import hachimiworld.composeapp.generated.resources.review_unknown
import hachimiworld.composeapp.generated.resources.review_view_history
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.api.module.PublishModule.SongPublishReviewBrief.Companion.STATUS_PENDING
import world.hachimi.app.getPlatform
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.ReviewDetailViewModel
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.Pagination
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.DropdownMenu
import world.hachimi.app.ui.design.components.DropdownMenuItem
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.LocalTextStyle
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.player.fullscreen.components.AmbientUserChip
import world.hachimi.app.util.AdaptiveScreenMargin
import world.hachimi.app.util.fillMaxWidthIn
import world.hachimi.app.util.formatSongDuration
import world.hachimi.app.util.formatTime
import kotlin.time.Duration.Companion.seconds

@Composable
fun ReviewDetailScreen(
    reviewId: Long,
    source: ReviewScreenSource,
    vm: ReviewDetailViewModel = koinViewModel()
) {
    DisposableEffect(vm, reviewId) {
        vm.mounted(reviewId)
        onDispose { vm.dispose() }
    }

    AnimatedContent(vm.initializeStatus) {
        when (it) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.refresh() })
            InitializeStatus.LOADED -> Content(vm, source = source)
        }
    }
}

@Composable
private fun Content(
    vm: ReviewDetailViewModel,
    source: ReviewScreenSource,
    global: GlobalStore = koinInject()
) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .fillMaxWidthIn()
            .navigationBarsPadding()
            .padding(LocalContentInsets.current.asPaddingValues())
            .padding(AdaptiveScreenMargin),
        Arrangement.spacedBy(16.dp)
    ) {
        vm.data?.let { data ->
            // Review Metadata
            ReviewMetadataSection(global, data, source)

            // Modify Button
            if (
                source == ReviewScreenSource.CREATION &&
                data.status != PublishModule.SongPublishReviewBrief.STATUS_APPROVED
            ) {
                Button(onClick = {
                    global.nav.push(Route.Root.CreationCenter.ReviewModify(data.reviewId))
                }) {
                    Text(stringResource(Res.string.review_modify_draft))
                }
            }

            // Review Action
            if (data.status == STATUS_PENDING && vm.isContributor) Column {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.reviewCommentInput,
                    onValueChange = { vm.reviewCommentInput = it },
                    minLines = 3,
                )
                Row {
                    Button(onClick = { vm.approve() }, enabled = !vm.operating) {
                        Text(stringResource(Res.string.review_approve))
                    }
                    Spacer(Modifier.width(12.dp))
                    TextButton(onClick = { vm.reject() }, enabled = !vm.operating) {
                        Text(stringResource(Res.string.review_reject))
                    }
                }
            }

            // Artwork Details
            ArtworkDetailSection(data, vm)

            ReviewDiscussionSection(vm = vm, global = global)
        }
    }
}

@Composable
private fun ArtworkDetailSection(
    data: PublishModule.SongPublishReviewData,
    vm: ReviewDetailViewModel
) {
    Text(
        stringResource(Res.string.artwork_details_title),
        style = MaterialTheme.typography.titleLarge
    )

    PropertyItem(stringResource(Res.string.review_id_label), data.displayId)
    PropertyItem({ Text(stringResource(Res.string.review_audio)) }) {
        Button(onClick = { vm.download() }) {
            Text(
                stringResource(Res.string.review_download_audio),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
    PropertyItem({ Text(stringResource(Res.string.review_cover)) }) {
        Surface(Modifier.size(120.dp)) {
            AsyncImage(
                ImageRequest.Builder(LocalPlatformContext.current)
                    .httpHeaders(CoilHeaders)
                    .data(data.coverUrl)
                    .crossfade(true)
                    .build(), null, Modifier.size(120.dp)
            )
        }
    }
    PropertyItem(stringResource(Res.string.review_title_label), data.title)
    PropertyItem(stringResource(Res.string.review_subtitle_label), data.subtitle)
    PropertyItem(stringResource(Res.string.review_description_label), data.description)
    PropertyItem(
        stringResource(Res.string.review_duration),
        formatSongDuration(data.durationSeconds.seconds)
    )
    PropertyItem(
        stringResource(Res.string.review_creation_type), when (data.creationType) {
            PublishModule.SongPublishReviewData.CREATION_TYPE_ORIGINAL -> stringResource(Res.string.review_creation_type_original)
            PublishModule.SongPublishReviewData.CREATION_TYPE_DERIVATION -> stringResource(Res.string.review_creation_type_derivation)
            PublishModule.SongPublishReviewData.CREATION_TYPE_DERIVATION_OF_DERIVATION -> stringResource(
                Res.string.review_creation_type_derivation_of_derivation
            )

            else -> stringResource(Res.string.review_unknown)
        }
    )
    PropertyItem(
        stringResource(Res.string.review_explicit), when (data.explicit) {
            true -> stringResource(Res.string.review_explicit_yes)
            false -> stringResource(Res.string.review_explicit_no)
            null -> stringResource(Res.string.review_explicit_unmarked)
        }
    )
    data.originInfos.fastForEach {
        PropertyItem(
            stringResource(Res.string.review_origin_type), when (it.originType) {
                PublishModule.SongPublishReviewData.CREATION_TYPE_ORIGINAL -> stringResource(Res.string.review_origin_type_original)
                PublishModule.SongPublishReviewData.CREATION_TYPE_DERIVATION -> stringResource(Res.string.review_origin_type_derivation)
                else -> stringResource(Res.string.review_unknown)
            }
        )
        PropertyItem(
            stringResource(Res.string.review_origin_title),
            it.title ?: stringResource(Res.string.review_empty_value)
        )
        PropertyItem(
            stringResource(Res.string.review_origin_artist),
            it.artist ?: stringResource(Res.string.review_empty_value)
        )
        PropertyItem({ Text(stringResource(Res.string.review_origin_link)) }) {
            Text(
                modifier = Modifier.clickable {
                    it.url?.let {
                        getPlatform().openUrl(it)
                    }
                },
                text = it.url ?: stringResource(Res.string.review_empty_value),
                textDecoration = if (it.url != null) TextDecoration.Underline else null
            )
        }
    }

    PropertyItem({ Text(stringResource(Res.string.review_tags)) }) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            data.tags.fastForEach {
                Text(it.name)
            }
        }
    }

    PropertyItem({ Text(stringResource(Res.string.review_production_crew)) }) {
        data.productionCrew.fastForEach {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(it.role, style = MaterialTheme.typography.labelMedium)
                Text(it.uid.toString())
                Text(it.personName.toString())
            }
        }
    }

    PropertyItem(stringResource(Res.string.review_lyrics), data.lyrics)

    PropertyItem({ Text(stringResource(Res.string.review_external_links)) }) {
        data.externalLink.fastForEach {
            Text(it.platform, style = MaterialTheme.typography.labelMedium)

            SelectionContainer {
                Text(it.url, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ReviewMetadataSection(
    global: GlobalStore,
    data: PublishModule.SongPublishReviewData,
    source: ReviewScreenSource
) {
    Text(
        text = stringResource(Res.string.review_detail_title),
        style = MaterialTheme.typography.titleLarge
    )
    PropertyItem(label = {
        Text(stringResource(Res.string.contributor_submitter))
    }) {
        Text(
            modifier = Modifier.clickable {
                global.nav.push(Route.Root.PublicUserSpace(data.uploaderUid))
            },
            text = data.uploaderName + " " + data.uploaderUid
        )
    }
    PropertyItem({ Text(stringResource(Res.string.review_submit_time)) }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(formatTime(data.submitTime, distance = true, precise = false, thresholdDay = 3))
            TextButton(onClick = {
                when (source) {
                    ReviewScreenSource.CREATION -> global.nav.push(
                        Route.Root.CreationCenter.ReviewHistory(
                            data.reviewId
                        )
                    )

                    ReviewScreenSource.CONTRIBUTOR -> global.nav.push(
                        Route.Root.ContributorCenter.ReviewHistory(
                            data.reviewId
                        )
                    )
                }
            }) {
                Text(stringResource(Res.string.review_view_history))
            }
        }
    }
    PropertyItem(
        stringResource(Res.string.review_status), when (data.status) {
            STATUS_PENDING -> stringResource(Res.string.review_status_pending)
            PublishModule.SongPublishReviewBrief.STATUS_APPROVED -> stringResource(Res.string.review_status_approved)
            PublishModule.SongPublishReviewBrief.STATUS_REJECTED -> stringResource(Res.string.review_status_rejected)
            else -> error("unreachable")
        }
    )

    PropertyItem(
        stringResource(Res.string.review_decision_comment),
        data.reviewComment ?: stringResource(Res.string.review_empty_value)
    )
    PropertyItem(
        stringResource(Res.string.review_time),
        data.reviewTime?.let { formatTime(it, distance = true, precise = false, thresholdDay = 3) }
            ?: stringResource(Res.string.review_empty_value)
    )
}

@Composable
private fun ReviewDiscussionSection(
    vm: ReviewDetailViewModel,
    global: GlobalStore,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.review_discussion_title, vm.commentTotal),
                style = MaterialTheme.typography.titleLarge
            )
            if (vm.commentsLoading) {
                CircularProgressIndicator(Modifier.size(18.dp))
            }
        }

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = vm.commentInput,
            onValueChange = { vm.commentInput = it },
            placeholder = { Text(stringResource(Res.string.review_discussion_input_placeholder)) },
            supportingText = { Text(stringResource(Res.string.review_discussion_input_supporting)) },
            minLines = 3,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { vm.createComment() },
                enabled = !vm.commentsSubmitting
            ) {
                if (vm.commentsSubmitting) {
                    CircularProgressIndicator(Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.review_discussion_sending))
                } else {
                    Text(stringResource(Res.string.review_discussion_send))
                }
            }
        }

        when {
            vm.comments.isEmpty() && !vm.commentsLoading -> {
                Text(
                    text = stringResource(Res.string.review_discussion_empty),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    vm.comments.fastForEach { item ->
                        ReviewDiscussionItem(
                            item = item,
                            deleting = vm.deletingCommentId == item.id,
                            canDelete = vm.canDeleteComment(item),
                            onDelete = { vm.deleteComment(item.id) },
                            onNavToUserClick = { uid ->
                                global.nav.push(Route.Root.PublicUserSpace(uid))
                            }
                        )
                    }
                }
            }
        }

        if (vm.commentTotal > vm.commentPageSize) {
            Pagination(
                modifier = Modifier.fillMaxWidth(),
                total = vm.commentTotal.toInt(),
                pageIndex = vm.commentPageIndex,
                pageSize = vm.commentPageSize,
                onPageChange = { pageIndex, pageSize -> vm.updateCommentPage(pageIndex = pageIndex, pageSize = pageSize) },
            )
        }
    }
}

@Composable
private fun ReviewDiscussionItem(
    item: PublishModule.ReviewCommentItem,
    canDelete: Boolean,
    deleting: Boolean,
    onDelete: () -> Unit,
    onNavToUserClick: (Long) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val author = item.author
                if (author != null) {
                    val avatar = author.avatarUrl?.let {
                        rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalPlatformContext.current)
                                .httpHeaders(CoilHeaders)
                                .crossfade(true)
                                .data(it)
                                .build()
                        )
                    }
                    AmbientUserChip(
                        onClick = { onNavToUserClick(author.uid) },
                        avatar = avatar,
                        name = author.username
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.review_discussion_unknown_author),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = buildString {
                        append(formatTime(item.createTime, distance = true, precise = false, thresholdDay = 3))
                        if (item.updateTime != item.createTime) {
                            append(" · ")
                            append(stringResource(Res.string.review_discussion_edited))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                )

                if (canDelete) {
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        if (deleting) {
                            CircularProgressIndicator(Modifier.size(14.dp))
                        } else {
                            HachimiIconButton(onClick = { expanded = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = stringResource(Res.string.common_more)
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    onClick = {
                                        expanded = false
                                        onDelete()
                                    },
                                    text = { Text(stringResource(Res.string.common_delete)) }
                                )
                            }
                        }
                    }
                }
            }

            SelectionContainer {
                Text(text = item.content, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun PropertyItem(
    label: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
            label()
        }
        Spacer(Modifier.height(8.dp))
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodySmall.copy(
                color = LocalContentColor.current.copy(
                    0.7f
                )
            )
        ) {
            content()
        }
    }
}

@Composable
private fun PropertyItem(
    label: String,
    content: String
) {
    PropertyItem(label = { Text(label) }, content = { Text(content) })
}