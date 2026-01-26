package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.developing_page_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun DevelopingPage(modifier: Modifier = Modifier.fillMaxSize()) {
    Box(modifier, Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(Res.string.developing_page_title))
        }
    }
}