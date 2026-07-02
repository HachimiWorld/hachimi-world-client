package world.hachimi.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.plugin.module.dsl.module
import world.hachimi.app.di.AndroidModule
import world.hachimi.app.model.GlobalStore

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initializeGlobalContext(this)

        val koin = startKoin {
            androidContext(this@MyApplication)
            module<AndroidModule>()
        }

        val global = koin.koin.get<GlobalStore>()
        global.initialize()
    }
}