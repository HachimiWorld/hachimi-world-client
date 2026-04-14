package world.hachimi.app.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import world.hachimi.app.model.SearchViewModel

@Serializable
sealed interface Route : NavKey {
    @Serializable
    sealed interface Root : Route {
        companion object {
            val Default = Home.Main
        }

        @Serializable
        sealed interface Events : Root {
            @Serializable
            @SerialName("/events/feed")
            data object Feed : Events

            @Serializable
            @SerialName("/events/feed")
            data class Detail(val postId: Long) : Events
        }

        @Serializable
        sealed interface Home : Root {
            @Serializable
            @SerialName("/home/main")
            data object Main : Home

            @Serializable
            @SerialName("/home/recent")
            data object Recent : Home

            @Serializable
            @SerialName("/home/recommend")
            data object Recommend : Home

            @Serializable
            @SerialName("/home/weekly_hot")
            data object WeeklyHot : Home

            @Serializable
            @SerialName("/home/hidden_gem")
            data object HiddenGem : Home

            @Serializable
            @SerialName("/home/category")
            data class Category(val category: String) : Home
        }

        @Serializable
        @SerialName("/recent_play")
        data object RecentPlay : Root

        @Serializable
        @SerialName("/recent_like")
        data object RecentLike : Root

        @Serializable
        @SerialName("/my_subscribe")
        data object MySubscribe : Root
        @Serializable
        sealed interface MyPlaylist : Root {
            companion object {
                val Default = List
            }

            @Serializable
            @SerialName("/my_playlist")
            data object List : MyPlaylist
            @Serializable
            @SerialName("/my_playlist/detail")
            data class Detail(val playlistId: Long) : MyPlaylist
        }

        @Serializable
        @SerialName("/playlist")
        data class PublicPlaylist(val playlistId: Long) : Root

        @Serializable
        sealed interface CreationCenter : Root {
            companion object Companion {
                val Default = MyArtwork
            }

            @Serializable
            @SerialName("/creator_center/my_artwork")
            data object MyArtwork : CreationCenter

            @Serializable
            @SerialName("/creator_center/publish")
            data object Publish : CreationCenter

            @Serializable
            @SerialName("/creator_center/review")
            data class ReviewDetail(val reviewId: Long) : CreationCenter

            @Serializable
            @SerialName("/creator_center/review/modify")
            data class ReviewModify(val reviewId: Long) : CreationCenter

            @Serializable
            @SerialName("/creator_center/review/history")
            data class ReviewHistory(val reviewId: Long) : CreationCenter


            @Serializable
            @SerialName("/creator_center/artwork_detail")
            data class ArtworkDetail(val songId: Long) : CreationCenter

            @Serializable
            @SerialName("/creator_center/artwork_modify")
            data class Modify(val songId: Long) : CreationCenter
        }

        @Serializable
        @SerialName("/committee_center")
        data object CommitteeCenter : Root

        @Serializable
        sealed interface ContributorCenter : Root {
            companion object {
                val Default = Entry
            }

            @Serializable
            @SerialName("/contributor_center")
            data object Entry : ContributorCenter


            @Serializable
            @SerialName("/contributor_center/review_list")
            data object ReviewList : ContributorCenter

            @Serializable
            @SerialName("/contributor_center/review_detail")
            data class ReviewDetail(val reviewId: Long) : ContributorCenter

            @Serializable
            @SerialName("/contributor_center/review_modify")
            data class ReviewModify(val reviewId: Long) : ContributorCenter

            @Serializable
            @SerialName("/contributor_center/review_history")
            data class ReviewHistory(val reviewId: Long) : ContributorCenter

            @Serializable
            @SerialName("/contributor_center/post")
            data object PostCenter : ContributorCenter

            @Serializable
            @SerialName("/contributor_center/post/create")
            data object CreatePost : ContributorCenter

            @Serializable
            @SerialName("/contributor_center/post/edit")
            data class EditPost(val postId: Long) : ContributorCenter
        }

        @Serializable
        @SerialName("/search")
        data class Search(
            val query: String,
            val type: SearchViewModel.SearchType = SearchViewModel.SearchType.SONG
        ) : Root

        @Serializable
        @SerialName("/space")
        data object UserSpace : Root

        @Serializable
        @SerialName("/user_space")
        data class PublicUserSpace(val userId: Long) : Root

        @Serializable
        @SerialName("/edit_profile")
        data object EditProfile : Root

        @Serializable
        @SerialName("/settings")
        data object Settings : Root

        @Serializable
        @SerialName("/changelog")
        data object Changelog : Root
    }

    @Serializable
    @SerialName("/auth")
    data class Auth(val initialLogin: Boolean = true) : Route

    @Serializable
    @SerialName("/forget_password")
    data object ForgetPassword : Route
}