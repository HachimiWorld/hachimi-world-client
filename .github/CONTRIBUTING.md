# Contributing

## Build Guide

### 1. Make sure to clone repo with tags

### 2. Install Rust toolchain

See https://rustup.rs/

### 3. Rust library

Install dependencies for linux

```shell
# This is essential for build `rodio` dependency.
sudo apt install libasound2-dev libdbus-1-dev 
```

Gradle builds the library with cargo and generates the Kotlin bindings automatically
when compiling the JVM target, so there is nothing else to run by hand.
It builds in release mode by default; pass `-P rust.debug=true` for a faster debug build.

If you have old `libhachimi.*` / `hachimi.dll` files in `composeApp/src/jvmMain/resources`
from the previous manual setup, delete them.

### 4. Add properties in `./local.properties`

```properties
app.release.apiBaseUrl=https://api.hachimi.world
app.release.assetsBaseUrl=https://api.hachimi.world
app.dev.apiBaseUrl=https://your-api-host
app.dev.assetsBaseUrl=https://your-assets-host
```

### 5. Build and run JVM target

JDK Version: 21

```shell
./gradlew run
```