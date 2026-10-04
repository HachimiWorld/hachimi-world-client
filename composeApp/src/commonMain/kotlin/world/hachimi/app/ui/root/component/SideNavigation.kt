package world.hachimi.app.ui.root.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import hachimiworld.composeapp.generated.resources.nav_committee_center
import hachimiworld.composeapp.generated.resources.nav_contributor_center
import hachimiworld.composeapp.generated.resources.nav_creation_center
import hachimiworld.composeapp.generated.resources.nav_home_events
import hachimiworld.composeapp.generated.resources.nav_home_title
import hachimiworld.composeapp.generated.resources.nav_my_playlist
import hachimiworld.composeapp.generated.resources.nav_my_subscribe
import hachimiworld.composeapp.generated.resources.nav_recent_like
import hachimiworld.composeapp.generated.resources.nav_recent_play
import hachimiworld.composeapp.generated.resources.nav_settings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.SearchViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Navigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.TestTags
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.SubtleButton
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme

/**
 * Expanded rail: search + nav items + auth / profile footer.
 */
@Composable
fun ExpandedSideNavigation(
    content: Route.Root,
    onChange: (Route.Root) -> Unit = {},
    modifier: Modifier = Modifier,
    promo: @Composable () -> Unit = {},
) {
    val navigator = LocalNavigator.current
    var searchText by remember { mutableStateOf("") }

    Column(modifier.fillMaxHeight()) {
        SearchBox(
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            onSearch = {
                navigator.push(Route.Root.Search(searchText, SearchViewModel.SearchType.SONG))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(40.dp),
        )

        SideNavItems(
            content = content,
            onChange = onChange,
            density = NavItemDensity.Expanded,
            modifier = Modifier.weight(1f),
            includeSettings = true,
        )

        promo()
        AuthFooter()
    }
}

/**
 * Compact drawer: nav items only.
 * Search / account entry live on [CompactTopAppBar].
 * Uses larger hit targets (pre-rail NavItem metrics).
 */
@Composable
fun CompactSideNavigation(
    content: Route.Root,
    onChange: (Route.Root) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier.defaultMinSize(minWidth = 300.dp).fillMaxHeight()) {
        SideNavItems(
            content = content,
            onChange = onChange,
            density = NavItemDensity.Compact,
            modifier = Modifier.weight(1f),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .height(1.dp)
                .background(HachimiTheme.colorScheme.outline)
        )

        NavItem(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.Settings,
            label = stringResource(Res.string.nav_settings),
            selected = false,
            onSelectedChange = { onChange(Route.Root.Settings) },
            density = NavItemDensity.Compact,
        )
    }
}

/** Compact = phone drawer (larger touch targets); Expanded = desktop rail (denser). */
private enum class NavItemDensity {
    Compact,
    Expanded,
}

@Composable
private fun SideNavItems(
    content: Route.Root,
    onChange: (Route.Root) -> Unit,
    density: NavItemDensity,
    modifier: Modifier = Modifier,
    includeSettings: Boolean = false,
) {
    Column(
        modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NavItem(
            modifier = Modifier.fillMaxWidth().testTag(TestTags.NAV_EVENTS),
            icon = Icons.Default.Newspaper,
            label = stringResource(Res.string.nav_home_events),
            selected = content is Route.Root.Events.Feed,
            onSelectedChange = { onChange(Route.Root.Events.Feed) },
            density = density,
        )
        NavItem(
            modifier = Modifier.fillMaxWidth().testTag(TestTags.NAV_HOME),
            icon = Icons.Default.Home,
            label = stringResource(Res.string.nav_home_title),
            selected = content is Route.Root.Home.Main,
            onSelectedChange = { onChange(Route.Root.Home.Main) },
            density = density,
        )
        NavItem(
            modifier = Modifier.fillMaxWidth().testTag(TestTags.NAV_RECENT_PLAY),
            icon = Icons.Default.History,
            label = stringResource(Res.string.nav_recent_play),
            selected = content == Route.Root.RecentPlay,
            onSelectedChange = { onChange(Route.Root.RecentPlay) },
            density = density,
        )
        NavItem(
            modifier = Modifier.fillMaxWidth().testTag(TestTags.NAV_RECENT_LIKE),
            icon = Icons.Default.FavoriteBorder,
            label = stringResource(Res.string.nav_recent_like),
            selected = content == Route.Root.RecentLike,
            onSelectedChange = { onChange(Route.Root.RecentLike) },
            density = density,
        )
        NavItem(
            modifier = Modifier.fillMaxWidth().testTag(TestTags.NAV_MY_FOLLOWS),
            icon = Icons.Default.PersonAdd,
            label = stringResource(Res.string.nav_my_subscribe),
            selected = content == Route.Root.MySubscribe,
            onSelectedChange = { onChange(Route.Root.MySubscribe) },
            density = density,
        )
        NavItem(
            modifier = Modifier.fillMaxWidth().testTag(TestTags.NAV_MY_PLAYLISTS),
            icon = Icons.AutoMirrored.Filled.QueueMusic,
            label = stringResource(Res.string.nav_my_playlist),
            selected = content is Route.Root.MyPlaylist,
            onSelectedChange = { onChange(Route.Root.MyPlaylist.Default) },
            density = density,
        )
        NavItem(
            modifier = Modifier.fillMaxWidth().testTag(TestTags.NAV_CREATION),
            icon = Icons.Default.Edit,
            label = stringResource(Res.string.nav_creation_center),
            selected = content is Route.Root.CreationCenter,
            onSelectedChange = { onChange(Route.Root.CreationCenter.Default) },
            density = density,
        )
        NavItem(
            modifier = Modifier.fillMaxWidth().testTag(TestTags.NAV_COMMITTEE),
            icon = Icons.Default.Groups,
            label = stringResource(Res.string.nav_committee_center),
            selected = content == Route.Root.CommitteeCenter,
            onSelectedChange = { onChange(Route.Root.CommitteeCenter) },
            density = density,
        )
        NavItem(
            modifier = Modifier.fillMaxWidth().testTag(TestTags.NAV_CONTRIBUTOR),
            icon = Icons.Default.Build,
            label = stringResource(Res.string.nav_contributor_center),
            selected = content is Route.Root.ContributorCenter,
            onSelectedChange = { onChange(Route.Root.ContributorCenter.Default) },
            density = density,
        )
        if (includeSettings) {
            val navigator = LocalNavigator.current
            NavItem(
                modifier = Modifier.fillMaxWidth().testTag(TestTags.NAV_SETTINGS),
                icon = Icons.Default.Settings,
                label = stringResource(Res.string.nav_settings),
                selected = false,
                onSelectedChange = { navigator.push(Route.Root.Settings) },
                density = density,
            )
        }
    }
}

@Composable
private fun AuthFooter() {
    val navigator = LocalNavigator.current
    val global = koinInject<GlobalStore>()
    Spacer(Modifier.height(8.dp))
    if (global.isLoggedIn) {
        val userInfo = global.userInfo!!
        UserProfileEntry(
            name = userInfo.name,
            avatarUrl = userInfo.avatarUrl,
            onClick = { navigator.push(Route.Root.UserSpace) },
        )
    } else {
        SubtleButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navigator.push(Route.Auth(false)) },
        ) {
            Text(stringResource(Res.string.auth_register))
        }
        Spacer(Modifier.height(8.dp))
        AccentButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navigator.push(Route.Auth()) },
        ) {
            Text(stringResource(Res.string.auth_login))
        }
    }
}

@Composable
private fun UserProfileEntry(
    name: String,
    avatarUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(LocalContentColor.current.copy(0.12f))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .httpHeaders(CoilHeaders)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Text(
            text = name,
            style = navLabelStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

private val navLabelStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp)

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    density: NavItemDensity,
    modifier: Modifier = Modifier,
) {
    // Compact (phone drawer): padding(12.dp) + full icon — previous drawer metrics.
    // Expanded (desktop rail): denser vertical padding + 20dp icon.
    val contentPadding = when (density) {
        NavItemDensity.Compact -> PaddingValues(12.dp)
        NavItemDensity.Expanded -> PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    }
    val iconHeight = when (density) {
        NavItemDensity.Compact -> 24.dp
        NavItemDensity.Expanded -> 20.dp
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) HachimiTheme.colorScheme.primaryContainer else Color.Transparent,
        contentColor = if (selected) HachimiTheme.colorScheme.primary else LocalContentColor.current,
    ) {
        Row(
            Modifier
                .selectable(selected, onClick = { onSelectedChange(true) })
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.height(iconHeight),
                tint = LocalContentColor.current.copy(0.72f),
            )
            Spacer(Modifier.width(12.dp))
            Text(text = label, style = navLabelStyle)
        }
    }
}

@Preview(widthDp = 180, name = "Expanded · Home")
@Composable
private fun PreviewExpandedSideNavigation() {
    PreviewTheme(background = true) {
        val navigator = remember { Navigator(Route.Root.Home.Main) }
        CompositionLocalProvider(LocalNavigator provides navigator) {
            ExpandedSideNavigation(content = Route.Root.Home.Main)
        }
    }
}

@Preview(widthDp = 300, name = "Compact · Home")
@Composable
private fun PreviewCompactSideNavigation() {
    PreviewTheme(background = true) {
        CompactSideNavigation(content = Route.Root.Home.Main)
    }
}
