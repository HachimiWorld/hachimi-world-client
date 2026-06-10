import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(21)

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
        binaries.executable()
        compilerOptions {
            freeCompilerArgs.add("-Xwasm-attach-js-exception")
            // https://kotlinlang.slack.com/archives/C3PQML5NU/p1758188562782809?thread_ts=1758060473.617919&cid=C3PQML5NU
            target = "es2015"
        }
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        val nonAndroidMain by creating {
            dependsOn(commonMain.get())
        }
        webMain {
            dependencies {
                dependsOn(nonAndroidMain)
                implementation(projects.composeApp)
                implementation(libs.compose.ui)

                implementation(libs.kotlinx.browser)
                implementation(libs.ktor.client.cio)
                implementation(libs.navigation3.browser)
                implementation(npm("howler", "2.2.4"))

                implementation(libs.jetbrains.navigation3.ui)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.koin.compose.viewmodelNavigation)
            }
        }
    }
}