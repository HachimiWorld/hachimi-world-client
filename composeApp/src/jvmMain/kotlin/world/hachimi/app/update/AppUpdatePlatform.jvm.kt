package world.hachimi.app.update

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import world.hachimi.app.JVMPlatform
import java.awt.Desktop
import java.io.File

object DesktopAppUpdatePlatform : AppUpdatePlatform {
    private val downloader by lazy { PackageDownloader(JVMPlatform.getCacheDir().file.resolve("updates")) }

    override val supportsInAppUpdate: Boolean = true

    override fun isMeteredNetwork(): Boolean = false

    override suspend fun findDownloaded(spec: UpdatePackageSpec): UpdatePackage? =
        downloader.findDownloaded(spec)?.let { UpdatePackage(it.absolutePath) }

    override suspend fun download(
        spec: UpdatePackageSpec,
        onProgress: (downloaded: Long, total: Long?) -> Unit
    ): UpdatePackage = UpdatePackage(downloader.download(spec, onProgress).absolutePath)

    override suspend fun clearDownloads() = downloader.clear()

    override fun install(pkg: UpdatePackage) {
        val file = File(pkg.path)
        // Opening the package starts msiexec on Windows, mounts the DMG on macOS and opens the software center on Linux
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(file)
            return
        }
        val command = when (hostOs) {
            OS.MacOS -> listOf("open", file.absolutePath)
            OS.Windows -> listOf("msiexec", "/i", file.absolutePath)
            else -> listOf("xdg-open", file.absolutePath)
        }
        ProcessBuilder(command).start()
    }
}

actual fun createAppUpdatePlatform(): AppUpdatePlatform = DesktopAppUpdatePlatform
