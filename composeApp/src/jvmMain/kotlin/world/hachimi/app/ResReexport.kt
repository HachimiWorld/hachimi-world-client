package world.hachimi.app

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.app_menu_next
import hachimiworld.composeapp.generated.resources.app_menu_pause
import hachimiworld.composeapp.generated.resources.app_menu_play
import hachimiworld.composeapp.generated.resources.app_menu_previous
import hachimiworld.composeapp.generated.resources.app_menu_quit
import hachimiworld.composeapp.generated.resources.app_menu_show_window
import hachimiworld.composeapp.generated.resources.icon_vector

/**
 * FIXME: Helper class to re-export resources from the generated Res class,
 *  since the generated Res class is internal
 */
object ResReexport {
    val icon_vector = Res.drawable.icon_vector

    val app_menu_show_window = Res.string.app_menu_show_window
    val app_menu_quit = Res.string.app_menu_quit
    val app_menu_play = Res.string.app_menu_play
    val app_menu_pause = Res.string.app_menu_pause
    val app_menu_previous = Res.string.app_menu_previous
    val app_menu_next = Res.string.app_menu_next
}
