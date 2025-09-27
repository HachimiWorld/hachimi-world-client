package world.hachimi.app.nav

import world.hachimi.app.model.SearchViewModel

sealed class Route {
    sealed class Root : Route() {
        companion object {
            val Default = Home
        }
        data object Home: Root()
        data object RecentPlay: Root()
        data object RecentLike: Root()
        data object MySubscribe: Root()
        sealed class MyPlaylist: Root() {
            companion object {
                val Default = List
            }
            data object List: MyPlaylist()
            data class Detail(val playlistId: Long): MyPlaylist()
        }
        sealed class CreationCenter: Root() {
            companion object Companion {
                val Default = MyArtwork
            }

            object MyArtwork: CreationCenter()
            object Publish: CreationCenter()
        }
        data object CommitteeCenter: Root()
        sealed class ContributorCenter: Root() {
            companion object {
                val Default = ReviewList
            }
            data object ReviewList: ContributorCenter()
            data class ReviewDetail(val reviewId: Long): ContributorCenter()
        }

        data class Search(
            val query: String,
            val type: SearchViewModel.SearchType = SearchViewModel.SearchType.SONG
        ): Root()
        data object UserSpace: Root()
        data class PublicUserSpace(val userId: Long): Root()
        data object Settings: Root()
    }
    data class Auth(val initialLogin: Boolean = true) : Route()
    data object ForgetPassword: Route()
}