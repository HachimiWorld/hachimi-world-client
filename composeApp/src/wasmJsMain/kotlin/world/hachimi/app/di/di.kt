package world.hachimi.app.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.ApiClient
import world.hachimi.app.model.AuthViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.MainViewModel
import world.hachimi.app.model.MyArtworkViewModel
import world.hachimi.app.model.PlaylistDetailViewModel
import world.hachimi.app.model.PlaylistViewModel
import world.hachimi.app.model.PublishViewModel
import world.hachimi.app.model.RecentPlayViewModel
import world.hachimi.app.model.ReviewDetailViewModel
import world.hachimi.app.model.ReviewViewModel
import world.hachimi.app.model.SearchViewModel
import world.hachimi.app.model.UserSpaceViewModel
import world.hachimi.app.player.Player
import world.hachimi.app.player.WasmPlayer
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.MyDataStoreImpl
import world.hachimi.app.storage.SongCache
import world.hachimi.app.storage.SongCacheImpl

val appModule = module {
    single {
        ApiClient(BuildKonfig.API_BASE_URL)
    }
    single<MyDataStore> { MyDataStoreImpl() }
    single<Player> { WasmPlayer() }
    single<SongCache> { SongCacheImpl() }
    singleOf(::GlobalStore)

    viewModelOf(::MainViewModel)
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
}
