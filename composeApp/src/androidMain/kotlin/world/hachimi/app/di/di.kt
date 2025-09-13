package world.hachimi.app.di

import android.content.ComponentName
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import io.github.vinceglb.filekit.AndroidFile
import okio.Path.Companion.toOkioPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.ApiClient
import world.hachimi.app.getPlatform
import world.hachimi.app.model.*
import world.hachimi.app.player.AndroidPlayer
import world.hachimi.app.player.Player
import world.hachimi.app.service.PlaybackService
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.MyDataStoreImpl
import world.hachimi.app.storage.SongCache
import world.hachimi.app.storage.SongCacheImpl

val appModule = module {
    single { ApiClient(BuildKonfig.API_BASE_URL) }
    single { getPreferencesDataStore() }
    single<MyDataStore> { MyDataStoreImpl(get()) }
    single<Player> {
        val sessionToken = SessionToken(androidContext(), ComponentName(androidContext(), PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(androidContext(), sessionToken).buildAsync()
        AndroidPlayer(controllerFuture)
    }
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

private fun getPreferencesDataStore(): DataStore<Preferences> {
    val file = (getPlatform().getDataDir().androidFile as AndroidFile.FileWrapper)
        .file.resolve("settings.preferences_pb")

    return PreferenceDataStoreFactory.createWithPath { file.toOkioPath() }
}