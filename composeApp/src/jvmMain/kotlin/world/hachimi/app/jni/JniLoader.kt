package world.hachimi.app.jni

import world.hachimi.app.logging.Logger

class JniLoader {
    init {
        val url = ClassLoader.getSystemResource(
            System.mapLibraryName("hachimi")
        )
        Logger.d("JniLoader", "Lib url: $url")
        System.load(url.path)
    }
    external fun javaInit()
}