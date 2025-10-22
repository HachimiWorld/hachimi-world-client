package world.hachimi.app.di

import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.ApiClient
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.player.IosPlayer
import world.hachimi.app.player.Player
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.MyDataStoreImpl
import world.hachimi.app.storage.SongCache
import world.hachimi.app.storage.SongCacheImpl

fun initKoin() {
    val koin = startKoin {
        modules(appModule)
    }
    val global = koin.koin.get<GlobalStore>()
    global.initialize()
}

val appModule = module {
    single { ApiClient(BuildKonfig.API_BASE_URL) }
    single<MyDataStore> { MyDataStoreImpl() }
    single<Player> { IosPlayer() }
    single<SongCache> { SongCacheImpl() }
    singleOf(::GlobalStore)
    applyViewModels()
}