package world.hachimi.app.ui.root.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
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
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun SideNavigation(
    content: Route,
    onChange: (Route) -> Unit = {},
) {
    Column(Modifier.defaultMinSize(minWidth = 300.dp)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Newspaper,
                label = stringResource(Res.string.nav_home_events),
                selected = content is Route.Root.Events,
                onSelectedChange = {
                    onChange(Route.Root.Events.Feed)
                }
            )
            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Home,
                label = stringResource(Res.string.nav_home_title),
                selected = content is Route.Root.Home,
                onSelectedChange = {
                    onChange(Route.Root.Home.Main)
                }
            )
            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.FavoriteBorder,
                label = stringResource(Res.string.nav_recent_like),
                selected = content == Route.Root.RecentLike,
                onSelectedChange = {
                    onChange(Route.Root.RecentLike)
                }
            )
            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.History,
                label = stringResource(Res.string.nav_recent_play),
                selected = content == Route.Root.RecentPlay,
                onSelectedChange = {
                    onChange(Route.Root.RecentPlay)
                }
            )

            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                label = stringResource(Res.string.nav_my_playlist),
                selected = content is Route.Root.MyPlaylist,
                onSelectedChange = {
                    onChange(Route.Root.MyPlaylist.Default)
                }
            )
            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.PersonAdd,
                label = stringResource(Res.string.nav_my_subscribe),
                selected = content == Route.Root.MySubscribe,
                onSelectedChange = {
                    onChange(Route.Root.MySubscribe)
                }
            )

            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Edit,
                label = stringResource(Res.string.nav_creation_center),
                selected = content is Route.Root.CreationCenter,
                onSelectedChange = {
                    onChange(Route.Root.CreationCenter.Default)
                }
            )

            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Groups,
                label = stringResource(Res.string.nav_committee_center),
                selected = content == Route.Root.CommitteeCenter,
                onSelectedChange = {
                    onChange(Route.Root.CommitteeCenter)
                }
            )
            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Build,
                label = stringResource(Res.string.nav_contributor_center),
                selected = content is Route.Root.ContributorCenter,
                onSelectedChange = {
                    onChange(Route.Root.ContributorCenter.Default)
                }
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 12.dp).height(1.dp)
                .background(HachimiTheme.colorScheme.outline)
        )

        NavItem(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.Settings,
            label = stringResource(Res.string.nav_settings),
            selected = content is Route.Root.Settings,
            onSelectedChange = {
                onChange(Route.Root.Settings)
            }
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
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) HachimiTheme.colorScheme.primaryContainer else Color.Transparent,
        contentColor = if (selected) HachimiTheme.colorScheme.primary else LocalContentColor.current,
    ) {
        Row(
            Modifier.selectable(selected, onClick = { onSelectedChange(true) }).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(text = label, style = navLabelStyle)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        SideNavigation(Route.Root.Home.Main) {
        }
    }
}