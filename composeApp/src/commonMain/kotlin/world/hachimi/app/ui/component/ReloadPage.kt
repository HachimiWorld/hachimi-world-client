package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.reload_page_message
import hachimiworld.composeapp.generated.resources.reload_retry
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun ReloadPage(
    onReloadClick: () -> Unit,
    message: String? = null,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    Box(modifier, Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message ?: stringResource(Res.string.reload_page_message))
            Spacer(Modifier.height(12.dp))
            Button(onReloadClick) {
                Text(stringResource(Res.string.reload_retry))
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        ReloadPage(onReloadClick = {})
    }
}