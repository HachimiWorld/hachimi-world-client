package world.hachimi.app

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.browser.window

class JsPlatform : Platform {
    override val name: String = "Web/JS"
    // Get chrome or wasm virtual machine version
    override val platformVersion: String = "1"
    override val variant: String = "${BuildKonfig.BUILD_TYPE}-js"
    override val userAgent: String = "HachimiWorld-js/${BuildKonfig.VERSION_NAME} (${window.navigator.userAgent}; ${window.navigator.platform})"

    override fun getCacheDir(): PlatformFile {
        TODO()
    }

    override fun getDataDir(): PlatformFile {
        TODO("Not yet implemented")
    }

    override fun openUrl(url: String) {
        window.open(url, target = "_blank")
    }
}

actual fun getPlatform(): Platform = JsPlatform()