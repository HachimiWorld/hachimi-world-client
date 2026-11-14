package world.hachimi.app.ui.likes

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.DpSize
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.component.PageTitleAction
import world.hachimi.app.ui.component.PlayAllCovers
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.component.SongListSkeleton
import world.hachimi.app.ui.component.LocalPageTitleShown
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import hachimiworld.composeapp.generated.resources.nav_recent_like
import hachimiworld.composeapp.generated.resources.player_unlike
import hachimiworld.composeapp.generated.resources.unavailable_song
import hachimiworld.composeapp.generated.resources.unavailable_unlike
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.model.RecentLikeViewModel
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.component.titleActionPlaced
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.insets.multiplatformStatusBarsPadding
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.InitStatusScaffold
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.ui.util.contentPaddingForMaxWidth
import world.hachimi.app.ui.util.listTailSpacerItem
import world.hachimi.app.util.formatDaysDistance
import world.hachimi.app.util.formatSongDuration
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

	InitStatusScaffold(
		initializeStatus = vm.initializeStatus,
		isLoading = vm.loading,
		onRetryClick = { vm.retry() },
		modifier = Modifier.fillMaxSize(),
		// Under a phone scaffold the button sits by the title instead
		initPage = { SongListSkeleton(playAll = !titleActionPlaced, dateLabel = true) },
	) {
		Content(vm)
	}
}

@Composable
private fun Content(vm: RecentLikeViewModel) {
	RecentLikeList(
		groups = vm.songs,
		total = vm.total,
		rhythm = vm.rhythm,
		hasMore = vm.hasMore,
		loading = vm.loading,
		isUnliking = vm::isUnliking,
		onPlay = vm::play,
		onPlayAll = vm::playAllLoaded,
		onUnlike = vm::unlike,
		onLoadMore = vm::loadMore,
	)
}

/**
 * The likes as they're shown, from what's loaded: the play-all (by the page title when a phone
 * scaffold holds it, otherwise in a row of its own), the [LikeSummary], then the likes day by day.
 */
@Composable
private fun RecentLikeList(
	groups: List<RecentLikeViewModel.SongGroup>,
	total: Long?,
	rhythm: RecentLikeViewModel.LikeRhythm?,
	hasMore: Boolean,
	loading: Boolean,
	isUnliking: (songId: Long) -> Boolean,
	onPlay: (SongModule.MyLikeItem) -> Unit,
	onPlayAll: () -> Unit,
	onUnlike: (songId: Long) -> Unit,
	onLoadMore: () -> Unit,
) {
	val state = rememberLazyListState()

	LaunchedEffect(state.canScrollForward, loading, hasMore) {
		if (!loading && hasMore && !state.canScrollForward) {
			onLoadMore()
		}
	}

	if (groups.isEmpty()) {
		Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
			Text(stringResource(Res.string.common_empty))
		}
		return
	}

	val covers = remember(groups) {
		groups.asSequence()
			.flatMap { it.entries }
			.filterIsInstance<RecentLikeViewModel.LikeEntry.Song>()
			.map { it.item.songData.coverUrl }
			.take(3)
			.toList()
	}
	val playAllInTitle = PageTitleAction { folded ->
		PlayAllCovers(covers = covers, folded = folded, onClick = onPlayAll)
	}
	val scope = rememberCoroutineScope()

	BoxWithConstraints {
		LazyColumn(
			state = state,
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPaddingForMaxWidth(PaddingValues(AdaptiveScreenMargin), maxWidth),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			if (!playAllInTitle) item {
				Header(covers, onPlayAll)
			}
			item(contentType = "summary") {
				LikeSummary(
					total = total,
					rhythm = rhythm,
					onDayClick = { day ->
						// Each day is its date line and then its songs
						var index = if (playAllInTitle) 1 else 2
						for (group in groups) {
							if (group.date == day) {
								scope.launch { state.animateScrollToItem(index) }
								break
							}
							index += 1 + group.entries.size
						}
					},
				)
			}

			groups.forEach { group ->
				item(key = group.date.toString(), contentType = "separator") {
					val daysOffset = remember(group.date) {
						val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
						(today.toEpochDays() - group.date.toEpochDays()).toInt()
					}
					Text(
						text = formatDaysDistance(daysOffset) ?: group.date.toString(),
						style = MaterialTheme.typography.titleSmall,
					)
				}
				items(
					items = group.entries,
					key = { entry -> "${entry.songId}-${entry.likedTime}" },
				) { entry ->
					when (entry) {
						is RecentLikeViewModel.LikeEntry.Song -> RecentLikeItem(
							item = entry.item,
							onClick = { onPlay(entry.item) },
							onUnlikeClick = { onUnlike(entry.songId) },
							unlikeEnabled = !isUnliking(entry.songId),
						)
						is RecentLikeViewModel.LikeEntry.Unavailable -> UnavailableLikeItem(
							onUnlikeClick = { onUnlike(entry.songId) },
							unlikeEnabled = !isUnliking(entry.songId),
						)
					}
				}
			}

			item(contentType = "load_more") {
				LoadMoreItem(hasMore = hasMore, isLoading = loading)
			}

			listTailSpacerItem()
		}
	}
}

@Composable
private fun Header(covers: List<String>, onPlayAll: () -> Unit) {
	Row(
		modifier = Modifier.fillMaxWidth().multiplatformStatusBarsPadding(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		if (!LocalPageTitleShown.current) Text(
			text = stringResource(Res.string.nav_recent_like),
			style = MaterialTheme.typography.titleLarge,
		)
		Spacer(Modifier.weight(1f))
		PlayAllCovers(covers = covers, folded = false, onClick = onPlayAll)
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

/** In place of a liked song that was deleted or hidden. */
@Composable
private fun UnavailableLikeItem(
	onUnlikeClick: () -> Unit,
	unlikeEnabled: Boolean,
	modifier: Modifier = Modifier,
) {
	Surface(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(16.dp),
	) {
		Row(
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Box(
				Modifier
					.size(48.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(MaterialTheme.colorScheme.onSurface.copy(0.06f)),
				contentAlignment = Alignment.Center,
			) {
				Icon(Icons.Default.MusicOff, contentDescription = null, tint = LocalContentColor.current.copy(0.4f))
			}
			Text(
				text = stringResource(Res.string.unavailable_song),
				modifier = Modifier.weight(1f),
				style = MaterialTheme.typography.bodyMedium,
				color = LocalContentColor.current.copy(0.5f),
			)
			HachimiIconButton(onClick = onUnlikeClick, enabled = unlikeEnabled) {
				Icon(
					imageVector = Icons.Default.Favorite,
					contentDescription = stringResource(Res.string.unavailable_unlike),
					tint = HachimiTheme.colorScheme.primary,
				)
			}
		}
	}
}

private fun previewLike(id: Long, title: String, artist: String, likedTime: Instant) = SongModule.MyLikeItem(
	songData = SongModule.PublicSongDetail(
		id = id,
		displayId = id.toString(),
		title = title,
		subtitle = "",
		description = "",
		durationSeconds = 120 + (id * 37 % 120).toInt(),
		tags = listOf(),
		lyrics = "",
		audioUrl = "",
		coverUrl = "https://example.com/cover/$id.jpg",
		productionCrew = listOf(),
		creationType = 0,
		originInfos = listOf(),
		uploaderUid = 42,
		uploaderName = artist,
		playCount = 12345,
		likeCount = 678,
		externalLinks = listOf(),
		createTime = Instant.parse("2024-01-01T00:00:00Z"),
		releaseTime = Instant.parse("2024-01-02T00:00:00Z"),
		explicit = false,
		gain = null,
	),
	likedTime = likedTime,
)

/** Likes on a few of the last 30 days, grouped the way the view model groups them. */
private fun previewGroups(): List<RecentLikeViewModel.SongGroup> {
	val now = Clock.System.now()
	val zone = TimeZone.currentSystemDefault()
	val likes = listOf(0, 0, 4, 6, 6, 13, 21, 24).mapIndexed { i, daysAgo ->
		previewLike(i + 1L, listOf("哈基米是猫", "Blue and White Porcelain", "辞基米回忆", "基琶曲", "Love<3#002", "Brightness", "Mambo Of Life", "Practice")[i], "Preview Artist", now - (daysAgo * 24).hours)
	}
	return likes.groupBy { it.likedTime.toLocalDateTime(zone).date }
		.map { (date, items) -> RecentLikeViewModel.SongGroup(date, items.map { RecentLikeViewModel.LikeEntry.Song(it) }) }
		.sortedByDescending { it.date }
}

private fun previewRhythm(groups: List<RecentLikeViewModel.SongGroup>): RecentLikeViewModel.LikeRhythm {
	val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
	val first = today.minus(29, DateTimeUnit.DAY)
	val byDay = groups.associate { it.date to it.entries.size }
	return RecentLikeViewModel.LikeRhythm(first, List(30) { byDay[first.plus(it, DateTimeUnit.DAY)] ?: 0 })
}

@Composable
private fun PreviewRecentLikeList() {
	val groups = remember { previewGroups() }
	RecentLikeList(
		groups = groups,
		total = 73,
		rhythm = remember(groups) { previewRhythm(groups) },
		hasMore = true,
		loading = false,
		isUnliking = { false },
		onPlay = {},
		onPlayAll = {},
		onUnlike = {},
		onLoadMore = {},
	)
}

@Preview(widthDp = 360, heightDp = 800, name = "Phone · under Me")
@Composable
private fun RecentLikePhonePreview() {
	PreviewTheme(background = true) {
		CompositionLocalProvider(LocalWindowSize provides DpSize(WindowSize.COMPACT - 1.dp, 800.dp)) {
			ScreenScaffold(title = { Text(stringResource(Res.string.nav_recent_like)) }, showBack = true, onBack = {}) {
				CompositionLocalProvider(LocalPageTitleShown provides true) {
					Box(Modifier.weight(1f)) { PreviewRecentLikeList() }
				}
			}
		}
	}
}

@Preview(widthDp = 960, heightDp = 640, name = "Wide")
@Composable
private fun RecentLikeWidePreview() {
	PreviewTheme(background = true) {
		CompositionLocalProvider(LocalWindowSize provides DpSize(960.dp, 640.dp)) {
			PreviewRecentLikeList()
		}
	}
}

@Preview(widthDp = 360, name = "Empty")
@Composable
private fun RecentLikeEmptyPreview() {
	PreviewTheme(background = true) {
		RecentLikeList(
			groups = emptyList(), total = 0, rhythm = null, hasMore = false, loading = false,
			isUnliking = { false }, onPlay = {}, onPlayAll = {}, onUnlike = {}, onLoadMore = {},
		)
	}
}

@Preview
@Composable
private fun RecentLikeItemPreview() {
	PreviewTheme(background = false) {
		RecentLikeItem(
			item = previewLike(1, "Test Song Title", "Preview Artist", Instant.parse("2024-01-03T12:00:00Z")),
			onClick = {},
			onUnlikeClick = {},
			unlikeEnabled = true,
			modifier = Modifier.fillMaxWidth(),
		)
	}
}

