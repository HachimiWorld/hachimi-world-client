package world.hachimi.app.ui.comment

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.comment_header
import hachimiworld.composeapp.generated.resources.comment_page_title
import hachimiworld.composeapp.generated.resources.common_play_cd
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.CommentModule
import world.hachimi.app.model.CommentTarget
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.SongCommentsViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.component.TitleStyle
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.placeholder
import world.hachimi.app.ui.message.components.RemoteImage
import world.hachimi.app.ui.util.listTailPadding

/**
 * A song's comments, as a page of its own.
 *
 * @since 261031
 */
@Composable
fun SongCommentsScreen(
    songDisplayId: String,
    vm: SongCommentsViewModel = koinViewModel(),
) {
    val navigator = LocalNavigator.current
    // In the first composition, so a song the player already has is there from the first frame
    remember(songDisplayId, vm) { vm.mounted(songDisplayId) }

    val song = vm.song?.takeIf { it.displayId == songDisplayId }
    if (song == null && vm.status == InitializeStatus.FAILED) {
        ScreenScaffold(
            titleStyle = TitleStyle.Compact,
            title = { Text(stringResource(Res.string.comment_page_title), maxLines = 1) },
            showBack = true,
            onBack = navigator::back,
        ) {
            CommentsLoadState(status = InitializeStatus.FAILED, onRetry = vm::retry, modifier = Modifier.listTailPadding())
        }
        return
    }
    // The page is up while the song loads, so its header and placeholders don't show twice
    CommentsPage(
        target = song?.let { CommentTarget(CommentModule.TARGET_SONG, it.id, it.uploaderUid) },
        title = { total ->
            if (total > 0) stringResource(Res.string.comment_header, total.toString())
            else stringResource(Res.string.comment_page_title)
        },
        onBack = navigator::back,
        header = {
            Crossfade(song) { shown ->
                if (shown == null) SongRowSkeleton()
                else SongRow(shown.title, shown.uploaderName, shown.coverUrl, onClick = vm::playSong)
            }
        },
    )
}

/** [SongRow]'s shape while the song loads. */
@Composable
private fun SongRowSkeleton() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HachimiTheme.colorScheme.onSurface.copy(0.04f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).placeholder(true, RoundedCornerShape(8.dp)))
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            // The two lines of text, 14sp/20 and 12sp/16 with 2dp between
            Box(Modifier.height(20.dp), contentAlignment = Alignment.CenterStart) {
                Box(Modifier.fillMaxWidth(0.5f).height(12.dp).placeholder(true))
            }
            Box(Modifier.padding(top = 2.dp).height(16.dp), contentAlignment = Alignment.CenterStart) {
                Box(Modifier.width(64.dp).height(10.dp).placeholder(true))
            }
        }
    }
}

@Composable
private fun SongRow(title: String, author: String, coverUrl: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HachimiTheme.colorScheme.onSurface.copy(0.04f))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RemoteImage(coverUrl, Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)))
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                author,
                modifier = Modifier.padding(top = 2.dp),
                fontSize = 12.sp,
                color = HachimiTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            Icons.Default.PlayArrow,
            contentDescription = stringResource(Res.string.common_play_cd),
            modifier = Modifier.padding(end = 4.dp).size(20.dp),
            tint = HachimiTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PreviewSongRow() {
    SongRow("曼波小队", "基米观察员", "", onClick = {})
}

@Preview(widthDp = 360, heightDp = 800, name = "Page · Compact")
@Composable
private fun SongCommentsCompactPreview() {
    PreviewCommentsPage { PreviewSongRow() }
}

@Preview(widthDp = 360, heightDp = 800, name = "Page · Compact · Dark")
@Composable
private fun SongCommentsCompactDarkPreview() {
    PreviewCommentsPage(darkTheme = true) { PreviewSongRow() }
}

@Preview(widthDp = 1280, heightDp = 800, name = "Page · Expanded")
@Composable
private fun SongCommentsExpandedPreview() {
    PreviewCommentsPage(width = 1280.dp) { PreviewSongRow() }
}

@Preview(widthDp = 360, heightDp = 800, name = "Page · Loading")
@Composable
private fun SongCommentsLoadingPreview() {
    PreviewCommentsPage(title = "评论", status = InitializeStatus.INIT, empty = true) { PreviewSongRow() }
}

@Preview(widthDp = 360, heightDp = 800, name = "Page · Song loading")
@Composable
private fun SongCommentsSongLoadingPreview() {
    PreviewCommentsPage(title = "评论", status = InitializeStatus.INIT, empty = true) { SongRowSkeleton() }
}

@Preview(widthDp = 360, heightDp = 800, name = "Page · Empty")
@Composable
private fun SongCommentsEmptyPreview() {
    PreviewCommentsPage(title = "评论", empty = true) { PreviewSongRow() }
}

@Preview(widthDp = 360, heightDp = 800, name = "Page · Load failed")
@Composable
private fun SongCommentsFailedPreview() {
    PreviewCommentsPage(title = "评论", status = InitializeStatus.FAILED, empty = true) { PreviewSongRow() }
}
