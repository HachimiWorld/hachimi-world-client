package world.hachimi.app.ui.me

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import hachimiworld.composeapp.generated.resources.message_entry_unread_cd
import hachimiworld.composeapp.generated.resources.notification_retry
import hachimiworld.composeapp.generated.resources.playlist_empty
import hachimiworld.composeapp.generated.resources.playlist_favorites_title
import hachimiworld.composeapp.generated.resources.playlist_my_playlists_title
import hachimiworld.composeapp.generated.resources.playlist_song_count
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.PlaylistViewModel
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.placeholder
import world.hachimi.app.ui.notification.components.NotificationBell
import world.hachimi.app.ui.notification.components.formatBadgeCount
import world.hachimi.app.ui.util.listHeadInsetsSpacerItem
import world.hachimi.app.ui.util.listTailSpacerItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.auth_login
import hachimiworld.composeapp.generated.resources.auth_register
import hachimiworld.composeapp.generated.resources.me_follow_stats
import hachimiworld.composeapp.generated.resources.me_guest_hint
import hachimiworld.composeapp.generated.resources.me_guest_title
import hachimiworld.composeapp.generated.resources.message_entry_cd
import hachimiworld.composeapp.generated.resources.nav_committee_center
import hachimiworld.composeapp.generated.resources.nav_contributor_center
import hachimiworld.composeapp.generated.resources.nav_creation_center
import hachimiworld.composeapp.generated.resources.nav_my_subscribe
import hachimiworld.composeapp.generated.resources.nav_recent_like
import hachimiworld.composeapp.generated.resources.nav_recent_play
import hachimiworld.composeapp.generated.resources.nav_settings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.model.AppEvent
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.MeViewModel
import world.hachimi.app.model.MessageCenterStore
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.TestTags
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.SubtleButton
import world.hachimi.app.ui.design.components.Text

/**
 * The phone's Me tab: who's signed in, six tiles for the library and the centers, and then the
 * user's playlists themselves, as long as they are. Messages and settings sit at the top corner.
 * Everything the side navigation lists on wide screens is reached from here.
 *
 * @since 261110
 */
@Composable
fun MeScreen(
    vm: MeViewModel = koinViewModel(),
    playlists: PlaylistViewModel = koinViewModel(),
    global: GlobalStore = koinInject(),
    messages: MessageCenterStore = koinInject(),
) {
    val navigator = LocalNavigator.current
    val user = global.userInfo
    LaunchedEffect(user?.uid) {
        vm.refresh()
        if (user != null) playlists.mounted()
    }
    // Playlists change elsewhere while this page waits underneath
    LaunchedEffect(global) {
        global.events.events.collect {
            if (it is AppEvent.PlaylistDeleted || it is AppEvent.SongAddedToPlaylist) playlists.mounted()
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = MeMaxWidth).fillMaxSize()) {
            listHeadInsetsSpacerItem()
            item(key = "actions") {
                Row(Modifier.fillMaxWidth().height(48.dp).padding(end = 4.dp), horizontalArrangement = Arrangement.End) {
                    if (user != null) {
                        val unread = messages.totalUnread
                        val messagesCd = if (unread != null && unread > 0) {
                            stringResource(Res.string.message_entry_unread_cd, formatBadgeCount(unread))
                        } else {
                            stringResource(Res.string.message_entry_cd)
                        }
                        HachimiIconButton(
                            onClick = { navigator.push(Route.Root.Messages) },
                            modifier = Modifier.size(48.dp).semantics { contentDescription = messagesCd },
                            touchMode = true,
                        ) { NotificationBell(unreadCount = unread) }
                    }
                    HachimiIconButton(
                        onClick = { navigator.push(Route.Root.Settings) },
                        modifier = Modifier.size(48.dp).testTag(TestTags.NAV_SETTINGS),
                        touchMode = true,
                    ) { Icon(Icons.Outlined.Settings, stringResource(Res.string.nav_settings), Modifier.size(24.dp)) }
                }
            }
            item(key = "identity") {
                if (user != null) {
                    Identity(
                        name = user.name,
                        avatarUrl = user.avatarUrl,
                        // Shown once loaded; the line keeps its place meanwhile
                        stats = vm.profile?.let {
                            stringResource(Res.string.me_follow_stats, it.followingCount.toString(), it.followerCount.toString())
                        } ?: "",
                        onClick = { navigator.push(Route.Root.UserSpace) },
                    )
                } else {
                    Guest(
                        onLogin = { navigator.push(Route.Auth()) },
                        onRegister = { navigator.push(Route.Auth(false)) },
                    )
                }
            }
            item(key = "tiles") {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(TileGap)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(TileGap)) {
                        Tile(
                            Icons.Default.History, stringResource(Res.string.nav_recent_play),
                            Modifier.weight(1f).testTag(TestTags.NAV_RECENT_PLAY),
                        ) { navigator.switchPrimary(Route.Root.RecentPlay) }
                        Tile(
                            Icons.Default.FavoriteBorder, stringResource(Res.string.nav_recent_like),
                            Modifier.weight(1f).testTag(TestTags.NAV_RECENT_LIKE),
                        ) { navigator.switchPrimary(Route.Root.RecentLike) }
                        Tile(
                            Icons.Outlined.PersonAdd, stringResource(Res.string.nav_my_subscribe),
                            Modifier.weight(1f).testTag(TestTags.NAV_MY_FOLLOWS),
                        ) { navigator.switchPrimary(Route.Root.MySubscribe) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(TileGap)) {
                        Tile(
                            Icons.Outlined.Edit, stringResource(Res.string.nav_creation_center),
                            Modifier.weight(1f).testTag(TestTags.NAV_CREATION),
                        ) { navigator.switchPrimary(Route.Root.CreationCenter.Default) }
                        Tile(
                            Icons.Outlined.Groups, stringResource(Res.string.nav_committee_center),
                            Modifier.weight(1f).testTag(TestTags.NAV_COMMITTEE),
                        ) { navigator.switchPrimary(Route.Root.CommitteeCenter) }
                        Tile(
                            Icons.Outlined.Build, stringResource(Res.string.nav_contributor_center),
                            Modifier.weight(1f).testTag(TestTags.NAV_CONTRIBUTOR),
                        ) { navigator.switchPrimary(Route.Root.ContributorCenter.Default) }
                    }
                }
            }

            // The playlists themselves, all of them: the page is theirs from here down
            if (user != null) {
                item(key = "my_title") { SectionTitle(stringResource(Res.string.playlist_my_playlists_title)) }
                when {
                    playlists.initializeStatus == InitializeStatus.INIT ->
                        items(3, key = { "playlist_skeleton_$it" }) { PlaylistRowSkeleton() }
                    playlists.initializeStatus == InitializeStatus.FAILED -> item(key = "playlist_failed") {
                        Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            TextButton(onClick = playlists::retry) { Text(stringResource(Res.string.notification_retry)) }
                        }
                    }
                    playlists.playlists.isEmpty() -> item(key = "playlist_empty") {
                        Text(
                            stringResource(Res.string.playlist_empty),
                            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            fontSize = 14.sp,
                            color = HachimiTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    else -> items(playlists.playlists, key = { "my_${it.id}" }) {
                        PlaylistRow(it.coverUrl, it.name, it.songsCount.toLong()) {
                            navigator.push(Route.Root.MyPlaylist.Detail(it.id))
                        }
                    }
                }
                if (playlists.favoritePlaylists.isNotEmpty()) {
                    item(key = "favorite_title") { SectionTitle(stringResource(Res.string.playlist_favorites_title)) }
                    items(playlists.favoritePlaylists, key = { "favorite_${it.metadata.id}" }) {
                        PlaylistRow(it.metadata.coverUrl, it.metadata.name, it.metadata.songsCount.toLong()) {
                            navigator.push(Route.Root.PublicPlaylist(it.metadata.id))
                        }
                    }
                }
            }
            listTailSpacerItem()
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        Modifier.padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 8.dp),
        fontSize = 17.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

/** A playlist, flat on the page: its cover, its name, how many songs. */
@Composable
private fun PlaylistRow(coverUrl: String?, name: String, songCount: Long, onClick: () -> Unit) {
    val colors = HachimiTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(PlaylistCoverSize).clip(RoundedCornerShape(10.dp)).background(colors.onSurface.copy(0.08f))) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .httpHeaders(CoilHeaders)
                    .data(coverUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(name, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                stringResource(Res.string.playlist_song_count, songCount),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = colors.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

/** Stands in for a [PlaylistRow] while the playlists load. */
@Composable
private fun PlaylistRowSkeleton() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(PlaylistCoverSize).placeholder(true, RoundedCornerShape(10.dp)))
        Column(Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.width(140.dp).height(12.dp).placeholder(true))
            Box(Modifier.width(48.dp).height(10.dp).placeholder(true))
        }
    }
}

/** The signed-in user, as the way into their own profile. */
@Composable
private fun Identity(name: String, avatarUrl: String?, stats: String, onClick: () -> Unit) {
    val colors = HachimiTheme.colorScheme
    // The press lights a rounded block hugging the avatar and name, set in from the edges, rather
    // than a full-width band that reaches into the space above the tiles
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .testTag(TestTags.PROFILE_AVATAR)
            .clickable(onClick = onClick)
            .padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(AvatarSize).clip(CircleShape).background(colors.onSurface.copy(0.08f))) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .httpHeaders(CoilHeaders)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Column(Modifier.weight(1f).padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                name,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(stats, fontSize = 14.sp, lineHeight = 20.sp, color = colors.onSurfaceVariant, maxLines = 1)
        }
        Chevron()
    }
}

/** In place of [Identity] while nobody is signed in. */
@Composable
private fun Guest(onLogin: () -> Unit, onRegister: () -> Unit) {
    val colors = HachimiTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(AvatarSize).clip(CircleShape).background(colors.onSurface.copy(0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Person, null, Modifier.size(32.dp), tint = colors.onSurfaceVariant)
            }
            Column(Modifier.weight(1f).padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(Res.string.me_guest_title),
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    stringResource(Res.string.me_guest_hint),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = colors.onSurfaceVariant,
                )
            }
        }
        Row(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AccentButton(onClick = onLogin, modifier = Modifier.weight(1f), shape = CircleShape) {
                Text(stringResource(Res.string.auth_login), fontWeight = FontWeight.Medium)
            }
            SubtleButton(onClick = onRegister, modifier = Modifier.weight(1f), shape = CircleShape) {
                Text(stringResource(Res.string.auth_register), fontWeight = FontWeight.Medium)
            }
        }
    }
}

/**
 * One way in, three to a row. Its icon is drawn twice: small in the brand color, and large and
 * faint in the far corner, running off the edge.
 */
@Composable
private fun Tile(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = HachimiTheme.colorScheme
    Box(
        modifier
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
    ) {
        Icon(
            icon, null,
            Modifier.align(Alignment.BottomEnd).offset(x = 12.dp, y = 14.dp).size(60.dp),
            tint = colors.onSurface.copy(alpha = 0.045f),
        )
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, null, Modifier.size(22.dp), tint = colors.primary)
            Text(label, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun Chevron() {
    Icon(
        Icons.Default.ChevronRight, null,
        Modifier.size(20.dp),
        tint = HachimiTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
    )
}

private val MeMaxWidth = 640.dp
private val AvatarSize = 68.dp
private val TileGap = 10.dp
private val PlaylistCoverSize = 52.dp
