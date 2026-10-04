package world.hachimi.app.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.compose.runtime.snapshotFlow
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import world.hachimi.app.MainActivity
import world.hachimi.app.R
import world.hachimi.app.model.CurrentSongLike
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PlayerService

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService(), MediaSession.Callback {
    private var mediaSession: MediaSession? = null
    private val global: GlobalStore = get()
    private var globalPlayer: PlayerService = global.player
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    companion object {
        private val LIKE_COMMAND = SessionCommand("world.hachimi.app.LIKE", Bundle.EMPTY)
    }

    override fun onCreate() {
        super.onCreate()

        val userAgentString = "HachimiWorld-android"
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(mapOf("Referer" to "https://hachimi.world/"))
            .setUserAgent(userAgentString)
        val dataSourceFactory = DefaultDataSource.Factory(
            this,
            httpDataSourceFactory
        )

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()
        player.addAnalyticsListener(EventLogger())

        val sessionPlayer = QueueForwardingPlayer(player, globalPlayer)
        val session = MediaSession.Builder(this, sessionPlayer)
            .setCallback(this)
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
        mediaSession = session

        // Previous/next are drawn by the system, the extra slots hold like and shuffle
        scope.launch {
            snapshotFlow { ExtraButtonsState(global.like.likeAvailable(), global.like.liked, globalPlayer.shuffleMode) }
                .distinctUntilChanged()
                .collect { state ->
                    sessionPlayer.refresh()
                    session.setMediaButtonPreferences(extraButtons(state))
                }
        }
    }

    private data class ExtraButtonsState(val likeAvailable: Boolean, val liked: Boolean, val shuffleOn: Boolean)

    private fun extraButtons(state: ExtraButtonsState): List<CommandButton> = buildList {
        if (state.likeAvailable) {
            add(
                CommandButton.Builder(if (state.liked) CommandButton.ICON_HEART_FILLED else CommandButton.ICON_HEART_UNFILLED)
                    .setDisplayName(getString(if (state.liked) R.string.media_unlike else R.string.media_like))
                    .setSessionCommand(LIKE_COMMAND)
                    .setSlots(CommandButton.SLOT_OVERFLOW)
                    .build()
            )
        }
        add(
            CommandButton.Builder(if (state.shuffleOn) CommandButton.ICON_SHUFFLE_ON else CommandButton.ICON_SHUFFLE_OFF)
                .setDisplayName(getString(if (state.shuffleOn) R.string.media_shuffle_off else R.string.media_shuffle_on))
                .setPlayerCommand(Player.COMMAND_SET_SHUFFLE_MODE)
                .setSlots(CommandButton.SLOT_OVERFLOW)
                .build()
        )
    }

    // Liking needs an account, and the login prompt would only show inside the app
    private fun CurrentSongLike.likeAvailable() = global.isLoggedIn && currentSongId != null

    override fun onGetSession(p0: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        scope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }


    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        // Since Media3 1.11 the default onConnect returns a placeholder with no commands, so start from the
        // documented defaults: full access for trusted controllers (this app, the system), read-only otherwise.
        // Previous/next and shuffle come from the player itself, see QueueForwardingPlayer.
        val builder = MediaSession.ConnectionResult.AcceptedResultBuilder(session, controller)
        if (controller.isTrusted) {
            builder.setAvailableSessionCommands(
                MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().add(LIKE_COMMAND).build()
            )
        }
        return builder.build()
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        if (customCommand.customAction == LIKE_COMMAND.customAction) {
            global.like.toggleLike()
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
        return super.onCustomCommand(session, controller, customCommand, args)
    }

    override fun onMediaButtonEvent(
        session: MediaSession,
        controllerInfo: MediaSession.ControllerInfo,
        intent: Intent
    ): Boolean {
        val event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
        } else {
            intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
        } ?: return super.onMediaButtonEvent(session, controllerInfo, intent)

        return when (event.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE,
            KeyEvent.KEYCODE_HEADSETHOOK -> {
                if (event.action == KeyEvent.ACTION_UP) globalPlayer.playOrPause()
                true
            }

            // Everything else, including next/previous, goes to Media3, which turns them into seeks on
            // QueueForwardingPlayer
            else -> super.onMediaButtonEvent(session, controllerInfo, intent)
        }
    }
}