package world.hachimi.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toOkioPath
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import world.hachimi.app.BuildKonfig
import world.hachimi.app.JVMPlatform
import world.hachimi.app.api.ApiClient
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.player.JVMPlayer
import world.hachimi.app.player.Player
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.MyDataStoreImpl
import world.hachimi.app.storage.SongCache
import world.hachimi.app.storage.SongCacheImpl

val appModule = module {
    single { ApiClient(BuildKonfig.API_BASE_URL) }
    single { getPreferencesDataStore() }
    single<MyDataStore> { MyDataStoreImpl(get()) }
    single<Player> { JVMPlayer(get()) }
    single<SongCache> { SongCacheImpl() }

    singleOf(::GlobalStore)

    applyViewModels()
}

private fun getPreferencesDataStore(): DataStore<Preferences> {
    val file = JVMPlatform.getDataDir().file.resolve("settings.preferences_pb")

    return PreferenceDataStoreFactory.createWithPath { file.toOkioPath() }
}