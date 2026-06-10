package world.hachimi.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toOkioPath
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import world.hachimi.app.BuildKonfig
import world.hachimi.app.JVMPlatform
import world.hachimi.app.api.ApiClient
import world.hachimi.app.player.PlayerEngine
import world.hachimi.app.player.RustPlayerEngine
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.MyDataStoreImpl
import world.hachimi.app.storage.SongCache
import world.hachimi.app.storage.SongCacheImpl

@Module
@ComponentScan("world.hachimi.app")
class JvmModule {
    @Singleton
    fun provideApiClient(): ApiClient {
        return ApiClient(BuildKonfig.API_BASE_URL)
    }

    @Singleton
    fun provideDateStore(): DataStore<Preferences> {
        return getPreferencesDataStore()
    }

    @Singleton
    fun provideMyDataStore(dataStore: DataStore<Preferences>): MyDataStore {
        return MyDataStoreImpl(dataStore)
    }

    @Singleton
    fun providePlayerEngine(): PlayerEngine {
        return RustPlayerEngine()
    }

    @Singleton
    fun provideSongCache(): SongCache {
        return SongCacheImpl()
    }
}

private fun getPreferencesDataStore(): DataStore<Preferences> {
    val file = JVMPlatform.getDataDir().file.resolve("settings.preferences_pb")

    return PreferenceDataStoreFactory.createWithPath { file.toOkioPath() }
}