package world.hachimi.app.update

/**
 * Platform side of in-app updates: where packages are stored, how they are downloaded and how they are installed.
 *
 * Desktop and Android download the package in the background and hand it to the system installer.
 * iOS and Web cannot install packages themselves and keep opening the download URL.
 */
interface AppUpdatePlatform {
    /** Whether packages can be downloaded and installed from inside the app. */
    val supportsInAppUpdate: Boolean

    /** Whether auto downloads should wait for the user, i.e. Android on a metered network. */
    fun isMeteredNetwork(): Boolean

    /**
     * Returns the package if it was already downloaded and verified, otherwise null.
     */
    suspend fun findDownloaded(spec: UpdatePackageSpec): UpdatePackage?

    /**
     * Downloads the package, resuming a previous partial download when possible, and verifies its size and SHA-256.
     * Other packages in the update directory are removed.
     *
     * @param onProgress called with downloaded bytes and the total size when known
     */
    suspend fun download(spec: UpdatePackageSpec, onProgress: (downloaded: Long, total: Long?) -> Unit): UpdatePackage

    /** Removes every downloaded package, e.g. after the app has been updated. */
    suspend fun clearDownloads()

    /** Hands a verified package to the system installer. */
    fun install(pkg: UpdatePackage)
}

/**
 * @property fileName file name to store the package under, see [packageFileName]
 * @property size expected size in bytes, null when the server does not know it
 * @property sha256 expected lowercase hex SHA-256, null when the server does not know it
 */
data class UpdatePackageSpec(
    val url: String,
    val fileName: String,
    val size: Long?,
    val sha256: String?,
)

/** A downloaded and verified package. */
data class UpdatePackage(val path: String)

/** For platforms that only open the download URL. */
object NoInAppUpdatePlatform : AppUpdatePlatform {
    override val supportsInAppUpdate: Boolean = false
    override fun isMeteredNetwork(): Boolean = false
    override suspend fun findDownloaded(spec: UpdatePackageSpec): UpdatePackage? = null
    override suspend fun download(
        spec: UpdatePackageSpec,
        onProgress: (downloaded: Long, total: Long?) -> Unit
    ): UpdatePackage = throw UnsupportedOperationException("In-app update is not supported on this platform")

    override suspend fun clearDownloads() {}
    override fun install(pkg: UpdatePackage) = throw UnsupportedOperationException("In-app update is not supported on this platform")
}

expect fun createAppUpdatePlatform(): AppUpdatePlatform

/**
 * Derives a safe local file name from the package URL, keeping the extension the system installer relies on
 * (.msi, .dmg, .apk). Falls back to `update-<versionNumber>` when the URL has no usable name.
 */
fun packageFileName(url: String, versionNumber: Int): String {
    val name = url.substringBefore('?').substringBefore('#').substringAfterLast('/')
        .filter { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' || it == '.' || it == '-' || it == '_' }
        .trimStart('.')
    return name.ifEmpty { "update-$versionNumber" }
}
