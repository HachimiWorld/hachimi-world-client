package world.hachimi.app.update

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.ConnectivityManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.hachimi.app.applicationContext
import world.hachimi.app.logging.Logger
import java.io.File

private const val TAG = "AndroidAppUpdate"

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

    override suspend fun install(pkg: UpdatePackage): InstallAction {
        val context = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            // The user allows installs from this app once, then taps install again
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, "package:${context.packageName}".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return InstallAction.HANDLED
        }
        withContext(Dispatchers.IO) { installWithSession(context, File(pkg.path)) }
        return InstallAction.HANDLED
    }

    /**
     * Installs through a [PackageInstaller] session. Once this app installed itself, Android 12+ can update without a
     * confirmation; otherwise the system asks the user, see [InstallStatusReceiver].
     */
    private fun installWithSession(context: Context, apk: File) {
        InstallStatusReceiver.register(context)
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(context.packageName)
            setSize(apk.length())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
            }
        }
        val sessionId = installer.createSession(params)
        try {
            installer.openSession(sessionId).use { session ->
                session.openWrite("base.apk", 0, apk.length()).use { out ->
                    apk.inputStream().use { it.copyTo(out) }
                    session.fsync(out)
                }
                val intent = Intent(InstallStatusReceiver.ACTION)
                    .setPackage(context.packageName)
                    .putExtra(InstallStatusReceiver.EXTRA_APK_PATH, apk.absolutePath)
                // The installer adds the status extras, so the intent must stay mutable
                val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
                val pending = PendingIntent.getBroadcast(context, sessionId, intent, flags)
                session.commit(pending.intentSender)
            }
        } catch (e: Throwable) {
            installer.abandonSession(sessionId)
            throw e
        }
    }

    /** The pre-session way: hand the APK to the system installer screen. */
    internal fun openWithSystemInstaller(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", apk)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }
}

/** Receives install session status: asks the user when Android requires it, falls back when the session fails. */
private object InstallStatusReceiver : BroadcastReceiver() {
    const val ACTION = "world.hachimi.app.update.INSTALL_STATUS"
    const val EXTRA_APK_PATH = "apk_path"

    @Volatile
    private var registered = false

    fun register(context: Context) {
        if (registered) return
        synchronized(this) {
            if (registered) return
            ContextCompat.registerReceiver(context, this, IntentFilter(ACTION), ContextCompat.RECEIVER_NOT_EXPORTED)
            registered = true
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java) ?: return
                context.startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }

            PackageInstaller.STATUS_SUCCESS -> Logger.i(TAG, "Update installed")
            // The user declined in the confirmation screen
            PackageInstaller.STATUS_FAILURE_ABORTED -> Logger.i(TAG, "Update install cancelled")
            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Logger.e(TAG, "Update install failed: $status $message")
                // The system installer screen explains what went wrong, e.g. a signature mismatch
                val apk = intent.getStringExtra(EXTRA_APK_PATH)?.let(::File)?.takeIf { it.isFile } ?: return
                AndroidAppUpdatePlatform.openWithSystemInstaller(context, apk)
            }
        }
    }
}

actual fun createAppUpdatePlatform(): AppUpdatePlatform = AndroidAppUpdatePlatform
