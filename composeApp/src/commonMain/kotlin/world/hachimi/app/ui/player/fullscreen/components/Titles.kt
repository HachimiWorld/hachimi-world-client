package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text

private val titleStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

private val subtitleStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
)

@Composable
fun Titles(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            modifier = Modifier.basicMarquee(
                iterations = Int.MAX_VALUE,
                velocity = 10.dp
            ),
            text = title,
            style = titleStyle, color = LocalContentColor.current,
            maxLines = 1
        )

        subtitle?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = subtitleStyle, color = LocalContentColor.current.copy(0.6f),
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
    }
}