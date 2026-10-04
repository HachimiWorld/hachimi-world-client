package world.hachimi.app.update

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import world.hachimi.app.JVMPlatform
import world.hachimi.app.logging.Logger
import java.awt.Desktop
import java.io.File

private const val TAG = "DesktopAppUpdate"

object DesktopAppUpdatePlatform : AppUpdatePlatform {
    private val updatesDir by lazy { JVMPlatform.getCacheDir().file.resolve("updates") }
    private val downloader by lazy { PackageDownloader(updatesDir) }

    /** The launcher of an installed app (set by jpackage); null when running from Gradle. */
    private val launcher: File? by lazy {
        System.getProperty("jpackage.app-path")?.let(::File)?.takeIf { it.isFile }
    }

    /** The installed .app bundle on macOS, when this process may replace it. */
    private val replaceableMacBundle: File? by lazy {
        val bundle = launcher?.let { file -> generateSequence(file) { it.parentFile }.firstOrNull { it.name.endsWith(".app") } }
        when {
            hostOs != OS.MacOS || bundle == null -> null
            // Gatekeeper runs quarantined apps from a random read-only path; replacing that copy does nothing
            "/AppTranslocation/" in bundle.path -> null
            // Not writable: a standard user in /Applications, or the app running straight from a mounted DMG
            !bundle.canWrite() || bundle.parentFile?.canWrite() != true -> null
            else -> bundle
        }
    }

    override val supportsInAppUpdate: Boolean = true

    override val installsOnExit: Boolean
        get() = when (hostOs) {
            OS.Windows -> launcher != null
            OS.MacOS -> replaceableMacBundle != null
            else -> false
        }

    override fun isMeteredNetwork(): Boolean = false

    override suspend fun findDownloaded(spec: UpdatePackageSpec): UpdatePackage? =
        downloader.findDownloaded(spec)?.let { UpdatePackage(it.absolutePath) }

    override suspend fun download(
        spec: UpdatePackageSpec,
        onProgress: (downloaded: Long, total: Long?) -> Unit
    ): UpdatePackage = UpdatePackage(downloader.download(spec, onProgress).absolutePath)

    override suspend fun clearDownloads() = downloader.clear()

    override suspend fun install(pkg: UpdatePackage): InstallAction {
        if (startInstaller(File(pkg.path), relaunch = true)) return InstallAction.EXIT_APP
        openPackage(File(pkg.path))
        return InstallAction.HANDLED
    }

    override fun installOnExit(pkg: UpdatePackage): Boolean = startInstaller(File(pkg.path), relaunch = false)

    /** Starts an installer that runs once this process has exited. Returns false where that isn't possible. */
    private fun startInstaller(file: File, relaunch: Boolean): Boolean {
        val launcher = launcher
        val macBundle = replaceableMacBundle
        return when {
            hostOs == OS.Windows && launcher != null && file.extension.equals("msi", ignoreCase = true) -> {
                WindowsMsiInstaller.start(file, updatesDir, relaunch = launcher.takeIf { relaunch })
                true
            }

            macBundle != null && file.extension.equals("dmg", ignoreCase = true) -> {
                MacDmgInstaller.start(file, updatesDir, macBundle, relaunch)
                true
            }

            else -> false
        }
    }

    /** Opening the package starts msiexec on Windows, mounts the DMG on macOS and opens the software center on Linux. */
    private fun openPackage(file: File) {
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

/**
 * Installs an MSI after the app has exited. The app is a per-user MSI install, so this needs no UAC prompt.
 *
 * A hidden PowerShell script waits for this process to end (so no files are in use), runs msiexec, and starts
 * [relaunch] afterwards when given, whether or not the install succeeded. The msiexec log is kept next to the
 * package for diagnosing failed upgrades.
 */
internal object WindowsMsiInstaller {
    private val script = """
        param([int]${'$'}ParentPid, [string]${'$'}Msi, [string]${'$'}Log, [string]${'$'}Ui, [string]${'$'}Relaunch)
        try { Wait-Process -Id ${'$'}ParentPid -Timeout 60 -ErrorAction Stop } catch {}
        ${'$'}arguments = @('/i', "`"${'$'}Msi`"", ${'$'}Ui, '/norestart', '/l*v', "`"${'$'}Log`"")
        ${'$'}process = Start-Process -FilePath 'msiexec.exe' -ArgumentList ${'$'}arguments -Wait -PassThru
        if (${'$'}Relaunch) { Start-Process -FilePath ${'$'}Relaunch }
        exit ${'$'}process.ExitCode
    """.trimIndent()

    fun start(msi: File, workDir: File, relaunch: File?) {
        workDir.mkdirs()
        val scriptFile = workDir.resolve("install-update.ps1").apply { writeText(script) }
        val command = buildList {
            addAll(listOf("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass"))
            addAll(listOf("-WindowStyle", "Hidden", "-File", scriptFile.absolutePath))
            addAll(listOf("-ParentPid", ProcessHandle.current().pid().toString()))
            addAll(listOf("-Msi", msi.absolutePath))
            addAll(listOf("-Log", workDir.resolve("install-update.log").absolutePath))
            // Show msiexec's progress bar when the user asked to install now, stay silent when quitting
            addAll(listOf("-Ui", if (relaunch != null) "/passive" else "/qn"))
            if (relaunch != null) addAll(listOf("-Relaunch", relaunch.absolutePath))
        }
        Logger.i(TAG, "Starting MSI installer: $command")
        ProcessBuilder(command)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
    }
}

/**
 * Replaces the app bundle from a DMG after the app has exited.
 *
 * A shell script waits for this process to end, mounts the DMG without showing it in Finder, copies the new bundle
 * next to the old one, swaps them (keeping the old bundle until the new one is in place), detaches the DMG and
 * reopens the app when asked. The package was downloaded by the app itself, so it carries no quarantine flag and
 * opens without Gatekeeper prompts. Output goes to a log next to the package.
 */
internal object MacDmgInstaller {
    private val script = """
        #!/bin/sh
        PARENT_PID="${'$'}1"; DMG="${'$'}2"; APP="${'$'}3"; RELAUNCH="${'$'}4"
        exec >>"${'$'}5" 2>&1
        echo "== ${'$'}(date) installing ${'$'}DMG into ${'$'}APP"

        i=0
        while kill -0 "${'$'}PARENT_PID" 2>/dev/null && [ ${'$'}i -lt 120 ]; do sleep 0.5; i=${'$'}((i + 1)); done

        relaunch() { [ "${'$'}RELAUNCH" = "1" ] && open "${'$'}APP"; }
        # When the user asked to install and the app couldn't be replaced (e.g. macOS blocked it), show the DMG
        # so they can drag the app over themselves. Quitting with a pending update stays quiet instead.
        fallback() {
            echo "${'$'}1, kept the old app"
            [ "${'$'}RELAUNCH" = "1" ] && open "${'$'}DMG"
            relaunch
            exit 1
        }

        MOUNT=${'$'}(mktemp -d "${'$'}{TMPDIR:-/tmp}/hachimi-update.XXXXXX") || fallback "mktemp failed"
        # The package was already checked against its SHA-256, so skip hdiutil's own verification.
        # hdiutil attach is deprecated on recent macOS; fall back to diskutil should it stop working.
        if ! hdiutil attach -nobrowse -readonly -noautoopen -noverify -mountpoint "${'$'}MOUNT" "${'$'}DMG" &&
            ! diskutil image attach --mountOptions nobrowse --readOnly --mountPoint "${'$'}MOUNT" "${'$'}DMG"; then
            rmdir "${'$'}MOUNT"
            fallback "mount failed"
        fi

        SOURCE=${'$'}(find "${'$'}MOUNT" -maxdepth 1 -name "*.app" -print | head -n 1)
        STAGED="${'$'}APP.updating"
        OLD="${'$'}APP.old"
        rm -rf "${'$'}STAGED" "${'$'}OLD"
        RESULT="installed"
        if [ -z "${'$'}SOURCE" ] || ! ditto "${'$'}SOURCE" "${'$'}STAGED"; then
            RESULT="copy failed"
        else
            xattr -dr com.apple.quarantine "${'$'}STAGED" 2>/dev/null
            if ! mv "${'$'}APP" "${'$'}OLD"; then
                RESULT="moving the old app failed"
            elif ! mv "${'$'}STAGED" "${'$'}APP"; then
                mv "${'$'}OLD" "${'$'}APP"
                RESULT="moving the new app failed"
            fi
        fi
        rm -rf "${'$'}STAGED"
        # Never delete the old copy unless an app is in place
        [ -d "${'$'}APP" ] && rm -rf "${'$'}OLD"

        hdiutil detach "${'$'}MOUNT" -quiet || hdiutil detach "${'$'}MOUNT" -force -quiet
        rmdir "${'$'}MOUNT" 2>/dev/null
        [ "${'$'}RESULT" = "installed" ] || fallback "${'$'}RESULT"
        echo "installed"
        relaunch
    """.trimIndent()

    fun start(dmg: File, workDir: File, bundle: File, relaunch: Boolean) {
        workDir.mkdirs()
        val scriptFile = workDir.resolve("install-update.sh").apply { writeText(script) }
        val command = listOf(
            "/bin/sh", scriptFile.absolutePath,
            ProcessHandle.current().pid().toString(),
            dmg.absolutePath,
            bundle.absolutePath,
            if (relaunch) "1" else "0",
            workDir.resolve("install-update.log").absolutePath,
        )
        Logger.i(TAG, "Starting DMG installer: $command")
        ProcessBuilder(command)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
    }
}

actual fun createAppUpdatePlatform(): AppUpdatePlatform = DesktopAppUpdatePlatform
