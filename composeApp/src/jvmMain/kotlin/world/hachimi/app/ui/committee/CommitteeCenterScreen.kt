package world.hachimi.app.ui.committee

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hachimiworld.composeapp.generated.resources.Res

@Composable
fun CommitteeCenterScreen() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {

        Text("委员会中心正在开发中，敬请期待", style = MaterialTheme.typography.headlineLarge)
    }
}