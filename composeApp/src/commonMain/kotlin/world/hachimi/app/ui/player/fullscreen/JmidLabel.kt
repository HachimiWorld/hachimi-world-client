package world.hachimi.app.ui.player.fullscreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text

private val jmidStyle = TextStyle(
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    lineHeight = 16.sp
)

@Composable
fun JmidLabel(
    modifier: Modifier = Modifier,
    jmid: String
) {
    Text(
        modifier = modifier,
        text = jmid,
        style = jmidStyle,
        color = LocalContentColor.current.copy(0.6f)
    )
}