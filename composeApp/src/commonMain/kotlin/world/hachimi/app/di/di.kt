package world.hachimi.app.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import world.hachimi.app.model.*

fun Module.applyViewModels() {
    viewModelOf(::RecentPublishViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::PublishViewModel)
    viewModelOf(::MyPRViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::UserSpaceViewModel)
    viewModelOf(::PlaylistViewModel)
    viewModelOf(::PlaylistDetailViewModel)
    viewModelOf(::RecentPlayViewModel)
    viewModelOf(::ReviewViewModel)
    viewModelOf(::ReviewDetailViewModel)
    viewModelOf(::ForgetPasswordViewModel)
    viewModelOf(::RecommendViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::WeeklyHotViewModel)
    viewModelOf(::CategorySongsViewModel)
    viewModelOf(::PublishedTabViewModel)
    viewModelOf(::ArtworkDetailViewModel)
}