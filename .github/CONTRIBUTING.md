# Contributing

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
app.release.apiBaseUrl=https://api.hachimi.world
app.release.assetsBaseUrl=https://api.hachimi.world
app.dev.apiBaseUrl=https://your-api-host
app.dev.assetsBaseUrl=https://your-assets-host
```

5. Run JVM target

```shell
./gradlew :composeApp:jvmRun
```