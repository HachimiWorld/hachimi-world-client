package world.hachimi.app.update

import android.content.Intent
import android.net.ConnectivityManager
import androidx.core.content.FileProvider
import world.hachimi.app.applicationContext
import java.io.File

/** Serves downloaded APKs to the system package installer. Declared in the app manifest. */
class UpdateFileProvider : FileProvider()

object AndroidAppUpdatePlatform : AppUpdatePlatform {
    private val downloader by lazy { PackageDownloader(File(applicationContext.cacheDir, "updates")) }

    override val supportsInAppUpdate: Boolean = true

    override fun isMeteredNetwork(): Boolean {
        val connectivity = applicationContext.getSystemService(ConnectivityManager::class.java) ?: return false
        return connectivity.isActiveNetworkMetered
    }

    override suspend fun findDownloaded(spec: UpdatePackageSpec): UpdatePackage? =
        downloader.findDownloaded(spec)?.let { UpdatePackage(it.absolutePath) }

    override suspend fun download(
        spec: UpdatePackageSpec,
        onProgress: (downloaded: Long, total: Long?) -> Unit
    ): UpdatePackage = UpdatePackage(downloader.download(spec, onProgress).absolutePath)

    override suspend fun clearDownloads() = downloader.clear()

    override fun install(pkg: UpdatePackage) {
        val context = applicationContext
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", File(pkg.path))
        // The system installer asks the user to allow installs from this app the first time
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }
}

actual fun createAppUpdatePlatform(): AppUpdatePlatform = AndroidAppUpdatePlatform
