package world.hachimi.app.player

import web.mediasession.MediaSession
import web.mediasession.MediaSessionAction
import web.mediasession.nexttrack
import web.mediasession.pause
import web.mediasession.play
import web.mediasession.previoustrack
import web.mediasession.stop
import web.navigator.navigator
import world.hachimi.app.model.PlayerService

class WebPlayerHelper(
    private val playerService: PlayerService
) {
    fun initialize() {
        val mediaSession: MediaSession? = navigator.mediaSession
        mediaSession?.let { mediaSession ->
            mediaSession.setActionHandler(MediaSessionAction.play) {
                playerService.playOrPause()
            }
            mediaSession.setActionHandler(MediaSessionAction.pause) {
                // Damn. I should separate it to play()/pause()
                playerService.playOrPause()
            }
            mediaSession.setActionHandler(MediaSessionAction.stop) {
                playerService.playOrPause()
            }
            mediaSession.setActionHandler(MediaSessionAction.previoustrack) {
                playerService.previous()
            }
            mediaSession.setActionHandler(MediaSessionAction.nexttrack) {
                playerService.next()
            }
        }
    }
}

