package world.hachimi.app.di

import org.koin.core.context.startKoin
import org.koin.plugin.module.dsl.module
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.player.IosPlayerServiceHelper

fun initKoin() {
    val koin = startKoin {
        module<IosModule>()
    }
    val global = koin.koin.get<GlobalStore>()
    global.initialize()
    val iosPlayerServiceHelper = koin.koin.get<IosPlayerServiceHelper>()
    iosPlayerServiceHelper.initialize()
}

