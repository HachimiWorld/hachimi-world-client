package world.hachimi.app.ui.likes

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_empty
import hachimiworld.composeapp.generated.resources.common_play_cd
import hachimiworld.composeapp.generated.resources.nav_recent_like
import hachimiworld.composeapp.generated.resources.play_all
import hachimiworld.composeapp.generated.resources.player_unlike
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.RecentLikeViewModel
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.AdaptiveScreenMargin
import world.hachimi.app.util.YMDHM
import world.hachimi.app.util.contentPaddingForMaxWidth
import world.hachimi.app.util.formatDaysDistance
import world.hachimi.app.util.formatSongDuration
import world.hachimi.app.util.formatTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Composable
fun RecentLikeScreen(
	vm: RecentLikeViewModel = koinViewModel(),
) {
	DisposableEffect(vm) {
		vm.mounted()
		onDispose { vm.dispose() }
	}

	AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) {
		when (it) {
			InitializeStatus.INIT -> LoadingPage()
			InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
			InitializeStatus.LOADED -> Content(vm)
		}
	}
}

@Composable
private fun Content(vm: RecentLikeViewModel) {
	val state = rememberLazyListState()

	LaunchedEffect(state.canScrollForward, vm.loading, vm.hasMore) {
		if (!vm.loading && vm.hasMore && !state.canScrollForward) {
			vm.loadMore()
		}
	}

	if (vm.songs.isEmpty()) {
		Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
			Text(stringResource(Res.string.common_empty))
		}
		return
	}

	BoxWithConstraints {
		LazyColumn(
			state = state,
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPaddingForMaxWidth(PaddingValues(AdaptiveScreenMargin), maxWidth),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			item {
				Header(vm)
			}

			vm.songs.forEach { group ->
				item(key = group.date.toString(), contentType = "separator") {
					Text(
						text = remember(group.date) {
							val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
							val daysOffset = today.toEpochDays() - group.date.toEpochDays()
							formatDaysDistance(daysOffset.toInt()) ?: group.date.toString()
						},
						style = MaterialTheme.typography.titleMedium,
					)
				}
				items(
					items = group.songs,
					key = { item -> "${item.songData.id}-${item.likedTime}" },
				) { item ->
					RecentLikeItem(
						item = item,
						onClick = { vm.play(item) },
						onUnlikeClick = { vm.unlike(item) },
						unlikeEnabled = !vm.isUnliking(item.songData.id),
					)
				}
			}

			item(contentType = "load_more") {
				LoadMoreItem(hasMore = vm.hasMore, isLoading = vm.loading)
			}

			item {
				Spacer(Modifier.navigationBarsPadding().padding(LocalContentInsets.current.asPaddingValues()))
			}
		}
	}
}

@Composable
private fun Header(vm: RecentLikeViewModel) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = stringResource(Res.string.nav_recent_like),
			style = MaterialTheme.typography.titleLarge,
		)
		Spacer(Modifier.weight(1f))
		Button(onClick = { vm.playAllLoaded() }) {
			Icon(Icons.Default.PlayArrow, contentDescription = stringResource(Res.string.common_play_cd))
			Spacer(Modifier.size(8.dp))
			Text(stringResource(Res.string.play_all))
		}
	}
}

@Composable
private fun RecentLikeItem(
	item: SongModule.MyLikeItem,
	onClick: () -> Unit,
	onUnlikeClick: () -> Unit,
	unlikeEnabled: Boolean,
	modifier: Modifier = Modifier,
) {
	Surface(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(16.dp),
	) {
		Row(
			modifier = Modifier
				.clickable(onClick = onClick)
				.padding(horizontal = 8.dp, vertical = 8.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Box(
				Modifier
					.size(48.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(MaterialTheme.colorScheme.onSurface.copy(0.12f))
			) {
				AsyncImage(
					modifier = Modifier.fillMaxSize(),
					model = ImageRequest.Builder(LocalPlatformContext.current)
						.httpHeaders(CoilHeaders)
						.data(item.songData.coverUrl)
						.crossfade(true)
						.build(),
					contentDescription = item.songData.title,
					contentScale = ContentScale.Crop,
				)
			}

			Column(Modifier.weight(1f)) {
				Text(
					text = item.songData.title,
					style = MaterialTheme.typography.bodyMedium,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				Text(
					text = item.songData.uploaderName,
					style = MaterialTheme.typography.bodySmall,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}

			Column(horizontalAlignment = Alignment.End) {
				Text(
					text = formatSongDuration(item.songData.durationSeconds.seconds),
					style = MaterialTheme.typography.bodySmall,
				)
				Text(
					text = formatTime(item.likedTime, distance = true, precise = false, fullFormat = LocalDateTime.Formats.YMDHM),
					style = MaterialTheme.typography.labelSmall,
				)
			}

			HachimiIconButton(onClick = onUnlikeClick, enabled = unlikeEnabled) {
				Icon(
					imageVector = Icons.Default.Favorite,
					contentDescription = stringResource(Res.string.player_unlike),
					tint = HachimiTheme.colorScheme.primary,
				)
			}
		}
	}
}

@Preview
@Composable
private fun RecentLikeItemPreview() {
	PreviewTheme(background = false) {
		RecentLikeItem(
			item = SongModule.MyLikeItem(
				songData = SongModule.PublicSongDetail(
					id = 1,
					displayId = "1",
					title = "Test Song Title",
					subtitle = "Test Subtitle",
					description = "This is a preview description.",
					durationSeconds = 185,
					tags = listOf(),
					lyrics = "",
					audioUrl = "",
					coverUrl = "https://example.com/cover.jpg",
					productionCrew = listOf(),
					creationType = 0,
					originInfos = listOf(),
					uploaderUid = 42,
					uploaderName = "Preview Artist",
					playCount = 12345,
					likeCount = 678,
					externalLinks = listOf(),
					createTime = Instant.parse("2024-01-01T00:00:00Z"),
					releaseTime = Instant.parse("2024-01-02T00:00:00Z"),
					explicit = false,
					gain = null,
				),
				likedTime = Instant.parse("2024-01-03T12:00:00Z"),
			),
			onClick = {},
			onUnlikeClick = {},
			unlikeEnabled = true,
			modifier = Modifier.fillMaxWidth(),
		)
	}
}

