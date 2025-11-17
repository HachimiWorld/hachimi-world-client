package world.hachimi.app.nav

import world.hachimi.app.model.SearchViewModel

sealed class Route {
    sealed class Root : Route() {
        companion object {
            val Default = Home.Main
        }
        sealed class Home: Root() {
            data object Main: Home()
            data object Recent: Home()
            data object Recommend: Home()
            data object WeeklyHot: Home()
            data object HiddenGem: Home()
            data class Category(val category: String): Home()
        }
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

            data object MyArtwork: CreationCenter()
            data object Publish: CreationCenter()
            data class ReviewDetail(val reviewId: Long): CreationCenter()
            data class ArtworkDetail(val songId: Long): CreationCenter()
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