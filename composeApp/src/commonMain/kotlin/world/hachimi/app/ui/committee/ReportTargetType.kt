package world.hachimi.app.ui.committee

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.report_action_hide
import hachimiworld.composeapp.generated.resources.report_action_hide_playlist_desc
import hachimiworld.composeapp.generated.resources.report_action_hide_song_desc
import hachimiworld.composeapp.generated.resources.report_action_reset_avatar
import hachimiworld.composeapp.generated.resources.report_action_reset_bio
import hachimiworld.composeapp.generated.resources.report_action_reset_username
import hachimiworld.composeapp.generated.resources.report_action_reset_username_desc
import hachimiworld.composeapp.generated.resources.report_action_restore
import hachimiworld.composeapp.generated.resources.report_open_song
import hachimiworld.composeapp.generated.resources.report_open_target
import hachimiworld.composeapp.generated.resources.report_type_playlist
import hachimiworld.composeapp.generated.resources.report_type_song
import hachimiworld.composeapp.generated.resources.report_type_user
import org.jetbrains.compose.resources.StringResource
import world.hachimi.app.api.module.ReportModule
import world.hachimi.app.model.PlayerService
import world.hachimi.app.nav.Navigator
import world.hachimi.app.nav.Route

/**
 * The kinds of content that can be reported, as the committee center shows them. This is the only
 * place the client branches on a report target's type: adding a kind means a new entry here.
 */
enum class ReportTargetType(
    val value: String,
    val label: StringResource,
    /** Shown when the target has no cover. */
    val placeholderIcon: ImageVector,
    /** Avatars are round; covers aren't. */
    val roundCover: Boolean,
    /** Whether it has an owner other than itself worth showing. */
    val showsOwner: Boolean,
    val openLabel: StringResource,
    /** How to show the content actions this kind declares, by id. */
    val actions: Map<String, ActionText>,
) {
    Song(
        value = ReportModule.TARGET_SONG,
        label = Res.string.report_type_song,
        placeholderIcon = Icons.Default.MusicNote,
        roundCover = false,
        showsOwner = true,
        openLabel = Res.string.report_open_song,
        actions = mapOf(
            "hide" to ActionText(Res.string.report_action_hide, Res.string.report_action_hide_song_desc),
            "restore" to ActionText(Res.string.report_action_restore),
        ),
    ) {
        override fun open(targetId: Long, target: ReportModule.TargetInfo, navigator: Navigator, player: PlayerService) {
            player.insertToQueueWithFetch(target.displayId, instantPlay = true, append = false)
        }
    },
    Playlist(
        value = ReportModule.TARGET_PLAYLIST,
        label = Res.string.report_type_playlist,
        placeholderIcon = Icons.AutoMirrored.Filled.QueueMusic,
        roundCover = false,
        showsOwner = true,
        openLabel = Res.string.report_open_target,
        actions = mapOf(
            "hide" to ActionText(Res.string.report_action_hide, Res.string.report_action_hide_playlist_desc),
            "restore" to ActionText(Res.string.report_action_restore),
        ),
    ) {
        override fun open(targetId: Long, target: ReportModule.TargetInfo, navigator: Navigator, player: PlayerService) {
            navigator.push(Route.Root.PublicPlaylist(targetId))
        }
    },
    User(
        value = ReportModule.TARGET_USER,
        label = Res.string.report_type_user,
        placeholderIcon = Icons.Default.Person,
        roundCover = true,
        showsOwner = false,
        openLabel = Res.string.report_open_target,
        actions = mapOf(
            "reset_avatar" to ActionText(Res.string.report_action_reset_avatar),
            "reset_bio" to ActionText(Res.string.report_action_reset_bio),
            "reset_username" to ActionText(Res.string.report_action_reset_username, Res.string.report_action_reset_username_desc),
        ),
    ) {
        override fun open(targetId: Long, target: ReportModule.TargetInfo, navigator: Navigator, player: PlayerService) {
            navigator.push(Route.Root.PublicUserSpace(targetId))
        }
    };

    /** Opens the target: plays a song, or goes to its page. */
    abstract fun open(targetId: Long, target: ReportModule.TargetInfo, navigator: Navigator, player: PlayerService)

    /** How a content action is shown. */
    data class ActionText(val label: StringResource, val description: StringResource? = null)

    companion object {
        /** Null for kinds this version doesn't know. */
        fun of(value: String): ReportTargetType? = entries.firstOrNull { it.value == value }
    }
}
