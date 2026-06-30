package world.hachimi.app.ui.design.components

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun TabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val colorScheme = HachimiTheme.colorScheme
    Row(modifier = modifier) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            val textColor by animateColorAsState(
                if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
            )
            val underlineColor by animateColorAsState(
                if (isSelected) colorScheme.primary else Color.Transparent
            )
            Column(
                modifier = Modifier
                    .clickable(indication = null, interactionSource = null) { onTabSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = maxLines,
                    overflow = overflow,
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
private fun PreviewTabBar() {
    PreviewTheme(background = true) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            TabBar(
                tabs = listOf("作品", "歌单", "动态"),
                selectedIndex = 0,
                onTabSelected = {},
            )
            TabBar(
                tabs = listOf("已发布", "我的提交"),
                selectedIndex = 1,
                onTabSelected = {},
            )
        }
    }
}