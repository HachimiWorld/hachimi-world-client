package world.hachimi.app.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.ApiClient
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.player.PlayerEngine
import world.hachimi.app.player.WebPlayerEngine
import world.hachimi.app.player.WebPlayerHelper
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.MyDataStoreImpl
import world.hachimi.app.storage.SongCache
import world.hachimi.app.storage.SongCacheImpl

val appModule = module {
    single {
        ApiClient(BuildKonfig.API_BASE_URL)
    }
    single<MyDataStore> { MyDataStoreImpl() }
    single<PlayerEngine> { WebPlayerEngine() }
    single<SongCache> { SongCacheImpl() }
    singleOf(::GlobalStore)
    single {
        WebPlayerHelper(get<GlobalStore>().player)
    }

    applyViewModels()
}

