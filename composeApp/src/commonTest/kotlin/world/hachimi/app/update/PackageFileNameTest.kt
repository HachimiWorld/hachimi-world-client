package world.hachimi.app.update

import kotlin.test.Test
import kotlin.test.assertEquals

class PackageFileNameTest {
    @Test
    fun keepsTheFileNameAndExtension() {
        assertEquals(
            "hachimi-world-1.3.0-950.msi",
            packageFileName("https://cdn.example.com/distribution/windows/hachimi-world-1.3.0-950.msi", 950)
        )
    }

    @Test
    fun dropsQueryAndFragment() {
        assertEquals("app.apk", packageFileName("https://cdn.example.com/app.apk?token=abc#x", 1))
    }

    @Test
    fun dropsUnsafeCharacters() {
        assertEquals("app.dmg", packageFileName("https://cdn.example.com/%2E%2E/..app.dmg", 1))
        assertEquals("1.0.dmg", packageFileName("https://cdn.example.com/基米天堂 1.0.dmg", 1))
    }

    @Test
    fun fallsBackToVersionNumber() {
        assertEquals("update-7", packageFileName("https://cdn.example.com/", 7))
        assertEquals("update-7", packageFileName("", 7))
    }
}
