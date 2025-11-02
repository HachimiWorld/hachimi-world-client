package world.hachimi.app.ui.root.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun SideNavigation(
    content: Route,
    onChange: (Route) -> Unit = {},
) {
    Column(Modifier.defaultMinSize(minWidth = 300.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            NavItem(
                icon = Icons.Default.Home,
                label = "主页",
                selected = content is Route.Root.Home,
                onSelectedChange = {
                    onChange(Route.Root.Home.Main)
                })
            NavItem(
                icon = Icons.Default.FavoriteBorder,
                label = "最近点赞", selected = content == Route.Root.RecentLike, onSelectedChange = {
                    onChange(Route.Root.RecentLike)
                })
            NavItem(
                icon = Icons.Default.History,
                label = "最近播放",
                selected = content == Route.Root.RecentPlay,
                onSelectedChange = {
                    onChange(Route.Root.RecentPlay)
                })

            NavItem(
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                label = "我的歌单",
                selected = content is Route.Root.MyPlaylist,
                onSelectedChange = {
                    onChange(Route.Root.MyPlaylist.Default)
                })
            NavItem(
                icon = Icons.Default.PersonAdd,
                label = "我的关注", selected = content == Route.Root.MySubscribe, onSelectedChange = {
                    onChange(Route.Root.MySubscribe)
                })

            NavItem(
                icon = Icons.Default.Edit,
                label = "创作中心",
                selected = content is Route.Root.CreationCenter,
                onSelectedChange = {
                    onChange(Route.Root.CreationCenter.Default)
                })

            NavItem(
                icon = Icons.Default.Groups,
                label = "委员会中心",
                selected = content == Route.Root.CommitteeCenter,
                onSelectedChange = {
                    onChange(Route.Root.CommitteeCenter)
                })
            NavItem(
                icon = Icons.Default.Build,
                label = "贡献者中心",
                selected = content is Route.Root.ContributorCenter,
                onSelectedChange = {
                    onChange(Route.Root.ContributorCenter.Default)
                })
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 12.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        NavItem(
            icon = Icons.Default.Settings,
            label = "设置",
            selected = content is Route.Root.Settings,
            onSelectedChange = {
                onChange(Route.Root.Settings)
            }
        )
    }
}


@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    NavigationDrawerItem(
        modifier = modifier,
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        selected = selected,
        onClick = {
            onSelectedChange(true)
        }
    )
    /*if (selected) {
        Button(modifier = modifier, onClick = {}) {
            Text(label)
        }
    } else {
        TextButton(modifier = modifier, onClick = { onSelectedChange(true) }) {
            Text(label)
        }
    }*/
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        SideNavigation(Route.Root.Home.Main) {
        }
    }
}