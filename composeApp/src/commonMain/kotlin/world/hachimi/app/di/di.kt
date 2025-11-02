package world.hachimi.app.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import world.hachimi.app.model.AuthViewModel
import world.hachimi.app.model.ForgetPasswordViewModel
import world.hachimi.app.model.HomeViewModel
import world.hachimi.app.model.RecentPublishViewModel
import world.hachimi.app.model.MyArtworkViewModel
import world.hachimi.app.model.PlaylistDetailViewModel
import world.hachimi.app.model.PlaylistViewModel
import world.hachimi.app.model.PublishViewModel
import world.hachimi.app.model.RecentPlayViewModel
import world.hachimi.app.model.RecommendViewModel
import world.hachimi.app.model.ReviewDetailViewModel
import world.hachimi.app.model.ReviewViewModel
import world.hachimi.app.model.SearchViewModel
import world.hachimi.app.model.UserSpaceViewModel

fun Module.applyViewModels() {
    viewModelOf(::RecentPublishViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::PublishViewModel)
    viewModelOf(::MyArtworkViewModel)
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
}