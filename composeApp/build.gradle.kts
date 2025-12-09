import com.android.SdkConstants
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinx.atomicfu)
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        iosTarget.compilations.getByName("main") {
            // Add the NSKeyValueObserving interface so we can use KVO in kotlin native
            // https://proandroiddev.com/leveraging-key-value-observing-kvo-in-kotlin-multiplatform-kmp-for-ios-231519e5c1ff
            // FIXME: Enabling this will get KLIB resolver error
//            val nskeyvalueobserving by cinterops.creating
        }
    }

    jvm()
    js {
        browser()
        binaries.executable()
        compilerOptions {
            // https://kotlinlang.slack.com/archives/C3PQML5NU/p1758188562782809?thread_ts=1758060473.617919&cid=C3PQML5NU
            target = "es2015"
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        compilerOptions {
            freeCompilerArgs.add("-Xwasm-attach-js-exception")
            // https://kotlinlang.slack.com/archives/C3PQML5NU/p1758188562782809?thread_ts=1758060473.617919&cid=C3PQML5NU
            target = "es2015"
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.components.uiToolingPreview)

            implementation(libs.compose.material3)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodelNavigation)

            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.encoding)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.filekit.coil)

            implementation(libs.haze)
//            implementation(libs.kmpalette.core)
//            implementation(libs.materialMotion)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.browser)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.media3.session)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.exoplayer.dash)

            implementation(libs.koin.android)
            implementation(libs.room.runtime)

            implementation(libs.ktor.client.cio)
            implementation(libs.androidx.datastore.preferences)
        }
        val nonAndroidMain by creating {
            dependsOn(commonMain.get())
        }
        jvmMain {
            dependsOn(nonAndroidMain)
            dependencies {
                implementation(compose.desktop.currentOs) {
                    exclude("org.jetbrains.compose.material")
                }
                implementation(libs.kotlinx.coroutinesSwing)

                implementation(libs.ktor.client.cio)
                implementation(libs.logback)

                implementation(libs.androidx.datastore.preferences)
//            implementation(libs.room.runtime)
//            implementation(libs.androidx.sqlite.bundled)
                implementation(libs.mp3spi)
                implementation(libs.jflac)

                implementation(libs.jna)
                implementation(libs.jna.platform)
            }
        }
        webMain {
            dependsOn(nonAndroidMain)
            dependencies {
                implementation(libs.ktor.client.cio)
                implementation(libs.kotlinx.browser)
                implementation(npm("howler", "2.2.4"))
            }
        }
        jsMain {
            dependsOn(webMain.get())
        }
        wasmJsMain {
            dependsOn(webMain.get())
        }
        listOf(iosX64Main, iosArm64Main, iosSimulatorArm64Main).forEach { iosTarget->
            iosTarget {
                dependsOn(nonAndroidMain)
                dependencies {
                    implementation(libs.ktor.client.darwin)
                }
            }
        }
    }

    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
//    add("kspJvm", libs.room.compiler)
//    add("kspAndroid", libs.room.compiler)
//    add("kspIosSimulatorArm64", libs.room.compiler)
//    add("kspIosX64", libs.room.compiler)
//    add("kspIosArm64", libs.room.compiler)
}

val gitVersionCode = providers.exec {
    commandLine("git", "rev-list", "--count", "--first-parent", "HEAD")
}.standardOutput.asText.map {
    it.trim().toInt()
}

val gitVersionName = providers.exec {
    commandLine("git", "describe", "--tags", "--match", "v[0-9]*")
}.standardOutput.asText.map {
    it.trim().trimStart('v') // Remove prefix 'vx.x.x'
}

val gitVersionNameShort = gitVersionName.map { it.substringBefore("-") }

android {
    namespace = "world.hachimi.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "world.hachimi.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = 35
        versionCode = gitVersionCode.get()
        versionName = gitVersionName.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        if (System.getenv("IS_CI") == "true") {
            register("release") {
                storeFile = file(System.getenv("ANDROID_KEYSTORE_FILE"))
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            if (System.getenv("IS_CI") == "true") {
                signingConfig = signingConfigs.getByName("release")
            }
            resValue("string", "app_name", "@string/app_name_base")
        }
        create("beta") {
            isMinifyEnabled = true
            applicationIdSuffix = ".dev"
        }
        debug {
            applicationIdSuffix = ".dev"
//            resValue("string", "app_name", "@string/app_name_dev")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "world.hachimi.app.MainKt"
        jvmArgs += listOf("-Xmx300M")
        val flavor = project.findProperty("buildkonfig.flavor")
        when (flavor) {
            "release" -> nativeDistributions {
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                packageName = "Hachimi World"
                description = "A community-driven open-source meme culture music community."
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

buildkonfig {
    packageName = "world.hachimi.app"

    val props = Properties().apply { load(rootProject.file(SdkConstants.FN_LOCAL_PROPERTIES).reader()) }

    defaultConfigs {
        buildConfigField(Type.LONG, "BUILD_TIME", System.currentTimeMillis().toString())
        buildConfigField(Type.INT, "VERSION_CODE", gitVersionCode.get().toString())
        buildConfigField(Type.STRING, "VERSION_NAME", gitVersionName.get())

        buildConfigField(Type.STRING, "BUILD_TYPE", "dev")
        buildConfigField(Type.STRING, "APP_PACKAGE_NAME", "world.hachimi.app.dev")
        buildConfigField(Type.STRING, "APP_NAME", "基米天堂 Dev")
        buildConfigField(Type.STRING, "API_BASE_URL", props.getProperty("app.dev.apiBaseUrl"))
        buildConfigField(Type.STRING, "ASSETS_BASE_URL", props.getProperty("app.dev.assetsBaseUrl"))
    }

    defaultConfigs("release") {
        buildConfigField(Type.STRING, "BUILD_TYPE", "release")
        buildConfigField(Type.STRING, "APP_PACKAGE_NAME", "world.hachimi.app")
        buildConfigField(Type.STRING, "APP_NAME", "基米天堂")
        buildConfigField(Type.STRING, "API_BASE_URL", props.getProperty("app.release.apiBaseUrl"))
        buildConfigField(Type.STRING, "ASSETS_BASE_URL", props.getProperty("app.release.assetsBaseUrl"))
    }

    defaultConfigs("beta") {
        buildConfigField(Type.STRING, "BUILD_TYPE", "beta")
        buildConfigField(Type.STRING, "APP_PACKAGE_NAME", "world.hachimi.app.beta")
        buildConfigField(Type.STRING, "APP_NAME", "基米天堂 Beta")
        buildConfigField(Type.STRING, "API_BASE_URL", props.getProperty("app.release.apiBaseUrl"))
        buildConfigField(Type.STRING, "ASSETS_BASE_URL", props.getProperty("app.release.assetsBaseUrl"))
    }
}

tasks.register("printVersions") {
    println(gitVersionName.get() + " " + gitVersionCode.get() + " " + gitVersionNameShort.get())
}