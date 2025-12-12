package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
            Text("基米好像去往了天堂")
            Spacer(Modifier.height(12.dp))
            Button(onReloadClick) {
                Text("重试")
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