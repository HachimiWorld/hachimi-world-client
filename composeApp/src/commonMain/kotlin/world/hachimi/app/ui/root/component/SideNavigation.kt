package world.hachimi.app.ui.root.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.design.HachimiTheme
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
                icon = Icons.Default.Home,
                label = "主页",
                selected = content is Route.Root.Home,
                onSelectedChange = {
                    onChange(Route.Root.Home.Main)
                }
            )
            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.FavoriteBorder,
                label = "最近点赞", selected = content == Route.Root.RecentLike, onSelectedChange = {
                    onChange(Route.Root.RecentLike)
                }
            )
            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.History,
                label = "最近播放",
                selected = content == Route.Root.RecentPlay,
                onSelectedChange = {
                    onChange(Route.Root.RecentPlay)
                }
            )

            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                label = "我的歌单",
                selected = content is Route.Root.MyPlaylist,
                onSelectedChange = {
                    onChange(Route.Root.MyPlaylist.Default)
                }
            )
            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.PersonAdd,
                label = "我的关注", selected = content == Route.Root.MySubscribe, onSelectedChange = {
                    onChange(Route.Root.MySubscribe)
                }
            )

            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Edit,
                label = "创作中心",
                selected = content is Route.Root.CreationCenter,
                onSelectedChange = {
                    onChange(Route.Root.CreationCenter.Default)
                }
            )

            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Groups,
                label = "委员会中心",
                selected = content == Route.Root.CommitteeCenter,
                onSelectedChange = {
                    onChange(Route.Root.CommitteeCenter)
                }
            )
            NavItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Build,
                label = "贡献者中心",
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
            label = "设置",
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