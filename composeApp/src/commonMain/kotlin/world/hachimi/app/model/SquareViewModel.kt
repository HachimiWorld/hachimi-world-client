package world.hachimi.app.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.KoinViewModel
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.FeedItem
import world.hachimi.app.api.module.FeedModule
import world.hachimi.app.api.ok

/** The square's moments tab: everyone's moments, newest first. @since 261106 */
@KoinViewModel
class SquareViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
    store: MomentStore,
) : ViewModel(CoroutineScope(Dispatchers.Default)) {
    val moments = MomentPager(
        scope = viewModelScope,
        global = global,
        store = store,
        acceptsPosted = { true },
    ) { cursor ->
        val resp = api.feedModule.square(FeedModule.PageReq(cursor = cursor), auth = global.isLoggedIn)
        if (!resp.ok) throw MomentLoadException(resp.err().msg)
        val data = resp.ok()
        // Kinds this version doesn't know are skipped
        MomentPage(data.items.filterIsInstance<FeedItem.Moment>().map { it.moment }, data.nextCursor)
    }
}
