package world.hachimi.app.ui

/**
 * Stable Compose [androidx.compose.ui.platform.testTag] ids for UI Automator / Macrobenchmark.
 *
 * On Android, enable `semantics { testTagsAsResourceId = true }` on the root so these map to
 * accessibility `resource-id` (use [simpleViewResourceName] / [viewIdResourceName] in tests).
 */
object TestTags {
    const val NAV_MENU = "nav_menu"
    const val NAV_HOME = "nav_home"
    const val NAV_EVENTS = "nav_events"
    const val NAV_RECENT_PLAY = "nav_recent_play"
    const val NAV_RECENT_LIKE = "nav_recent_like"
    const val NAV_MY_FOLLOWS = "nav_my_follows"
    const val NAV_MY_PLAYLISTS = "nav_my_playlists"
    const val NAV_CREATION = "nav_creation"
    const val NAV_COMMITTEE = "nav_committee"
    const val NAV_CONTRIBUTOR = "nav_contributor"
    const val NAV_SETTINGS = "nav_settings"

    const val HOME_FEED = "home_feed"
    const val HOME_RECENT_SECTION = "home_recent_section"
    const val HOME_RECOMMEND_SECTION = "home_recommend_section"
    const val HOME_WEEKLY_SECTION = "home_weekly_section"
    /** Segment "更多" for 最近发布 */
    const val HOME_RECENT_MORE = "home_recent_more"
    /** Segment "更多" for 本周热门 */
    const val HOME_WEEKLY_MORE = "home_weekly_more"

    const val SONG_CARD = "song_card"

    const val MINI_PLAYER = "mini_player"
    const val MINI_PLAYER_PLAY = "mini_player_play"
    const val MINI_PLAYER_NEXT = "mini_player_next"

    const val PLAYER_SCREEN = "player_screen"
    const val PLAYER_SHRINK = "player_shrink"

    const val SETTINGS_SCREEN = "settings_screen"

    /** Top bar avatar (guest → Auth, logged-in → UserSpace) */
    const val PROFILE_AVATAR = "profile_avatar"
    const val AUTH_SCREEN = "auth_screen"
    const val AUTH_BACK = "auth_back"
}
