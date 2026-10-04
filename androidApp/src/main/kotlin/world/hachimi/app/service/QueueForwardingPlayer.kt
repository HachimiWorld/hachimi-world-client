package world.hachimi.app.service

import androidx.annotation.OptIn
import androidx.media3.common.ForwardingSimpleBasePlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import world.hachimi.app.model.PlayerService

/**
 * The queue lives in [PlayerService] and the wrapped player only ever holds the current song, so it never offers
 * previous/next or shuffle by itself. Advertise them here so the system draws its own controls (notification, lock
 * screen, Bluetooth, car), and route them to the app queue.
 */
@OptIn(UnstableApi::class)
class QueueForwardingPlayer(
    player: Player,
    private val queue: PlayerService,
) : ForwardingSimpleBasePlayer(player) {

    override fun getState(): State {
        val state = super.getState()
        val commands = state.availableCommands.buildUpon()
            .addAll(*QUEUE_COMMANDS)
            .build()
        return state.buildUpon()
            .setAvailableCommands(commands)
            .setShuffleModeEnabled(queue.shuffleMode)
            .build()
    }

    /** Re-reads the queue state, call it after the queue changed outside of this player */
    fun refresh() = invalidateState()

    override fun handleSeek(mediaItemIndex: Int, positionMs: Long, seekCommand: Int): ListenableFuture<*> {
        when (seekCommand) {
            COMMAND_SEEK_TO_NEXT, COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> queue.next()
            COMMAND_SEEK_TO_PREVIOUS, COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> queue.previous()
            else -> return super.handleSeek(mediaItemIndex, positionMs, seekCommand)
        }
        return Futures.immediateVoidFuture()
    }

    override fun handleSetShuffleModeEnabled(shuffleModeEnabled: Boolean): ListenableFuture<*> {
        queue.updateShuffleMode(shuffleModeEnabled)
        return Futures.immediateVoidFuture()
    }

    private companion object {
        val QUEUE_COMMANDS = intArrayOf(
            COMMAND_SEEK_TO_PREVIOUS,
            COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
            COMMAND_SEEK_TO_NEXT,
            COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
            COMMAND_SET_SHUFFLE_MODE,
        )
    }
}
