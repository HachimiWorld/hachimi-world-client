package world.hachimi.app

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import io.github.vinceglb.filekit.PlatformFile


class AndroidPlatform : Platform {
    override val name: String = "Android"
    override val platformVersion: String = Build.VERSION.SDK_INT.toString()
    override val variant: String = "${BuildKonfig.BUILD_TYPE}-android"
    override val userAgent: String = "HachimiWorld-android/${BuildKonfig.VERSION_NAME} (${Build.MANUFACTURER}; ${Build.MODEL}; ${Build.VERSION.RELEASE})"

    override fun getCacheDir(): PlatformFile {
        return PlatformFile(applicationContext.cacheDir)
    }

    override fun getDataDir(): PlatformFile {
        return PlatformFile(applicationContext.filesDir)
    }

    override fun openUrl(url: String) {
        val intent = CustomTabsIntent.Builder().build()
        intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.launchUrl(applicationContext, url.toUri())
    }
}

private lateinit var applicationContext: Context

fun initializeGlobalContext(context: Context) {
    applicationContext = context
}

actual fun getPlatform(): Platform = AndroidPlatform()