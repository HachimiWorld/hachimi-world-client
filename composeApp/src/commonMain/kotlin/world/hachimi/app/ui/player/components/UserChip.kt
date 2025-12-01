package world.hachimi.app.ui.player.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.component.AmbientChip
import world.hachimi.app.ui.component.Chip
import world.hachimi.app.ui.component.HintChip
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun UserChip(
    onClick: () -> Unit,
    avatar: Painter?,
    name: String,
    modifier: Modifier = Modifier
) {
    Chip(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(start = if (avatar != null) 4.dp else 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        UserChipContent(avatar, name)
    }
}

@Composable
fun AmbientUserChip(
    onClick: () -> Unit,
    avatar: Painter?,
    name: String,
    modifier: Modifier = Modifier
) {
    AmbientChip(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(start = if (avatar != null) 4.dp else 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        UserChipContent(avatar, name)
    }
}

@Composable
private fun UserChipContent(avatar: Painter?, name: String) {
    avatar?.let {
        Box(Modifier.size(16.dp).clip(CircleShape).background(Color(0xFFB7B7B7).copy(0.6f))) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = it,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.width(4.dp))
    }
    Text(text = name)
}

@Composable
fun HintUserChip(
    name: String,
    modifier: Modifier = Modifier
) {
    HintChip(modifier) {
        Text(text = name)
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
private fun PreviewAmbient() {
    PreviewTheme(background = false) {
        AmbientUserChip(onClick = {}, avatar = null, name = "Hachimi")
    }
}

@Preview
@Composable
private fun PreviewHint() {
    PreviewTheme(background = false) {
        HintUserChip(name = "Hachimi")
    }
}