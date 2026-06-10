package world.hachimi.app.di

import android.content.ComponentName
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import io.github.vinceglb.filekit.AndroidFile
import okio.Path.Companion.toOkioPath
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.ApiClient
import world.hachimi.app.getPlatform
import world.hachimi.app.player.AndroidPlayerEngine
import world.hachimi.app.player.PlayerEngine
import world.hachimi.app.service.PlaybackService
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.MyDataStoreImpl
import world.hachimi.app.storage.SongCache
import world.hachimi.app.storage.SongCacheImpl

@Module
@ComponentScan("world.hachimi.app")
class AndroidModule {
    @Singleton
    fun provideApiClient(): ApiClient {
        return ApiClient(BuildKonfig.API_BASE_URL)
    }

    @Singleton
    fun provideDataStore(): DataStore<Preferences> {
        return getPreferencesDataStore()
    }

    @Singleton
    fun provideMyDataStore(dataStore: DataStore<Preferences>): MyDataStore {
        return MyDataStoreImpl(dataStore)
    }

    @Singleton
    fun providePlayerEngine(context: Context): PlayerEngine {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        return AndroidPlayerEngine(controllerFuture)
    }

    @Singleton
    fun provideSongCache(): SongCache {
        return SongCacheImpl()
    }
}

private fun getPreferencesDataStore(): DataStore<Preferences> {
    val file = (getPlatform().getDataDir().androidFile as AndroidFile.FileWrapper)
        .file.resolve("settings.preferences_pb")

    return PreferenceDataStoreFactory.createWithPath { file.toOkioPath() }
}

/*
private suspend fun getPlayerBlocking(context: Context): Player {
    val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
    val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
    Logger.i("player", "Waiting for MediaController")

    val controller = try {
        suspendCoroutine<MediaController> { cont ->
            controllerFuture.addListener({
                try {
                    cont.resume(controllerFuture.get())
                } catch (e: Throwable) {
                    cont.resumeWithException(e)
                }
            }, MoreExecutors.directExecutor())
        }
    } catch (e: Throwable) {
        Logger.e("player", "Failed to get MediaController", e)
        throw e
    }
    return AndroidPlayer(controller)
}*/
