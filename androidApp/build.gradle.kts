plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinx.atomicfu)
    alias(libs.plugins.ksp)
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
        /*create("beta") {
            isMinifyEnabled = true
            applicationIdSuffix = ".dev"
        }*/
        /*debug {
            applicationIdSuffix = ".dev"
//            resValue("string", "app_name", "@string/app_name_dev")
        }*/
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        resValues = true
    }
}


dependencies {
    implementation(projects.composeApp)

    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.toolingPreview)
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
    implementation(libs.jetbrains.navigation3.ui)
    implementation(libs.jetbrains.material3.adaptiveNavigation3)
    implementation(libs.jetbrains.lifecycle.viewmodelNavigation3)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)

//            implementation(libs.androidx.palette)
//            implementation(libs.androidx.palette.ktx)

    implementation(libs.koin.android)
    implementation(libs.room.runtime)

    implementation(libs.ktor.client.cio)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.kotlin.test)
    debugImplementation(libs.compose.ui.tooling)
}