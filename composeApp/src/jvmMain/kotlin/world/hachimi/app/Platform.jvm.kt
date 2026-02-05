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
        // Validate URL format to prevent command injection
        // URI constructor will throw if URL is malformed
        val uri = try {
            URI(url)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid URL: $url", e)
        }

        try {
            // Try Desktop.browse() first (works on Windows, macOS, and some Linux desktops)
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri)
                return
            }
        } catch (e: Exception) {
            // Desktop.browse() failed, will try fallback methods
        }

        // Fallback for Linux: try xdg-open and common browsers
        if (hostOs == OS.Linux) {
            val commands = listOf(
                listOf("xdg-open", url),
                listOf("gnome-open", url),
                listOf("kde-open", url),
                listOf("firefox", url),
                listOf("chromium", url),
                listOf("google-chrome", url)
            )

            for (command in commands) {
                try {
                    val process = Runtime.getRuntime().exec(command.toTypedArray())
                    // Wait briefly to check if command succeeded
                    if (process.waitFor(100, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                        // Process completed within timeout
                        if (process.exitValue() == 0) {
                            return
                        }
                    } else {
                        // Process is still running (likely succeeded in opening the URL)
                        return
                    }
                } catch (e: Exception) {
                    // Try next command
                }
            }
            throw Exception("Failed to open URL. Please install xdg-utils (xdg-open) or a web browser (firefox, chromium, or google-chrome).")
        } else {
            throw Exception("Desktop browse is not supported on this system")
        }
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