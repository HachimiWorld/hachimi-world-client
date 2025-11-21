package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun NeedLoginScreen(modifier: Modifier = Modifier.fillMaxSize()) {
    Box(modifier, Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("登录后才可以使用哦")
        }
    }
}

@Composable
@Preview
private fun Preview() {
    PreviewTheme(background = true) {
        NeedLoginScreen()
    }
}