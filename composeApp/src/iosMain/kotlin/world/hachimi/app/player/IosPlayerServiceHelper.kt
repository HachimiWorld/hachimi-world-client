package world.hachimi.app.player

import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess
import world.hachimi.app.model.PlayerService

class IosPlayerServiceHelper(
    private val playerService: PlayerService
) {
    fun initialize() {
        val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()
        commandCenter.playCommand.enabled = true
        commandCenter.playCommand.addTargetWithHandler {
            playerService.playOrPause()
            MPRemoteCommandHandlerStatusSuccess
        }
        commandCenter.pauseCommand.enabled = true
        commandCenter.pauseCommand.addTargetWithHandler {
            playerService.playOrPause()
            MPRemoteCommandHandlerStatusSuccess
        }

        commandCenter.nextTrackCommand.enabled = true
        commandCenter.nextTrackCommand.addTargetWithHandler {
            playerService.next()
            MPRemoteCommandHandlerStatusSuccess
        }

        commandCenter.previousTrackCommand.enabled = true
        commandCenter.previousTrackCommand.addTargetWithHandler {
            playerService.previous()
            MPRemoteCommandHandlerStatusSuccess
        }
    }
}