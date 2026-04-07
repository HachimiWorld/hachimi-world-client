package world.hachimi.app.di

import android.content.ComponentName
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import world.hachimi.app.player.AndroidPlayerEngine
import world.hachimi.app.player.PlayerEngine
import world.hachimi.app.service.PlaybackService

val androidModule = module {
    single<PlayerEngine> {
        val context = androidContext()
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        AndroidPlayerEngine(controllerFuture)
    }
}