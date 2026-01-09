This is a Kotlin Multiplatform project targeting Android, iOS, Web, Desktop (JVM).

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
    - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
      For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
      the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
      Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
      folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack
channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).

You can open the web application by running the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task.

## Build Guide

1. Make sure to clone repo with tags

2. Install Rust toolchain (https://rustup.rs/)

3. Build rust library

```shell
cd ./rustLib
cargo build -r --lib

# Linux
cp target/release/libhachimi.so ../composeApp/src/jvmMain/resources
cargo run --bin uniffi-bindgen generate \
  --library target/release/libhachimi.so \
  --language kotlin --out-dir ../composeApp/src/jvmMain/kotlin

# macOS
cp target/release/libhachimi.dylib ../composeApp/src/jvmMain/resources
cargo run --bin uniffi-bindgen generate \
  --library target/release/libhachimi.dylib \
  --language kotlin --out-dir ../composeApp/src/jvmMain/kotlin

# Windows
cp target/release/hachimi.dll ../composeApp/src/jvmMain/resources
cargo run --bin uniffi-bindgen generate \
  --library target/release/hachimi.dll \
  --language kotlin --out-dir ../composeApp/src/jvmMain/kotlin
```

4. Add properties in `./local.properties`

```properties
app.dev.apiBaseUrl=https://your-api-host
app.dev.assetsBaseUrl=https://your-assets-host
```

5. Run JVM target

```shell
./gradlew :composeApp:jvmRun
```