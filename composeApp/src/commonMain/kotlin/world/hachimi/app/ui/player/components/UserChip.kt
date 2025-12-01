package world.hachimi.app.ui.player.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme

private val textStyle = TextStyle(
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    lineHeight = 16.sp
)

@Composable
fun UserChip(
    onClick: () -> Unit,
    avatar: Painter?,
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .height(24.dp).defaultMinSize(minWidth = 60.dp)
            .clip(RoundedCornerShape(50))
            .background(HachimiTheme.colorScheme.onSurfaceReverse)
            .clickable(onClick = onClick)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(16.dp).clip(CircleShape).background(Color(0xFFB7B7B7))) {
            avatar?.let {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = it,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop
                )
            }
        }
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = name,
            style = textStyle,
            color = HachimiTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TextUserChip(
    onClick: () -> Unit,
    avatar: Painter?,
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .height(24.dp).defaultMinSize(minWidth = 60.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(16.dp).clip(CircleShape).background(Color(0xFFB7B7B7))) {
            avatar?.let {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = it,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop
                )
            }
        }
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = name,
            style = textStyle,
        )
    }
}

@Composable
fun OutlineUserChip(
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .height(24.dp).defaultMinSize(minWidth = 60.dp)
            .border(1.dp, color = LocalContentColor.current, shape = RoundedCornerShape(50))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = textStyle,
        )
    }
}


@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        UserChip(onClick = {}, avatar = null, name = "Hachimi")
    }
}

@Preview
@Composable
private fun PreviewText() {
    PreviewTheme(background = false) {
        TextUserChip(onClick = {}, avatar = null, name = "Hachimi")
    }
}