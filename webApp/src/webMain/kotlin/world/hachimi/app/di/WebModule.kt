package world.hachimi.app.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.ApiClient
import world.hachimi.app.model.ArtworkDetailViewModel
import world.hachimi.app.model.AuthViewModel
import world.hachimi.app.model.CategorySongsViewModel
import world.hachimi.app.model.ChangelogViewModel
import world.hachimi.app.model.ContributorEntryViewModel
import world.hachimi.app.model.CreatePostViewModel
import world.hachimi.app.model.DeviceManagementViewModel
import world.hachimi.app.model.EditProfileViewModel
import world.hachimi.app.model.EventDetailViewModel
import world.hachimi.app.model.EventsListViewModel
import world.hachimi.app.model.FollowViewModel
import world.hachimi.app.model.ForgetPasswordViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.HomeViewModel
import world.hachimi.app.model.MyPRViewModel
import world.hachimi.app.model.PlayerViewModel
import world.hachimi.app.model.PlaylistDetailViewModel
import world.hachimi.app.model.PlaylistViewModel
import world.hachimi.app.model.PublicPlaylistViewModel
import world.hachimi.app.model.PublishViewModel
import world.hachimi.app.model.PublishedTabViewModel
import world.hachimi.app.model.RecentLikeViewModel
import world.hachimi.app.model.RecentPlayViewModel
import world.hachimi.app.model.RecentPublishViewModel
import world.hachimi.app.model.RecommendViewModel
import world.hachimi.app.model.ReviewDetailViewModel
import world.hachimi.app.model.ReviewHistoryViewModel
import world.hachimi.app.model.ReviewViewModel
import world.hachimi.app.model.SearchViewModel
import world.hachimi.app.model.UserSpaceViewModel
import world.hachimi.app.model.WeeklyHotViewModel
import world.hachimi.app.player.PlayerEngine
import world.hachimi.app.player.WebPlayerEngine
import world.hachimi.app.player.WebPlayerHelper
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.MyDataStoreImpl
import world.hachimi.app.storage.SongCache
import world.hachimi.app.storage.SongCacheImpl

/*@Module
//@ComponentScan("world.hachimi.app")
class WebModule {
    @Singleton
    fun provideApiClient(): ApiClient {
        return ApiClient(BuildKonfig.API_BASE_URL)
    }

    @Singleton
    fun provideMyDataStore(): MyDataStore {
        return MyDataStoreImpl()
    }

    @Singleton
    fun providePlayerEngine(): PlayerEngine {
        return WebPlayerEngine()
    }

    @Singleton
    fun provideSongCache(): SongCache {
        return SongCacheImpl()
    }

    @Singleton
    fun provideWebPlayerHelper(globalStore: GlobalStore): WebPlayerHelper {
        return WebPlayerHelper(globalStore.player)
    }
}*/

// FIXME: The koin compiler plugin doesn't work in web target for some reason, so we have to define the module manually
//  @ComponentScan throws multiple definition error for some reason
val webModule = module {
    single { ApiClient(BuildKonfig.API_BASE_URL) }
    single<MyDataStore> { MyDataStoreImpl() }
    single<PlayerEngine> { WebPlayerEngine() }
    single<SongCache> { SongCacheImpl() }
    singleOf(::GlobalStore)
    single { WebPlayerHelper(get<GlobalStore>().player) }

    applyViewModels()
}

fun org.koin.core.module.Module.applyViewModels() {
    viewModelOf(::RecentPublishViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::PublishViewModel)
    viewModelOf(::MyPRViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::UserSpaceViewModel)
    viewModelOf(::EditProfileViewModel)
    viewModelOf(::PlaylistViewModel)
    viewModelOf(::PlaylistDetailViewModel)
    viewModelOf(::PlayerViewModel)
    viewModelOf(::RecentPlayViewModel)
    viewModelOf(::RecentLikeViewModel)
    viewModelOf(::ReviewViewModel)
    viewModelOf(::ReviewDetailViewModel)
    viewModelOf(::ReviewHistoryViewModel)
    viewModelOf(::ForgetPasswordViewModel)
    viewModelOf(::RecommendViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::WeeklyHotViewModel)
    viewModelOf(::CategorySongsViewModel)
    viewModelOf(::PublishedTabViewModel)
    viewModelOf(::ArtworkDetailViewModel)
    viewModelOf(::PublicPlaylistViewModel)
    viewModelOf(::ContributorEntryViewModel)
    viewModelOf(::CreatePostViewModel)
    viewModelOf(::EventsListViewModel)
    viewModelOf(::EventDetailViewModel)
    viewModelOf(::ChangelogViewModel)
    viewModelOf(::FollowViewModel)
    viewModelOf(::DeviceManagementViewModel)
}