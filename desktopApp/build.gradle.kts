import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.koinCompiler)
}

@Suppress("UNCHECKED_CAST")
val gitVersionCode = rootProject.extra["gitVersionCode"] as Provider<Int>

@Suppress("UNCHECKED_CAST")
val gitVersionName = rootProject.extra["gitVersionName"] as Provider<String>

@Suppress("UNCHECKED_CAST")
val gitVersionNameShort = rootProject.extra["gitVersionNameShort"] as Provider<String>

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.composeApp)
    implementation(compose.desktop.currentOs) {
        exclude("org.jetbrains.compose.material")
    }

    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.toolingPreview)
    implementation(libs.compose.components.resources)
    implementation(libs.compose.components.uiToolingPreview)
//    implementation(libs.compose.material)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.logback)
//    implementation(libs.compose.components.splitpane)

    implementation(libs.kotlinx.coroutines.swing)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
//    implementation(libs.koin.annotations)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
//    implementation(libs.koin.compose.viewmodel.navigation)

    implementation(libs.kotlinx.coroutines.swing)

    implementation(libs.ktor.client.cio)
    implementation(libs.logback)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.mp3spi)
    implementation(libs.jflac)

    implementation(libs.jna)
    implementation(libs.jna.platform)
}

compose.desktop {
    application {
        mainClass = "world.hachimi.app.MainKt"
        jvmArgs += listOf("-XX:+UseZGC", "-XX:+ZGenerational", "-Xms128M", "-Xmx512M")
        val flavor = project.findProperty("buildkonfig.flavor")
        when (flavor) {
            "release" -> nativeDistributions {
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                packageName = "Hachimi World"
                vendor = "Hachimi World, NPO"
                copyright = "© 2025 Hachimi World Open Source Project"
                packageVersion = gitVersionNameShort.get()
                licenseFile = rootProject.file("LICENSE")

                modules("jdk.unsupported", "java.naming")

                windows {
                    upgradeUuid = "1544B476-25C9-4A01-705E-B374B14B2F1B"
                    perUserInstall = true
                    dirChooser = false
                    shortcut = true
                    menu = true
                    iconFile.set(rootProject.file("icons/icon.ico"))
                }
                macOS {
                    appCategory = "public.app-category.entertainment"
                    packageName = "基米天堂"
                    dockName = "基米天堂"
                    bundleID = "world.hachimi.app"
                    iconFile.set(rootProject.file("icons/icon.icns"))
                }
                linux {
                    packageName = "hachimi-world" // Linux does not support Chinese characters
                    iconFile.set(rootProject.file("icons/icon.png"))
                }
            }
            else -> nativeDistributions {
                targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
                packageName = "Hachimi World Dev"
                packageVersion = gitVersionNameShort.get()
                modules("jdk.unsupported", "java.naming")

                windows {
                    upgradeUuid = "8AD88FC9-A6A2-478D-9E80-FF735EE15785"
                    perUserInstall = true
                    dirChooser = false
                    shortcut = true
                    menu = true
                    iconFile.set(rootProject.file("icons/icon.ico"))
                }
                macOS {
                    packageName = "基米天堂 Dev"
                    bundleID = "world.hachimi.app.dev"
                    appCategory = "public.app-category.entertainment"
                    iconFile.set(rootProject.file("icons/icon.icns"))
                }
                linux {
                    packageName = "hachimi-world-dev" // Linux does not support Chinese characters
                    iconFile.set(rootProject.file("icons/icon.png"))
                }
            }
        }
    }
}