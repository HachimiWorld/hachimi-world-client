package world.hachimi.app

import io.github.vinceglb.filekit.PlatformFile
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostArch
import org.jetbrains.skiko.hostOs
import java.awt.Desktop
import java.io.File
import java.net.URI

object JVMPlatform : Platform {
    override val name: String = "JVM"
    override val platformVersion: String = System.getProperty("java.version")
    override val variant: String = getVariant()
    override val userAgent: String = "HachimiWorld-jvm/${BuildKonfig.VERSION_NAME} (${hostFullName})"

    private val appName = BuildKonfig.APP_PACKAGE_NAME

    override fun getCacheDir(): PlatformFile {
        val file = when (hostOs) {
            OS.Windows -> File(System.getenv("LOCALAPPDATA"), "/Cache")
            OS.MacOS -> File(System.getProperty("user.home"), "/Library/Caches")
            OS.Linux -> System.getProperty("XDG_DATA_HOME")?.let { File(it) }
                ?: File(System.getProperty("user.home"), "/.local/cache")
            else -> error("Unsupported os: $hostOs")
        }.resolve(appName).also { if (!it.exists()) it.mkdirs() }
        return PlatformFile(file)
    }

    override fun getDataDir(): PlatformFile {
        val file = when (hostOs) {
            OS.Windows -> File(System.getenv("APPDATA"))
            OS.MacOS -> File(System.getProperty("user.home"), "/Library/Application Support")
            OS.Linux -> System.getProperty("XDG_DATA_HOME")?.let { File(it) }
                ?: File(System.getProperty("user.home"), "/.local/share")
            else -> error("Unsupported os: $hostOs")
        }.resolve(appName).also { if (!it.exists()) it.mkdirs() }
        return PlatformFile(file)
    }

    override fun openUrl(url: String) {
        val desktop = Desktop.getDesktop()
        desktop.browse(URI(url))
    }
}

actual fun getPlatform(): Platform = JVMPlatform

private fun getVariant(): String {
    val type = BuildKonfig.BUILD_TYPE
    val platform = when(hostOs) {
        OS.Windows -> "windows"
        OS.MacOS -> "macos"
        OS.Linux -> "linux"
        else -> "unknown"
    }
    return "$type-$platform"
}

internal val hostFullName by lazy {
    "${System.getProperty("os.name")}, ${System.getProperty("os.version")}, ${hostArch.id}"
}