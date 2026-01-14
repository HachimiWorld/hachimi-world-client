package world.hachimi.app.ui.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.model.SearchViewModel
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.LocalTextStyle
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.formatSongDuration
import kotlin.time.Duration.Companion.seconds

@Composable
fun SearchSongItem(
    data: SearchViewModel.SearchSongItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.clickable(onClick = onClick)) {
            Surface(
                Modifier.aspectRatio(1f).fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                color = LocalContentColor.current.copy(0.12f)
            ) {
                AsyncImage(
                    model = data.info.coverArtUrl,
                    contentDescription = "Song Cover Image",
                    contentScale = ContentScale.Crop
                )
            }
            Row(Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)) {
                Column(Modifier.weight(1f)) {

                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = buildAnnotatedString {
                                val title = data.info.title
                                var currentIndex = 0
                                for (range in data.matchTitleRanges) {
                                    append(title.substring(currentIndex, range.first))
                                    withStyle(MaterialTheme.typography.titleSmall.toSpanStyle().copy(color = HachimiTheme.colorScheme.primary)) {
                                        append(title.substring(range.first, range.last + 1))
                                    }
                                    currentIndex = range.last + 1
                                }
                                if (currentIndex < title.length) {
                                    append(title.substring(currentIndex))
                                }
                            },
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1
                        )

                        if (data.info.explicit == true) Icon(
                            modifier = Modifier.padding(start = 8.dp).requiredSize(16.dp),
                            imageVector = Icons.Default.Explicit,
                            contentDescription = "Explicit",
                            tint = LocalContentColor.current.copy(0.72f),
                        )
                    }

                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.bodySmall.copy(LocalContentColor.current.copy(0.72f))
                    ) {
                        if (data.matchedOriginTitles.isNotEmpty()) {
                            Text(
                                text = buildAnnotatedString {
                                    append("原曲：")
                                    withStyle(LocalTextStyle.current.copy(color = HachimiTheme.colorScheme.primary).toSpanStyle()) {
                                        append(data.matchedOriginTitles.first())
                                    }
                                },
                                maxLines = 1
                            )
                        } else if (data.matchedOriginArtists.isNotEmpty()) {
                            Text(
                                text = buildAnnotatedString {
                                    append("原作者：")
                                    withStyle(LocalTextStyle.current.copy(color = HachimiTheme.colorScheme.primary).toSpanStyle()) {
                                        append(data.matchedOriginArtists.first())
                                    }
                                },
                                maxLines = 1
                            )
                        } else if (data.matchSubtitleRanges.isNotEmpty()) {
                            Text(
                                text = buildAnnotatedString {
                                    val subtitle = data.info.subtitle
                                    var currentIndex = 0
                                    for (range in data.matchSubtitleRanges) {
                                        append(subtitle.substring(currentIndex, range.first))
                                        withStyle(LocalTextStyle.current.toSpanStyle().copy(color = HachimiTheme.colorScheme.primary)) {
                                            append(subtitle.substring(range.first, range.last + 1))
                                        }
                                        currentIndex = range.last + 1
                                    }
                                    if (currentIndex < subtitle.length) {
                                        append(subtitle.substring(currentIndex))
                                    }
                                },
                                maxLines = 1
                            )
                        } else  if (data.matchDescRanges.isNotEmpty()) {
                            Text(
                                text = buildAnnotatedString {
                                    val description = data.info.description
                                    var currentIndex = 0
                                    for (range in data.matchDescRanges) {
                                        append(description.substring(currentIndex, range.first))
                                        withStyle(LocalTextStyle.current.toSpanStyle().copy(color = HachimiTheme.colorScheme.primary)) {
                                            append(description.substring(range.first, range.last + 1))
                                        }
                                        currentIndex = range.last + 1
                                    }
                                    if (currentIndex < description.length) {
                                        append(description.substring(currentIndex))
                                    }
                                },
                                maxLines = 1
                            )
                        } else {
                            Text(
                                text = data.info.subtitle,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))
                    Text(data.info.uploaderName, style = MaterialTheme.typography.labelSmall, color = LocalContentColor.current.copy(0.6f))
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = "Duration", modifier = Modifier.size(12.dp))
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = formatSongDuration(data.info.durationSeconds.seconds),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Headphones, contentDescription = "Play Count", modifier = Modifier.size(12.dp))
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = data.info.playCount.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        SearchSongItem(SearchViewModel.SearchSongItem(
            info = SongModule.SearchSongItem(
                id = 0,
                displayId = "1",
                title = "Test Song Title",
                subtitle = "This is a test subtitle",
                description = "test",
                artist = "test",
                durationSeconds = 100,
                playCount = 100,
                likeCount = 100,
                coverArtUrl = "",
                audioUrl = "",
                uploaderUid = 0,
                uploaderName = "Author",
                explicit = true,
                originalTitles = listOf(),
                originalArtists = listOf()
            ),
            matchTitleRanges = listOf(0..3),
            matchDescRanges = listOf(6..9),
            matchSubtitleRanges = listOf(),
            matchedOriginArtists = listOf(),
            matchedOriginTitles = listOf(),
        ))
    }
}