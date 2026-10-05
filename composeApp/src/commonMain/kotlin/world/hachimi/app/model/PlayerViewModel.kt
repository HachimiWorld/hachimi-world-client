package world.hachimi.app.model

import androidx.lifecycle.ViewModel
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class PlayerViewModel(
    private val global: GlobalStore,
) : ViewModel() {
    val uiState: PlayerUIState
        get() = global.player.playerState

    val liked: Boolean
        get() = global.like.liked
    val likeLoading: Boolean
        get() = global.like.likeLoading
    val likeOperating: Boolean
        get() = global.like.likeOperating
    val likeEnabled: Boolean
        get() = global.like.likeEnabled

    fun toggleLike() = global.like.toggleLike()
}
