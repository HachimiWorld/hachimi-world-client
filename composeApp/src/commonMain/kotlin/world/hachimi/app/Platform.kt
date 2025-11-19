package world.hachimi.app

import io.github.vinceglb.filekit.PlatformFile

interface Platform {
    val name: String
    val platformVersion: String
    val variant: String
    val userAgent: String
    fun getCacheDir(): PlatformFile
    fun getDataDir(): PlatformFile
    fun openUrl(url: String)
}

expect fun getPlatform(): Platform