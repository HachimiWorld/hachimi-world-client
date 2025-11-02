package world.hachimi.app.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class TopViewModel: ViewModel(CoroutineScope(Dispatchers.Default)) {
    fun mounted() {

    }
}