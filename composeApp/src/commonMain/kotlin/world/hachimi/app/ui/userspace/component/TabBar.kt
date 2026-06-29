package world.hachimi.app.ui.userspace.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.user_space_tab_activity
import hachimiworld.composeapp.generated.resources.user_space_tab_playlists
import hachimiworld.composeapp.generated.resources.user_space_tab_songs
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun TabBar(selectedIndex: Int, onTabSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colorScheme = HachimiTheme.colorScheme
    Row(
        modifier = modifier,
    ) {
        val tabs = listOf(
            Res.string.user_space_tab_songs,
            Res.string.user_space_tab_playlists,
            Res.string.user_space_tab_activity
        )
        tabs.forEachIndexed { index, titleRes ->
            val isSelected = selectedIndex == index
            val textColor by animateColorAsState(
                if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
            )
            val underlineColor by animateColorAsState(
                if (isSelected) colorScheme.primary else Color.Transparent
            )
            Column(
                modifier = Modifier.clickable(indication = null, interactionSource = null) { onTabSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(titleRes),
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(3.dp)
                        .background(underlineColor, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewTabBarSelected() {
    PreviewTheme(background = true) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            TabBar(selectedIndex = 0, onTabSelected = {})
            TabBar(selectedIndex = 1, onTabSelected = {})
            TabBar(selectedIndex = 2, onTabSelected = {})
        }
    }
}
