package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.getPlatform
import world.hachimi.app.model.PlayerUIState
import world.hachimi.app.model.SongDetailInfo
import world.hachimi.app.ui.component.Chip
import world.hachimi.app.ui.component.HintChip
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.isValidHttpsUrl

@Composable
fun InfoTabContent(
    modifier: Modifier,
    uiState: PlayerUIState,
    contentPadding: PaddingValues = PaddingValues()
) {
    Crossfade(uiState.readySongInfo) { readySongInfo ->
        Column(modifier.verticalScroll(rememberScrollState()).padding(contentPadding)) {
            // No max line limit
            Text(
                text = uiState.displayedTitle,
                style = propertyTitleStyle
            )
            readySongInfo?.subtitle?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = contentSubtitleStyle,
                    color = LocalContentColor.current.copy(0.6f)
                )
            }
            Column(
                modifier = Modifier.padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PropertyLine("作者") {
                    UserChip(
                        onClick = {
                            // TODO
                        },
                        avatar = null,
                        name = uiState.displayedAuthor
                    )
                }

                readySongInfo?.let { songInfo ->
                    // PV
                    PVs(songInfo)
                    // Staffs
                    Staffs(songInfo)
                    // Origin infos
                    Origins(songInfo)
                }
            }
            // Description
            readySongInfo?.description?.takeIf { it.isNotBlank() }?.let {
                SelectionContainer {
                    Text(
                        modifier = Modifier.padding(top = 16.dp),
                        text = it, style = contentSubtitleStyle,
                        color = LocalContentColor.current.copy(0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PVs(songInfo: SongDetailInfo) {
    if (songInfo.externalLinks.isEmpty()) return
    PropertyLine(
        label = "PV",
        verticalAlignment = if (songInfo.externalLinks.size > 1) Alignment.Top else Alignment.CenterVertically
    ) {
        songInfo.externalLinks.forEach {
            PVChip(
                platform = it.platform,
                onClick = {
                    if (isValidHttpsUrl(it.url)) {
                        getPlatform().openUrl(it.url)
                    } else {
                        // TODO: Show a dialog
                    }
                }
            )
        }
    }
}

@Composable
private fun Staffs(songInfo: SongDetailInfo) {
    if (songInfo.productionCrew.isEmpty()) return
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
    if (songInfo.originInfos.isEmpty()) return
    PropertyLine(
        label = "原作",
        verticalAlignment = if (songInfo.originInfos.size > 1) Alignment.Top else Alignment.CenterVertically,
        content = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
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
    info: SongModule.CreationTypeInfo,
    onNavToInternalSong: (jmid: String) -> Unit,
    onOpenLinkClick: (url: String) -> Unit
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
        TitleArtist(title, artist)
        Icon(
            modifier = Modifier.padding(start = 4.dp).requiredSize(16.dp),
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            tint = LocalContentColor.current.copy(0.6f),
            contentDescription = "Listen this music"
        )
    }
}

@Composable
private fun ExternalOriginChip(title: String?, artist: String?, onClick: () -> Unit) {
    Chip(onClick = onClick) {
        TitleArtist(title, artist)
        Icon(
            modifier = Modifier.padding(start = 4.dp).requiredSize(16.dp),
            imageVector = Icons.Filled.ArrowOutward,
            tint = LocalContentColor.current.copy(0.6f),
            contentDescription = "Open in new tab"
        )
    }
}

@Composable
private fun NoLinkOriginChip(title: String?, artist: String?) {
    HintChip {
        TitleArtist(title, artist)
    }
}

@Composable
private fun RowScope.TitleArtist(title: String?, artist: String?) {
    Text(
        modifier = Modifier,
        text = title ?: "unknown",
        overflow = TextOverflow.Ellipsis,
    )
    if (artist != null) Text(" - $artist")
}

@Composable
private fun PropertyLine(
    label: String,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable () -> Unit
) {
    Row(verticalAlignment = verticalAlignment) {
        Text(text = label, style = propertyTitleStyle)
        Box(Modifier.padding(start = 16.dp).weight(1f), contentAlignment = Alignment.CenterEnd) {
            content()
        }
    }
}

private val propertyTitleStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

private val contentSubtitleStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
)