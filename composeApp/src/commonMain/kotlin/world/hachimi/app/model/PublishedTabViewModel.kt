package world.hachimi.app.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import world.hachimi.app.api.ApiClient

class PublishedTabViewModel(
    private val api: ApiClient
): ViewModel(CoroutineScope(Dispatchers.Default)) {

}