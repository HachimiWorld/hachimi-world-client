package world.hachimi.app.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import world.hachimi.app.model.SearchViewModel

sealed class Route: NavKey {
    sealed class Root : Route() {
        companion object {
            val Default = Home.Main
        }
        sealed class Events: Root() {
            @Serializable
            data object Feed: Events()
            @Serializable
            data class Detail(val postId: Long): Events()
        }

        sealed class Home: Root() {
            @Serializable
            data object Main: Home()
            @Serializable
            data object Recent: Home()
            @Serializable
            data object Recommend: Home()
            @Serializable
            data object WeeklyHot: Home()
            @Serializable
            data object HiddenGem: Home()
            @Serializable
            data class Category(val category: String): Home()
        }

        @Serializable
        data object RecentPlay: Root()
        @Serializable
        data object RecentLike: Root()
        @Serializable
        data object MySubscribe: Root()
        sealed class MyPlaylist: Root() {
            companion object {
                val Default = List
            }
            @Serializable
            data object List: MyPlaylist()
            @Serializable
            data class Detail(val playlistId: Long): MyPlaylist()
        }

        @Serializable
        data class PublicPlaylist(val playlistId: Long): Root()

        sealed class CreationCenter: Root() {
            companion object Companion {
                val Default = MyArtwork
            }

            @Serializable
            data object MyArtwork: CreationCenter()
            @Serializable
            data object Publish: CreationCenter()
            @Serializable
            data class ReviewDetail(val reviewId: Long): CreationCenter()
            @Serializable
            data class ArtworkDetail(val songId: Long): CreationCenter()
            @Serializable
            data class Modify(val songId: Long): CreationCenter()
        }
        @Serializable
        data object CommitteeCenter: Root()
        @Serializable
        sealed class ContributorCenter: Root() {
            companion object {
                val Default = Entry
            }
            @Serializable
            data object Entry: ContributorCenter()
            @Serializable
            data object ReviewList: ContributorCenter()
            @Serializable
            data class ReviewDetail(val reviewId: Long): ContributorCenter()
            @Serializable
            data object PostCenter: ContributorCenter()
            @Serializable
            data object CreatePost: ContributorCenter()
            @Serializable
            data class EditPost(val postId: Long): ContributorCenter()
        }

        @Serializable
        data class Search(
            val query: String,
            val type: SearchViewModel.SearchType = SearchViewModel.SearchType.SONG
        ): Root()
        @Serializable
        data object UserSpace: Root()
        @Serializable
        data class PublicUserSpace(val userId: Long): Root()
        @Serializable
        data object Settings: Root()
    }
    @Serializable
    data class Auth(val initialLogin: Boolean = true) : Route()
    @Serializable
    data object ForgetPassword: Route()
}