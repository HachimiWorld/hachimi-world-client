package world.hachimi.app.ui.creation.artworkdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.ArtworkDetailViewModel

private enum class Tab(
    val title: String
) {

}
@Composable
fun ArtworkDetailScreen(
    songId: Long,
    vm: ArtworkDetailViewModel = koinViewModel()
) {
    Column(Modifier.fillMaxSize()) {
        Text("作品详情")


    }
}