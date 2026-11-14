package world.hachimi.app.ui.util

/** How saving an image went. @since 261113 */
enum class SaveImageResult {
    SAVED,
    /** Handed to the browser, which shows the download itself. */
    DOWNLOADING,
    CANCELLED,
    FAILED,
}

/**
 * Saves an image where the platform keeps them: the photo gallery on phones, a file the user picks
 * on desktop, a download on the web.
 *
 * @param fileName With its extension.
 * @since 261113
 */
expect suspend fun saveImage(bytes: ByteArray, fileName: String): SaveImageResult
