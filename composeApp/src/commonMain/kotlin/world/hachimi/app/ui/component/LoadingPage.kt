package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun LoadingPage(modifier: Modifier = Modifier.fillMaxSize(), ) {
    Box(modifier, Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        LoadingPage()
    }
}