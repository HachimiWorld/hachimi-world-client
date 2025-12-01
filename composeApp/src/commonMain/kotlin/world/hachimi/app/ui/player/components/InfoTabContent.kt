package world.hachimi.app.ui.player.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.getPlatform
import world.hachimi.app.model.PlayerUIState
import world.hachimi.app.model.SongDetailInfo
import world.hachimi.app.ui.component.Chip
import world.hachimi.app.ui.component.HintChip
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.isValidHttpsUrl

@Composable
fun InfoTabContent(
    modifier: Modifier,
    uiState: PlayerUIState,
    contentPadding: PaddingValues = PaddingValues()
) {
    Column(modifier.verticalScroll(rememberScrollState()).padding(contentPadding)) {
        // No max line limit
        Text(
            text = uiState.displayedTitle,
            style = titleStyle,
            color = HachimiTheme.colorScheme.onSurfaceReverse
        )
        uiState.readySongInfo?.subtitle?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = subtitleStyle,
                color = HachimiTheme.colorScheme.onSurfaceReverse.copy(0.6f)
            )
        }
        Column(
            modifier = Modifier.padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PropertyLine(
                label = "作者",
                content = {
                    UserChip(
                        onClick = {
                            // TODO
                        },
                        avatar = null,
                        name = uiState.displayedAuthor
                    )
                }
            )

            uiState.readySongInfo?.let { songInfo ->
                // Staffs
                Staffs(songInfo)
                // Origin infos
                Origins(songInfo)
            }
        }
        // Description
        uiState.readySongInfo?.description?.takeIf { it.isNotBlank() }?.let {
            SelectionContainer {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = it, style = subtitleStyle,
                    color = HachimiTheme.colorScheme.onSurfaceReverse.copy(0.6f)
                )
            }
        }
    }
}

@Composable
private fun Staffs(songInfo: SongDetailInfo) {
    songInfo.productionCrew.forEach {
        PropertyLine(
            label = it.role,
            content = {
                if (it.uid != null) {
                    UserChip(
                        onClick = {
                            it.uid
                            // TODO
                        },
                        avatar = null,
                        name = it.personName ?: "Unknown"
                    )
                } else {
                    HintUserChip(name = it.personName ?: "Unknown")
                }
            }
        )
    }
}

@Composable
private fun Origins(songInfo: SongDetailInfo) {
    PropertyLine(
        label = "原作",
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                songInfo.originInfos.forEach {
                    OriginChip(
                        info = it,
                        onNavToInternalSong = {
                           // TODO:
                        },
                        onOpenLinkClick = {
                            getPlatform().openUrl(it)
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun OriginChip(
    info: SongModule.CreationTypeInfo, onNavToInternalSong: (jmid: String) -> Unit, onOpenLinkClick: (url: String) -> Unit
) {
    if (info.songDisplayId != null) {
        // Internal song
        InternalOriginChip(info.title, info.artist, { onNavToInternalSong(info.songDisplayId) })
    } else if (info.url != null && isValidHttpsUrl(info.url)) {
        // External song
        ExternalOriginChip(info.title, info.artist, { onOpenLinkClick(info.url) })
    } else {
        // No Link
        NoLinkOriginChip(info.title, info.artist)
    }
}

@Composable
private fun InternalOriginChip(title: String?, artist: String?, onClick: () -> Unit) {
    Chip(onClick = onClick) {
        Text(title ?: "unknown")
        if (artist != null) Text(" - $artist")
        Icon(
            modifier = Modifier.padding(start = 4.dp),
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Listen this music"
        )
    }
}

@Composable
private fun ExternalOriginChip(title: String?, artist: String?, onClick: () -> Unit) {
    Chip(onClick = onClick) {
        Text(title ?: "unknown")
        if (artist != null) Text(" - $artist")
        Icon(
            modifier = Modifier.padding(start = 4.dp),
            imageVector = Icons.Filled.ArrowOutward,
            contentDescription = "Open in new tab"
        )
    }
}

@Composable
private fun NoLinkOriginChip(title: String?, artist: String?) {
    HintChip {
        Text(title ?: "unknown")
        if (artist != null) Text(" - $artist")
    }
}

@Composable
private fun PropertyLine(
    label: String,
    content: @Composable () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = titleStyle)
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            content()
        }
    }
}

private val titleStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

private val subtitleStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
)