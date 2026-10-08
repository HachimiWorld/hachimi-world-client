package world.hachimi.app.model

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.notification_action_edit_profile
import hachimiworld.composeapp.generated.resources.notification_action_view_artwork
import hachimiworld.composeapp.generated.resources.notification_action_view_playlist
import hachimiworld.composeapp.generated.resources.notification_action_view_review
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.jetbrains.compose.resources.StringResource
import world.hachimi.app.api.module.NotificationModule
import world.hachimi.app.nav.Route

/** Where a notification's follow-up button goes, and what it says. */
data class NotificationTarget(
    val route: Route,
    val label: StringResource,
)

/**
 * Maps a content intent to a page. Returns null for actions this version doesn't know or data it
 * can't read, so the button is hidden. Add an action here together with the server producer that
 * first sends it.
 */
fun NotificationModule.ContentIntent.toTarget(): NotificationTarget? = when (action) {
    "profile.edit" -> NotificationTarget(Route.Root.EditProfile, Res.string.notification_action_edit_profile)
    "creation.review.view" -> data.long("review_id")?.let {
        NotificationTarget(Route.Root.CreationCenter.ReviewDetail(it), Res.string.notification_action_view_review)
    }
    "creation.artwork.view" -> data.long("song_id")?.let {
        NotificationTarget(Route.Root.CreationCenter.ArtworkDetail(it), Res.string.notification_action_view_artwork)
    }
    "playlist.view" -> data.long("playlist_id")?.let {
        NotificationTarget(Route.Root.MyPlaylist.Detail(it), Res.string.notification_action_view_playlist)
    }
    else -> null
}

private fun JsonObject.long(key: String): Long? = (get(key) as? JsonPrimitive)?.longOrNull
