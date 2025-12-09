package world.hachimi.app.ui.player.footer.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Text

private val titleTypography = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

@Composable
fun Title(
    text: String
) {
    Text(
        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE, velocity = 10.dp),
        text = text,
        style = titleTypography,
        color = HachimiTheme.colorScheme.onSurface,
        maxLines = 1,
    )
}