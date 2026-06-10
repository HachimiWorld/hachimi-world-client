package world.hachimi.app.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.ApiClient
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.player.IosPlayerEngine
import world.hachimi.app.player.IosPlayerServiceHelper
import world.hachimi.app.player.PlayerEngine
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.MyDataStoreImpl
import world.hachimi.app.storage.SongCache
import world.hachimi.app.storage.SongCacheImpl

@Module
@ComponentScan("world.hachimi.app")
class IosModule {
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
        return IosPlayerEngine()
    }

    @Singleton
    fun provideSongCache(): SongCache {
        return SongCacheImpl()
    }

    @Singleton
    fun provideIosPlayerServiceHelper(globalStore: GlobalStore): IosPlayerServiceHelper {
        return IosPlayerServiceHelper(globalStore.player)
    }
}