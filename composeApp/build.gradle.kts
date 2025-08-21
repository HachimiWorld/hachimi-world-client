import com.android.SdkConstants
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.util.Properties
import kotlin.apply
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type

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
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.androidx.core.ktx)

            implementation(libs.koin.android)
            implementation(libs.room.runtime)

            implementation(libs.ktor.client.cio)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodelNavigation)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.encoding)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.kotlinx.datetime)

            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.datastore.preferences)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs) {
                exclude("org.jetbrains.compose.material")
            }
            implementation(libs.kotlinx.coroutinesSwing)

            implementation(libs.ktor.client.cio)
            implementation(libs.logback)

            implementation(libs.room.runtime)

        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    add("kspJvm", libs.room.compiler)
    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
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
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "world.hachimi.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = gitVersionCode.get()
        versionName = gitVersionName.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "world.hachimi.app"
            packageVersion = gitVersionNameShort.get()
            modules("jdk.unsupported")
        }
    }
}

buildkonfig {
    packageName = "world.hachimi.app"

    val props = Properties().apply { load(rootProject.file(SdkConstants.FN_LOCAL_PROPERTIES).reader()) }

    defaultConfigs {
        buildConfigField(Type.LONG, "BUILD_TIME", System.currentTimeMillis().toString())
        buildConfigField(Type.INT,"VERSION_CODE", gitVersionCode.get().toString())
        buildConfigField(Type.STRING,"VERSION_NAME", gitVersionName.get())

        buildConfigField(Type.STRING, "BUILD_TYPE", "dev")
        buildConfigField(Type.STRING, "APP_PACKAGE_NAME", "world.hachimi.app.dev")
        buildConfigField(Type.STRING, "APP_NAME", "KeepTally Dev")
        buildConfigField(Type.STRING, "API_BASE_URL", props.getProperty("app.dev.apiBaseUrl"))
    }

    defaultConfigs("release") {
        buildConfigField(Type.STRING, "BUILD_TYPE", "release")
        buildConfigField(Type.STRING, "APP_PACKAGE_NAME", "world.hachimi.app")
        buildConfigField(Type.STRING, "APP_NAME", "KeepTally")
        buildConfigField(Type.STRING, "API_BASE_URL", props.getProperty("app.release.apiBaseUrl"))
    }

    defaultConfigs("beta") {
        buildConfigField(Type.STRING, "BUILD_TYPE", "beta")
        buildConfigField(Type.STRING, "APP_PACKAGE_NAME", "world.hachimi.app.beta")
        buildConfigField(Type.STRING, "APP_NAME", "KeepTally")
        buildConfigField(Type.STRING, "API_BASE_URL", props.getProperty("app.release.apiBaseUrl"))
    }
}
