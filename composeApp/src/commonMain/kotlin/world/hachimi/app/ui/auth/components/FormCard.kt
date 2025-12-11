package world.hachimi.app.ui.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.component.Logo
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.LocalTextStyle
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme

private val titleStyle = TextStyle(fontSize = 48.sp)
private val subtitleStyle = TextStyle(fontSize = 16.sp)

@Composable
fun FormCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(modifier.defaultMinSize(minWidth = 280.dp)) {
        Box(Modifier.padding(horizontal = 32.dp, vertical = 24.dp)) {
            content()
        }
    }
}

@Composable
fun FormContent(
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    logoShowText: Boolean = true,
    content: @Composable () -> Unit
) {
    Column(modifier) {
        Logo(Modifier.align(Alignment.End), showText = logoShowText)
        Spacer(Modifier.height(24.dp))
        CompositionLocalProvider(LocalTextStyle provides titleStyle) {
            title()
        }
        CompositionLocalProvider(
            LocalTextStyle provides subtitleStyle,
            LocalContentColor provides HachimiTheme.colorScheme.onSurfaceVariant
        ) {
            Spacer(Modifier.height(8.dp))
            subtitle()
        }
        Spacer(Modifier.height(24.dp))
        content()
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        FormContent(
            title = { Text("成为神人") },
            subtitle = { Text("验证码已发送到您的邮箱") }
        ) {

        }
    }
}